/**
 * Copyright (c) 2007-2015, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 	- Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * 	  disclaimer.
 * 	- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * 	  following disclaimer in the documentation and/or other materials provided with the distribution.
 * 	- Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote
 * 	  products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package lupos.optimizations.logical.rules.parallel;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.multiinput.join.Join;
import lupos.engine.operators.multiinput.join.MergeJoinWithoutSorting;
import lupos.engine.operators.multiinput.optional.MergeWithoutSortingOptional;
import lupos.engine.operators.singleinput.parallel.ParallelOperand;
import lupos.engine.operators.singleinput.parallel.QueryResultInBlocks;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.Rule;

/**
 * Implements a graph transformation which inserts a {@link lupos.engine.operators.singleinput.parallel.ParallelOperand}
 * between each {@link lupos.engine.operators.multiinput.join.Join} operator and its arguments, effectively evaluating
 * in a separate thread and thus distributing it across possibly multiple
 * processors.
 *
 * @see ParallelOperand
 * @author groppe
 * @version $Id: $Id
 */
public class RuleJoinWithParallelOperands extends Rule {

	/** Constant <code>BLOCKWISE=false</code> */
	protected static boolean BLOCKWISE = false;

	/**
	 * <p>isBLOCKWISE.</p>
	 *
	 * @return a boolean.
	 */
	public static boolean isBLOCKWISE() {
		return BLOCKWISE;
	}

	/**
	 * <p>setBLOCKWISE.</p>
	 *
	 * @param blockwise a boolean.
	 */
	public static void setBLOCKWISE(final boolean blockwise) {
		BLOCKWISE = blockwise;
	}

	/** {@inheritDoc} */
	@Override
	protected void init() {
		final Operator a = new Join();

		subGraphMap = new HashMap<BasicOperator, String>();
		subGraphMap.put(a, "join");

		startNode = a;
	}

	/** {@inheritDoc} */
	@Override
	protected boolean checkPrecondition(final Map<String, BasicOperator> mso) {
		final BasicOperator join = mso.get("join");
		if (isBLOCKWISE()
				&& (join instanceof MergeJoinWithoutSorting || join instanceof MergeWithoutSortingOptional)) {
			// blockwise does not work for merge joins/optionals!
			return false;
		} else
			return true;
	}

	/** {@inheritDoc} */
	@Override
	protected Tuple<Collection<BasicOperator>, Collection<BasicOperator>> transformOperatorGraph(
			final Map<String, BasicOperator> mso,
			final BasicOperator rootOperator) {
		final Collection<BasicOperator> deleted = new LinkedList<BasicOperator>();
		final Collection<BasicOperator> added = new LinkedList<BasicOperator>();
		OperatorIDTuple tuple;
		final BasicOperator join = mso.get("join");
		final List<BasicOperator> pre = join.getPrecedingOperators();

		join.setPrecedingOperators(new LinkedList<BasicOperator>());

		for (final BasicOperator op : pre) {
			if (op instanceof ParallelOperand
					|| op instanceof QueryResultInBlocks) {
				join.addPrecedingOperator(op);
				continue;
			}

			final Operator par = new ParallelOperand();
			par.addPrecedingOperator(op);
			added.add(par);

			final Operator maybequeryresultinblocks;
			if (isBLOCKWISE()) {
				maybequeryresultinblocks = new QueryResultInBlocks();
				added.add(maybequeryresultinblocks);
				par.setSucceedingOperator(new OperatorIDTuple(
						maybequeryresultinblocks, 0));
				maybequeryresultinblocks.addPrecedingOperator(par);
			} else {
				maybequeryresultinblocks = par;
			}
			tuple = op.getOperatorIDTuple(join);
			op.replaceOperatorIDTuple(tuple, new OperatorIDTuple(par, 0));

			join.addPrecedingOperator(maybequeryresultinblocks);
			maybequeryresultinblocks.addSucceedingOperator(new OperatorIDTuple(
					join, tuple.getId()));
		}

		rootOperator.detectCycles();
		rootOperator.sendMessage(new BoundVariablesMessage());
		if (!deleted.isEmpty() || !added.isEmpty())
			return new Tuple<Collection<BasicOperator>, Collection<BasicOperator>>(
					added, deleted);
		else
			return null;
	}

	/** {@inheritDoc} */
	@Override
	public String getName() {
		return "JoinWithParallelOperands";
	}
}
