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
package lupos.engine.operators.singleinput.sort;

import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.queryresult.ParallelIterator;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.queryresult.QueryResultDebug;
import lupos.datastructures.sorteddata.SortedBag;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.ComputeIntermediateResultMessage;
import lupos.engine.operators.messages.EndOfEvaluationMessage;
import lupos.engine.operators.messages.Message;
import lupos.misc.debug.DebugStep;
public abstract class SortedBagSort extends Sort {

	protected SortedBag<Bindings> sswd;

	/**
	 * <p>Constructor for SortedBagSort.</p>
	 */
	public SortedBagSort() {
		super();
	}

	/**
	 * <p>Constructor for SortedBagSort.</p>
	 *
	 * @param sswd a {@link lupos.datastructures.sorteddata.SortedBag} object.
	 * @param node a lupos$sparql1_1$Node object.
	 */
	public SortedBagSort(final SortedBag<Bindings> sswd,
			final lupos.sparql1_1.Node node) {
		super(node);
		this.sswd = sswd;
	}

	/** {@inheritDoc} */
	@Override
	public synchronized QueryResult process(final QueryResult bindings,
			final int operandID) {
		final Iterator<Bindings> itb = bindings.oneTimeIterator();
		while (itb.hasNext()) {
			this.sswd.add(itb.next());
		}
		return null;
	}

	/**
	 * <p>getIterator.</p>
	 *
	 * @return a {@link lupos.datastructures.queryresult.ParallelIterator} object.
	 */
	protected ParallelIterator<Bindings> getIterator() {
		final Iterator<Bindings> itb = this.sswd.iterator();
		return new ParallelIterator<Bindings>() {

			@Override
			public void close() {
				// derived classes may override the above method in order to
				// release some resources here!
			}

			@Override
			public boolean hasNext() {
				return itb.hasNext();
			}

			@Override
			public Bindings next() {
				return itb.next();
			}

			@Override
			public void remove() {
				itb.remove();
			}

		};
	}

	private void computeResult(){
		final QueryResult qr = QueryResult.createInstance(this.getIterator());
		if (this.succeedingOperators.size() > 1) {
			qr.materialize();
		}
		for (final OperatorIDTuple opId : this.succeedingOperators) {
			opId.processAll(qr);
		}
	}

	/** {@inheritDoc} */
	@Override
	public Message preProcessMessage(final EndOfEvaluationMessage msg) {
		this.computeResult();
		return msg;
	}

	/** {@inheritDoc} */
	@Override
	public Message preProcessMessage(final ComputeIntermediateResultMessage msg) {
		this.computeResult();
		return msg;
	}

	/** {@inheritDoc} */
	@Override
	public QueryResult deleteQueryResult(final QueryResult queryResult, final int operandID) {
		// problem: it does not count the number of occurences of a binding
		// i.e. { ?a=<a> }, { ?a=<a> } and delete { ?a=<a> } will result in
		// {} instead of { ?a=<a> }!!!!!!
		final Iterator<Bindings> itb = queryResult.oneTimeIterator();
		while (itb.hasNext()) {
			this.sswd.remove(itb.next());
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public void deleteAll(final int operandID) {
		this.sswd.clear();
	}

	/** {@inheritDoc} */
	@Override
	protected boolean isPipelineBreaker() {
		return true;
	}

	private void computeResultDebug(final DebugStep debugstep){
		final QueryResult qr = QueryResult.createInstance(this.getIterator());
		if (this.succeedingOperators.size() > 1) {
			qr.materialize();
		}
		for (final OperatorIDTuple opId : this.succeedingOperators) {
			final QueryResultDebug qrDebug = new QueryResultDebug(qr,
					debugstep, this, opId.getOperator(), true);
			((Operator) opId.getOperator()).processAllDebug(qrDebug, opId
					.getId(), debugstep);
		}
	}

	/** {@inheritDoc} */
	@Override
	public Message preProcessMessageDebug(
			final ComputeIntermediateResultMessage msg,
			final DebugStep debugstep) {
		this.computeResultDebug(debugstep);
		return msg;
	}

	/** {@inheritDoc} */
	@Override
	public Message preProcessMessageDebug(final EndOfEvaluationMessage msg,
			final DebugStep debugstep) {
		this.computeResultDebug(debugstep);
		return msg;
	}
}
