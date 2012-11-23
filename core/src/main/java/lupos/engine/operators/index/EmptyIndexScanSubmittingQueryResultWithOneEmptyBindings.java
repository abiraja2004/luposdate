/**
 * Copyright (c) 2012, Institute of Information Systems (Sven Groppe), University of Luebeck
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
package lupos.engine.operators.index;

import java.net.URISyntaxException;
import java.util.Collection;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.rdf.Prefix;

public class EmptyIndexScanSubmittingQueryResultWithOneEmptyBindings extends EmptyIndexScan {
	
	protected final Root root;
	protected final Item rdfGraph;

	/**
	 * 
	 */
	private static final long serialVersionUID = -6813056199050211285L;

	public EmptyIndexScanSubmittingQueryResultWithOneEmptyBindings(final OperatorIDTuple succeedingOperator,
			final Collection<TriplePattern> triplePattern, Item graphConstraint,
			final lupos.engine.operators.index.Root root_param) {
		super(succeedingOperator, triplePattern, root_param);
		this.root = root_param;
		this.rdfGraph = graphConstraint;
	}

	/**
	 * Creating a new query result with an empty binding to handle an empty BIND
	 * statement
	 * 
	 * @param Dataset
	 */
	@Override
	public QueryResult process(final Dataset dataset) {
		final QueryResult queryResult = QueryResult.createInstance();
		if(this.rdfGraph!=null && this.rdfGraph.isVariable()){
			Variable graphConstraint = (Variable) this.rdfGraph;
			if (this.root.namedGraphs != null && this.root.namedGraphs.size() > 0) {
				// Convert the named graphs' names into URILiterals
				// to be applicable later on
				for (final String name : this.root.namedGraphs) {
					final Bindings graphConstraintBindings = Bindings.createNewInstance();
					try {
						graphConstraintBindings.add(graphConstraint, LiteralFactory.createURILiteralWithoutLazyLiteral(name));
					} catch (URISyntaxException e) {
						System.err.println(e);
						e.printStackTrace();
					}
					queryResult.add(graphConstraintBindings);
				}
				} else {
					final Collection<Indices> dataSetIndices = dataset.getNamedGraphIndices();
					if (dataSetIndices != null) {
						for (final Indices indices : dataSetIndices) {
							final Bindings graphConstraintBindings = Bindings.createNewInstance();
							graphConstraintBindings.add(graphConstraint, indices.getRdfName());
							queryResult.add(graphConstraintBindings);
						}
					}
				}
		} else {
			queryResult.add(Bindings.createNewInstance());
		}

		for (final OperatorIDTuple succOperator : this.succeedingOperators) {

			((Operator) succOperator.getOperator()).processAll(queryResult,
					succOperator.getId());
		}
		return queryResult;
	}

	@Override
	public String toString() {
		return super.toString()+"\nReturning queryResult with one empty bindings";
	}

	@Override
	public String toString(Prefix prefix) {
		return super.toString(prefix)+"\nReturning queryResult with one empty bindings";
	}	
}