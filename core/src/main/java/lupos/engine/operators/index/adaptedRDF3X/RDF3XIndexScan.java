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
package lupos.engine.operators.index.adaptedRDF3X;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.TripleKey;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.paged_dbbptree.DBBPTree;
import lupos.datastructures.paged_dbbptree.OptimizedDBBPTreeGeneration;
import lupos.datastructures.paged_dbbptree.PrefixSearchMinMax;
import lupos.datastructures.queryresult.ParallelIterator;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.BasicIndexScan;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.index.Indices;
import lupos.engine.operators.multiinput.join.Join;
import lupos.engine.operators.multiinput.join.MergeJoinWithoutSorting;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.misc.Tuple;
import lupos.optimizations.logical.statistics.Entry;
import lupos.optimizations.logical.statistics.VarBucket;
import lupos.optimizations.physical.joinorder.costbasedoptimizer.operatorgraphgenerator.RDF3XOperatorGraphGenerator;
public class RDF3XIndexScan extends BasicIndexScan {

	public enum CollationOrder {
		SPO {
			@Override
			public int getSortCriterium(final int pos) {
				switch(pos){
					default:
					case 0:
						return 0;
					case 1:
						return 1;
					case 2:
						return 2;
				}
			}
		},
		SOP {
			@Override
			public int getSortCriterium(final int pos) {
				switch(pos){
					default:
					case 0:
						return 0;
					case 1:
						return 2;
					case 2:
						return 1;
				}
			}
		},
		PSO {
			@Override
			public int getSortCriterium(final int pos) {
				switch(pos){
					default:
					case 0:
						return 1;
					case 1:
						return 0;
					case 2:
						return 2;
				}
			}
		},
		POS {
			@Override
			public int getSortCriterium(final int pos) {
				switch(pos){
					default:
					case 0:
						return 1;
					case 1:
						return 2;
					case 2:
						return 0;
				}
			}
		},
		OSP {
			@Override
			public int getSortCriterium(final int pos) {
				switch(pos){
					default:
					case 0:
						return 2;
					case 1:
						return 0;
					case 2:
						return 1;
				}
			}
		},
		OPS {
			@Override
			public int getSortCriterium(final int pos) {
				switch(pos){
					default:
					case 0:
						return 2;
					case 1:
						return 1;
					case 2:
						return 0;
				}
			}
		};

		public abstract int getSortCriterium(int pos);

		public static CollationOrder createCollationOrder(final int primaryCriterium, final int secondaryCriterium){
			if(primaryCriterium==1) {
				if(secondaryCriterium==0){
					return PSO;
				} else {
					return POS;
				}
			} else if(primaryCriterium==2) {
				if(secondaryCriterium==0){
					return OSP;
				} else {
					return OPS;
				}

			} else {
				if(secondaryCriterium==1){
					return SPO;
				} else {
					return SOP;
				}
			}
		}

		public static CollationOrder getCollationOrder(final TriplePattern tp, final Collection<Variable> orderVars) {
			int[] collationOrder1 = { -1, -1, -1 };
			int i1 = 0;
			for (int i = 0; i < 3; i++) {
				if (!tp.getPos(i).isVariable()) {
					collationOrder1[i1] = i;
					i1++;
				}
			}
			for (final Variable v : orderVars) {
				final int pos = tp.getPos(v);
				if (pos >= 0) {
					collationOrder1[i1] = pos;
					i1++;
				}
			}
			collationOrder1 = fill(collationOrder1, i1);
			return getCollationOrder(collationOrder1);
		}

		protected static int[] fill(final int[] collationOrder, int i) {
			for (; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					int i2 = 0;
					for (; i2 < i; i2++) {
						if (j == collationOrder[i2]) {
							break;
						}
					}
					if (i2 == i) {
						collationOrder[i] = j;
						break;
					}
				}
			}
			return collationOrder;
		}

		protected static CollationOrder getCollationOrder(
				final int[] collationOrderArray) {
			if (collationOrderArray[0] == 0) {
				if (collationOrderArray[1] == 1) {
					return CollationOrder.SPO;
				} else {
					return CollationOrder.SOP;
				}
			} else if (collationOrderArray[0] == 1) {
				if (collationOrderArray[1] == 0) {
					return CollationOrder.PSO;
				} else {
					return CollationOrder.POS;
				}
			} else {
				if (collationOrderArray[1] == 0) {
					return CollationOrder.OSP;
				} else {
					return CollationOrder.OPS;
				}
			}
		}
	}

	protected CollationOrder collationOrder = CollationOrder.SPO;

	protected Map<Variable, Literal> minima;
	protected Map<Variable, Literal> maxima;

	/**
	 *
	 */
	private static final long serialVersionUID = -2346474799334082208L;

	/**
	 * <p>setMinimaMaxima.</p>
	 *
	 * @param minima a {@link java.util.Map} object.
	 * @param maxima a {@link java.util.Map} object.
	 */
	public void setMinimaMaxima(final Map<Variable, Literal> minima,
			final Map<Variable, Literal> maxima) {
		this.minima = minima;
		this.maxima = maxima;
	}

	/**
	 * <p>Getter for the field <code>minima</code>.</p>
	 *
	 * @return a {@link java.util.Map} object.
	 */
	public Map<Variable, Literal> getMinima() {
		return this.minima;
	}

	/**
	 * <p>Getter for the field <code>maxima</code>.</p>
	 *
	 * @return a {@link java.util.Map} object.
	 */
	public Map<Variable, Literal> getMaxima() {
		return this.maxima;
	}

	/** {@inheritDoc} */
	@Override
	public RDF3XIndexScan clone() {
		final RDF3XIndexScan clone = new RDF3XIndexScan(this.succeedingOperators, this.triplePatterns, this.rdfGraph, this.root);
		clone.collationOrder = this.collationOrder;
		clone.bindingsFactory = this.bindingsFactory;
		return clone;
	}

	/**
	 * <p>Constructor for RDF3XIndexScan.</p>
	 *
	 * @param succeedingOperator a {@link lupos.engine.operators.OperatorIDTuple} object.
	 * @param triplePatterns a {@link java.util.Collection} object.
	 * @param rdfGraph a {@link lupos.datastructures.items.Item} object.
	 * @param root a lupos$engine$operators$index$Root object.
	 */
	public RDF3XIndexScan(final OperatorIDTuple succeedingOperator,
			final Collection<TriplePattern> triplePatterns, final Item rdfGraph, final lupos.engine.operators.index.Root root) {
		super(succeedingOperator, triplePatterns, rdfGraph, root);
	}

	/**
	 * <p>Constructor for RDF3XIndexScan.</p>
	 *
	 * @param succeedingOperators a {@link java.util.List} object.
	 * @param triplePatterns a {@link java.util.Collection} object.
	 * @param rdfGraph a {@link lupos.datastructures.items.Item} object.
	 * @param root a lupos$engine$operators$index$Root object.
	 */
	public RDF3XIndexScan(final List<OperatorIDTuple> succeedingOperators,
			final Collection<TriplePattern> triplePatterns, final Item rdfGraph,final lupos.engine.operators.index.Root root) {
		super(succeedingOperators, triplePatterns, rdfGraph, root);
	}

	/**
	 * <p>Constructor for RDF3XIndexScan.</p>
	 *
	 * @param operatorIDTuple a {@link lupos.engine.operators.OperatorIDTuple} object.
	 * @param triplePatterns a {@link java.util.Collection} object.
	 * @param graphConstraint a {@link lupos.datastructures.items.Item} object.
	 * @param minima a {@link java.util.Map} object.
	 * @param maxima a {@link java.util.Map} object.
	 * @param root a lupos$engine$operators$index$Root object.
	 */
	public RDF3XIndexScan(final OperatorIDTuple operatorIDTuple,
			final Collection<TriplePattern> triplePatterns,
			final Item graphConstraint, final Map<Variable, Literal> minima,
			final Map<Variable, Literal> maxima, final lupos.engine.operators.index.Root root) {
		this(operatorIDTuple, triplePatterns, graphConstraint, root);
		this.minima = minima;
		this.maxima = maxima;
	}

	/**
	 * <p>Constructor for RDF3XIndexScan.</p>
	 *
	 * @param root a lupos$engine$operators$index$Root object.
	 * @param triplePatterns a {@link java.util.Collection} object.
	 */
	public RDF3XIndexScan(final lupos.engine.operators.index.Root root,
			final Collection<TriplePattern> triplePatterns) {
		super(root, triplePatterns);
	}

	/** {@inheritDoc} */
	@Override
	public QueryResult join(final Dataset dataset) {
		try {

			// get the graph constraint from the super class.
			// If it is null, a default graph is used, if not null a named one
			// is used
			final Item graphConstraintItem = this.getGraphConstraint();

			// get a collection of indices using the determined graph constraint
			final Collection<Indices> indicesC = dataset.indexingRDFGraphs(
					graphConstraintItem, false, false, this.root);
			if ((indicesC != null) && !(indicesC.isEmpty())) {
				if (graphConstraintItem == null) {
					if (indicesC != null && indicesC.size() > 1) {
						// deal with special case: several default graphs!
						if (this.triplePatterns.size() != 1) {
							System.err.println("Can only process one triple pattern!");
						}
						final TriplePattern tp = this.triplePatterns.iterator()
								.next();
						final Triple key = getKey(tp, null);
						final Triple keyMinimum = this.getKeyMinimum(tp, null);
						final Triple keyMaximum = this.getKeyMaximum(tp, null);

						final IndicesTripleIterator[] ita = new IndicesTripleIterator[indicesC
								.size()];
						int id = 0;
						for (final Indices indices : indicesC) {
							ita[id] = new IndicesTripleIterator(this.getIterator(
									(SixIndices) indices, key, keyMinimum,
									keyMaximum), id);
							id++;
						}

						final MergeIndicesTripleIterator it = new MergeIndicesTripleIterator(ita, this.collationOrder);

						if (!it.hasNext()) {
							return null;
						}

						return QueryResult.createInstance(it, tp);
					}
				}
			}
		} catch (final Exception e) {
			System.err.println("Error while joining triple patterns: "+ e);
			return null;
		}
		// in all other case call the method of the super class, which will
		// finally call join(final Indices indices, final Bindings bindings)!
		return super.join(dataset);
	}

	/**
	 * <p>getKey.</p>
	 *
	 * @param tp a {@link lupos.engine.operators.tripleoperator.TriplePattern} object.
	 * @param bindings a {@link lupos.datastructures.bindings.Bindings} object.
	 * @return a {@link lupos.datastructures.items.Triple} object.
	 */
	protected static Triple getKey(final TriplePattern tp,
			final Bindings bindings) {
		final Item[] items = tp.getItems();
		final Triple key = new Triple();
		for (int i = 0; i < 3; i++) {
			if (items[i].isVariable()) {
				if (bindings != null
						&& bindings.getVariableSet().contains(items[i])) {
					key.setPos(i, bindings.get((Variable) items[i]));
				} else {
					key.setPos(i, null);
				}
			} else {
				key.setPos(i, (Literal) items[i]);
			}
		}
		return key;
	}

	/**
	 * <p>getKeyMinimum.</p>
	 *
	 * @param tp a {@link lupos.engine.operators.tripleoperator.TriplePattern} object.
	 * @param bindings a {@link lupos.datastructures.bindings.Bindings} object.
	 * @return a {@link lupos.datastructures.items.Triple} object.
	 */
	protected Triple getKeyMinimum(final TriplePattern tp,
			final Bindings bindings) {
		return this.getKey(tp, bindings, this.minima);
	}

	/**
	 * <p>getKeyMaximum.</p>
	 *
	 * @param tp a {@link lupos.engine.operators.tripleoperator.TriplePattern} object.
	 * @param bindings a {@link lupos.datastructures.bindings.Bindings} object.
	 * @return a {@link lupos.datastructures.items.Triple} object.
	 */
	protected Triple getKeyMaximum(final TriplePattern tp,
			final Bindings bindings) {
		return this.getKey(tp, bindings, this.maxima);
	}

	/**
	 * <p>getKey.</p>
	 *
	 * @param tp a {@link lupos.engine.operators.tripleoperator.TriplePattern} object.
	 * @param bindings a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param minMax a {@link java.util.Map} object.
	 * @return a {@link lupos.datastructures.items.Triple} object.
	 */
	protected Triple getKey(final TriplePattern tp, final Bindings bindings,
			final Map<Variable, Literal> minMax) {
		boolean flag = false;
		final Item[] items = tp.getItems();
		final Triple key = new Triple();
		for (int i = 0; i < 3; i++) {
			if (items[i].isVariable()) {
				if (bindings != null
						&& bindings.getVariableSet().contains(items[i])) {
					key.setPos(i, bindings.get((Variable) items[i]));
				} else {
					final Literal l = (minMax == null) ? null : minMax
							.get(items[i]);
					key.setPos(i, l);
					if (l != null) {
						flag = true;
					}
				}
			} else {
				key.setPos(i, (Literal) items[i]);
			}
		}
		return flag ? key : null;
	}

	/**
	 * <p>getIterator.</p>
	 *
	 * @param sixIndices a {@link lupos.engine.operators.index.adaptedRDF3X.SixIndices} object.
	 * @param key a {@link lupos.datastructures.items.Triple} object.
	 * @param keyMinimum a {@link lupos.datastructures.items.Triple} object.
	 * @param keyMaximum a {@link lupos.datastructures.items.Triple} object.
	 * @return a {@link java.util.Iterator} object.
	 */
	protected Iterator<Triple> getIterator(final SixIndices sixIndices,
			final Triple key, final Triple keyMinimum, final Triple keyMaximum) {
		return RDF3XIndexScan.getIterator(sixIndices, key, this.collationOrder,
				keyMinimum, keyMaximum);
	}

	/**
	 * <p>getIterator.</p>
	 *
	 * @param sixIndices a {@link lupos.engine.operators.index.adaptedRDF3X.SixIndices} object.
	 * @param key a {@link lupos.datastructures.items.Triple} object.
	 * @param collationOrder a {@link lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan.CollationOrder} object.
	 * @param keyMinimum a {@link lupos.datastructures.items.Triple} object.
	 * @param keyMaximum a {@link lupos.datastructures.items.Triple} object.
	 * @return a {@link java.util.Iterator} object.
	 */
	protected static Iterator<Triple> getIterator(final SixIndices sixIndices,
			final Triple key, final CollationOrder collationOrder,
			final Triple keyMinimum, final Triple keyMaximum) {
		if (keyMinimum == null) {
			if (keyMaximum == null) {
				switch (collationOrder) {
				case SPO:
					return sixIndices.SPO.prefixSearch(new TripleKey(key,
							CollationOrder.SPO));
				case SOP:
					return sixIndices.SOP.prefixSearch(new TripleKey(key,
							CollationOrder.SOP));
				case PSO:
					return sixIndices.PSO.prefixSearch(new TripleKey(key,
							CollationOrder.PSO));
				case POS:
					return sixIndices.POS.prefixSearch(new TripleKey(key,
							CollationOrder.POS));
				case OSP:
					return sixIndices.OSP.prefixSearch(new TripleKey(key,
							CollationOrder.OSP));
				default:
				case OPS:
					return sixIndices.OPS.prefixSearch(new TripleKey(key,
							CollationOrder.OPS));
				}
			} else {
				switch (collationOrder) {
				case SPO:
					return sixIndices.SPO.prefixSearchMax(new TripleKey(key,
							CollationOrder.SPO),
							new TripleKey(keyMaximum,
									CollationOrder.SPO));
				case SOP:
					return sixIndices.SOP.prefixSearchMax(new TripleKey(key,
							CollationOrder.SOP),
							new TripleKey(keyMaximum,
									CollationOrder.SOP));
				case PSO:
					return sixIndices.PSO.prefixSearchMax(new TripleKey(key,
							CollationOrder.PSO),
							new TripleKey(keyMaximum,
									CollationOrder.PSO));
				case POS:
					return sixIndices.POS.prefixSearchMax(new TripleKey(key,
							CollationOrder.POS),
							new TripleKey(keyMaximum,
									CollationOrder.POS));
				case OSP:
					return sixIndices.OSP.prefixSearchMax(new TripleKey(key,
							CollationOrder.OSP),
							new TripleKey(keyMaximum,
									CollationOrder.OSP));
				default:
				case OPS:
					return sixIndices.OPS.prefixSearchMax(new TripleKey(key,
							CollationOrder.OPS),
							new TripleKey(keyMaximum,
									CollationOrder.OPS));
				}
			}
		} else {
			if (keyMaximum == null) {
				switch (collationOrder) {
				case SPO:
					return sixIndices.SPO.prefixSearch(new TripleKey(key,
							CollationOrder.SPO),
							new TripleKey(keyMinimum,
									CollationOrder.SPO));
				case SOP:
					return sixIndices.SOP.prefixSearch(new TripleKey(key,
							CollationOrder.SOP),
							new TripleKey(keyMinimum,
									CollationOrder.SOP));
				case PSO:
					return sixIndices.PSO.prefixSearch(new TripleKey(key,
							CollationOrder.PSO),
							new TripleKey(keyMinimum,
									CollationOrder.PSO));
				case POS:
					return sixIndices.POS.prefixSearch(new TripleKey(key,
							CollationOrder.POS),
							new TripleKey(keyMinimum,
									CollationOrder.POS));
				case OSP:
					return sixIndices.OSP.prefixSearch(new TripleKey(key,
							CollationOrder.OSP),
							new TripleKey(keyMinimum,
									CollationOrder.OSP));
				default:
				case OPS:
					return sixIndices.OPS.prefixSearch(new TripleKey(key,
							CollationOrder.OPS),
							new TripleKey(keyMinimum,
									CollationOrder.OPS));
				}
			} else {
				switch (collationOrder) {
				case SPO:
					return sixIndices.SPO.prefixSearch(new TripleKey(key,
							CollationOrder.SPO),
							new TripleKey(keyMinimum,
									CollationOrder.SPO),
							new TripleKey(keyMaximum,
									CollationOrder.SPO));
				case SOP:
					return sixIndices.SOP.prefixSearch(new TripleKey(key,
							CollationOrder.SOP),
							new TripleKey(keyMinimum,
									CollationOrder.SOP),
							new TripleKey(keyMaximum,
									CollationOrder.SOP));
				case PSO:
					return sixIndices.PSO.prefixSearch(new TripleKey(key,
							CollationOrder.PSO),
							new TripleKey(keyMinimum,
									CollationOrder.PSO),
							new TripleKey(keyMaximum,
									CollationOrder.PSO));
				case POS:
					return sixIndices.POS.prefixSearch(new TripleKey(key,
							CollationOrder.POS),
							new TripleKey(keyMinimum,
									CollationOrder.POS),
							new TripleKey(keyMaximum,
									CollationOrder.POS));
				case OSP:
					return sixIndices.OSP.prefixSearch(new TripleKey(key,
							CollationOrder.OSP),
							new TripleKey(keyMinimum,
									CollationOrder.OSP),
							new TripleKey(keyMaximum,
									CollationOrder.OSP));
				default:
				case OPS:
					return sixIndices.OPS.prefixSearch(new TripleKey(key,
							CollationOrder.OPS),
							new TripleKey(keyMinimum,
									CollationOrder.OPS),
							new TripleKey(keyMaximum,
									CollationOrder.OPS));
				}
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public QueryResult join(final Indices indices, final Bindings bindings) {
		final SixIndices sixIndices = (SixIndices) indices;
		if (this.triplePatterns.size() != 1) {
			System.err.println("Can only process one triple pattern!");
		}
		final TriplePattern tp = this.triplePatterns.iterator().next();
		final Triple key = getKey(tp, bindings);
		final Triple keyMinimum = this.getKeyMinimum(tp, bindings);
		final Triple keyMaximum = this.getKeyMaximum(tp, bindings);
		final Iterator<Triple> it = this.getIterator(sixIndices, key, keyMinimum,
				keyMaximum);

		if (!it.hasNext()) {
			return null;
		}

		return QueryResult.createInstance(it, tp, this.collationOrder, tp
				.getBloomFilters() != null);
	}

	/**
	 * <p>Getter for the field <code>collationOrder</code>.</p>
	 *
	 * @param first a {@link lupos.engine.operators.tripleoperator.TriplePattern} object.
	 * @param sortCriterium a {@link java.util.Collection} object.
	 * @return a {@link lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan.CollationOrder} object.
	 */
	public static CollationOrder getCollationOrder(final TriplePattern first,
			final Collection<Variable> sortCriterium) {

		// all these elements have to occur in the computed order
		final String[] teElements = { "S", "P", "O" };

		String orderFirst = "";
		for (int i = 0; i < 3; i++) {
			if (!first.getItems()[i].isVariable()) {
				orderFirst += teElements[i];
			}
		}

		// get the positions of these items
		if (sortCriterium != null) {
			for (final Variable variables : sortCriterium) {
				for (int i = 0; i < 3; i++) {
					if (first.getItems()[i].equals(variables)) {
						orderFirst += teElements[i];
					}
				}
			}
		}

		// fill up the items to receive valid order patterns (which have three
		// digits)
		for (int i = 0; i < teElements.length; i++) {
			if (orderFirst.indexOf(teElements[i]) == -1) {
				orderFirst += teElements[i];
			}
		}

		// convert the strings to valid order pattern objects
		return CollationOrder.valueOf(orderFirst.toString());
	}

	/**
	 * <p>Getter for the field <code>collationOrder</code>.</p>
	 *
	 * @return a {@link lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan.CollationOrder} object.
	 */
	public CollationOrder getCollationOrder() {
		return this.collationOrder;
	}

	/**
	 * <p>Setter for the field <code>collationOrder</code>.</p>
	 *
	 * @param collationOrder a {@link lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan.CollationOrder} object.
	 */
	public void setCollationOrder(final CollationOrder collationOrder) {
		this.collationOrder = collationOrder;
	}

	/**
	 * <p>Setter for the field <code>collationOrder</code>.</p>
	 *
	 * @param sortCriterium a {@link java.util.Collection} object.
	 */
	public void setCollationOrder(final Collection<Variable> sortCriterium) {
		this.collationOrder = getCollationOrder(this.triplePatterns.iterator().next(), sortCriterium);
	}

	/**
	 * <p>getBinaryJoin.</p>
	 *
	 * @return a {@link lupos.engine.operators.index.adaptedRDF3X.RDF3XRoot} object.
	 */
	public RDF3XRoot getBinaryJoin() {
		final RDF3XRoot ic = new RDF3XRoot();
		if (this.triplePatterns.size() <= 1) {
			int[] collationOrder1 = { -1, -1, -1 };
			int i1 = 0;
			for (int i = 0; i < 3; i++) {
				if (!this.getTriplePattern().iterator().next().getPos(i)
						.isVariable()) {
					collationOrder1[i1] = i;
					i1++;
				}
			}
			for (final Variable v : this.unionVariables) {
				collationOrder1[i1] = this.getTriplePattern().iterator().next()
						.getPos(v);
				i1++;
			}
			collationOrder1 = this.fill(collationOrder1, i1);
			final CollationOrder co1 = getCollationOrder(collationOrder1);
			this.setCollationOrder(co1);
			ic.setSucceedingOperator(new OperatorIDTuple(this, 0));
			return ic;
		}
		this.optimizeJoinOrderAccordingToMostRestrictionsForMergeJoin();
		final Collection<BasicOperator> remainingJoins = new LinkedList<BasicOperator>();
		final Iterator<TriplePattern> itp = this.triplePatterns.iterator();
		while (itp.hasNext()) {
			final Collection<TriplePattern> c1 = new LinkedList<TriplePattern>();
			c1.add(itp.next());
			final RDF3XIndexScan index1 = new RDF3XIndexScan((OperatorIDTuple) null,
					c1, this.getGraphConstraint(), this.root);
			index1.intersectionVariables = new HashSet<Variable>();
			index1.unionVariables = new HashSet<Variable>();
			for (final TriplePattern tp : c1) {
				for (final Item i : tp.getItems()) {
					if (i.isVariable()) {
						index1.intersectionVariables.add((Variable) i);
						index1.unionVariables.add((Variable) i);
					}
				}
			}
			if (itp.hasNext()) {
				final Collection<TriplePattern> c2 = new LinkedList<TriplePattern>();
				c2.add(itp.next());
				final RDF3XIndexScan index2 = new RDF3XIndexScan(
						(OperatorIDTuple) null, c2, this.getGraphConstraint(), this.root);
				index2.intersectionVariables = new HashSet<Variable>();
				index2.unionVariables = new HashSet<Variable>();
				for (final TriplePattern tp : c2) {
					for (final Item i : tp.getItems()) {
						if (i.isVariable()) {
							index2.intersectionVariables.add((Variable) i);
							index2.unionVariables.add((Variable) i);
						}
					}
				}
				final HashSet<Variable> hsv = new HashSet<Variable>();
				hsv.addAll(index1.unionVariables);
				hsv.retainAll(index2.unionVariables);
				int[] collationOrder1 = { -1, -1, -1 };
				int[] collationOrder2 = { -1, -1, -1 };
				int i1 = 0;
				int i2 = 0;
				for (int i = 0; i < 3; i++) {
					if (!index1.getTriplePattern().iterator().next().getPos(i)
							.isVariable()) {
						collationOrder1[i1] = i;
						i1++;
					}
				}
				for (int i = 0; i < 3; i++) {
					if (!index2.getTriplePattern().iterator().next().getPos(i)
							.isVariable()) {
						collationOrder2[i2] = i;
						i2++;
					}
				}
				for (final Variable v : hsv) {
					collationOrder1[i1] = index1.getTriplePattern().iterator()
							.next().getPos(v);
					collationOrder2[i2] = index2.getTriplePattern().iterator()
							.next().getPos(v);
					i1++;
					i2++;
				}
				collationOrder1 = this.fill(collationOrder1, i1);
				collationOrder2 = this.fill(collationOrder2, i2);
				final CollationOrder co1 = getCollationOrder(collationOrder1);
				final CollationOrder co2 = getCollationOrder(collationOrder2);
				index1.setCollationOrder(co1);
				index2.setCollationOrder(co2);
				final Join join = new MergeJoinWithoutSorting();
				join.setIntersectionVariables(hsv);
				join.setUnionVariables(new HashSet<Variable>());
				join.getUnionVariables().addAll(index1.getUnionVariables());
				join.getUnionVariables().addAll(index2.getUnionVariables());
				index1.setSucceedingOperator(new OperatorIDTuple(join, 0));
				index2.setSucceedingOperator(new OperatorIDTuple(join, 1));
				ic.addSucceedingOperator(new OperatorIDTuple(index1, 0));
				ic.addSucceedingOperator(new OperatorIDTuple(index2, 0));
				remainingJoins.add(join);
			} else {
				int[] collationOrder1 = { -1, -1, -1 };
				int i1 = 0;
				for (int i = 0; i < 3; i++) {
					if (!index1.getTriplePattern().iterator().next().getPos(i)
							.isVariable()) {
						collationOrder1[i1] = i;
						i1++;
					}
				}
				for (final Variable v : index1.unionVariables) {
					collationOrder1[i1] = index1.getTriplePattern().iterator()
							.next().getPos(v);
					i1++;
				}
				collationOrder1 = this.fill(collationOrder1, i1);
				final CollationOrder co1 = getCollationOrder(collationOrder1);
				index1.setCollationOrder(co1);
				ic.addSucceedingOperator(new OperatorIDTuple(index1, 0));
				remainingJoins.add(index1);
			}
		}
		while (remainingJoins.size() > 1) {
			// choose best combination
			final Collection<BasicOperator> co = this.getNextJoin(remainingJoins);
			final Iterator<BasicOperator> io = co.iterator();
			final BasicOperator first = io.next();
			final BasicOperator second = io.next();
			final Join join = new Join();
			join.setIntersectionVariables(new HashSet<Variable>());
			join.setUnionVariables(new HashSet<Variable>());
			join.getUnionVariables().addAll(first.getUnionVariables());
			join.getUnionVariables().addAll(second.getUnionVariables());
			first.setSucceedingOperator(new OperatorIDTuple(join, 0));
			second.setSucceedingOperator(new OperatorIDTuple(join, 1));
			remainingJoins.remove(first);
			remainingJoins.remove(second);
			remainingJoins.add(join);
		}
		remainingJoins.iterator().next().setSucceedingOperators(
				this.succeedingOperators);
		return ic;
	}

	/**
	 * <p>fill.</p>
	 *
	 * @param collationOrder an array of int.
	 * @param i a int.
	 * @return an array of int.
	 */
	protected int[] fill(final int[] collationOrder, int i) {
		for (; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				int i2 = 0;
				for (; i2 < i; i2++) {
					if (j == collationOrder[i2]) {
						break;
					}
				}
				if (i2 == i) {
					collationOrder[i] = j;
					break;
				}
			}
		}
		return collationOrder;
	}

	/**
	 * <p>Getter for the field <code>collationOrder</code>.</p>
	 *
	 * @param collationOrderArray an array of int.
	 * @return a {@link lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan.CollationOrder} object.
	 */
	protected static CollationOrder getCollationOrder(
			final int[] collationOrderArray) {
		if (collationOrderArray[0] == 0) {
			if (collationOrderArray[1] == 1) {
				return CollationOrder.SPO;
			} else {
				return CollationOrder.SOP;
			}
		} else if (collationOrderArray[0] == 1) {
			if (collationOrderArray[1] == 0) {
				return CollationOrder.PSO;
			} else {
				return CollationOrder.POS;
			}
		} else {
			if (collationOrderArray[1] == 0) {
				return CollationOrder.OSP;
			} else {
				return CollationOrder.OPS;
			}
		}
	}

	private Collection<BasicOperator> getNextJoin(
			final Collection<BasicOperator> remainingJoins) {
		final Collection<BasicOperator> co = new LinkedList<BasicOperator>();
		BasicOperator best1 = null;
		BasicOperator best2 = null;
		int minCommonVariables = -1;
		for (final BasicOperator o1 : remainingJoins) {
			for (final BasicOperator o2 : remainingJoins) {
				if (!o1.equals(o2)) {
					final Collection<Variable> v = o1.getUnionVariables();
					v.retainAll(o2.getUnionVariables());
					final int commonVariables = v.size();
					if (commonVariables > minCommonVariables) {
						minCommonVariables = commonVariables;
						best1 = o1;
						best2 = o2;
					}
				}
			}
		}
		co.add(best1);
		co.add(best2);
		return co;
	}

	private void optimizeJoinOrderAccordingToMostRestrictionsForMergeJoin() {
		final Collection<TriplePattern> remainingTP = new LinkedList<TriplePattern>();
		remainingTP.addAll(this.triplePatterns);
		final Collection<TriplePattern> newTriplePattern = new LinkedList<TriplePattern>();
		while (remainingTP.size() > 1) {
			TriplePattern best1 = null;
			TriplePattern best2 = null;
			int minOpenPositions = 4;
			for (final TriplePattern tp1 : remainingTP) {
				for (final TriplePattern tp2 : remainingTP) {
					if (!tp1.equals(tp2)) {
						final HashSet<String> v = tp1.getVariableNames();
						v.retainAll(tp2.getVariableNames());
						final int openPositions = 3 - v.size();
						if (openPositions < minOpenPositions) {
							minOpenPositions = openPositions;
							best1 = tp1;
							best2 = tp2;
						}

					}
				}
			}
			newTriplePattern.add(best1);
			newTriplePattern.add(best2);
			remainingTP.remove(best1);
			remainingTP.remove(best2);
		}
		if (remainingTP.size() == 1) {
			for (final TriplePattern tp1 : remainingTP) {
				newTriplePattern.add(tp1);
			}
		}
		this.triplePatterns = newTriplePattern;
	}

	/** {@inheritDoc} */
	@Override
	public Map<Variable, Tuple<Literal, Literal>> getMinMax(final TriplePattern triplePattern, final Collection<Variable> variables) {
		final Map<Variable, Tuple<Literal, Literal>> result = new HashMap<Variable, Tuple<Literal, Literal>>();
		for(final Variable v: variables){
			int pos = 0;
			for (final Item item : triplePattern.getItems()) {
				if (v.equals(item)) {
					break;
				}
				pos++;
			}
			final Literal min = this.getMin(v, triplePattern);
			if (min != null){
				// collation order is already set by getMin(...) method!
				result.put(v, new Tuple<Literal, Literal>(min, this.getMax(triplePattern, pos)));
			}
		}
		if(!result.isEmpty()){
			return result;
		}
		return null;
	}

	/**
	 * <p>getMin.</p>
	 *
	 * @param v a {@link lupos.datastructures.items.Variable} object.
	 * @param tp a {@link lupos.engine.operators.tripleoperator.TriplePattern} object.
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 */
	public Literal getMin(final Variable v,
			final TriplePattern tp) {
		final Collection<TriplePattern> ztp = this.getTriplePattern();
		final Collection<TriplePattern> ctp = new LinkedList<TriplePattern>();
		ctp.add(tp);
		this.setTriplePatterns(ctp);
		final Collection<Variable> cv = new LinkedList<Variable>();
		cv.add(v);
		this.setCollationOrder(RDF3XOperatorGraphGenerator.getCollationOrder(tp, cv));
		final QueryResult qr = this.join(this.root.dataset);
		if (qr == null) {
			this.setTriplePatterns(ztp);
			return null;
		}
		final Iterator<Bindings> itb = qr.oneTimeIterator();
		if (!itb.hasNext()) {
			this.setTriplePatterns(ztp);
			return null;
		}
		final Literal min = itb.next().get(v);
		if (itb instanceof ParallelIterator) {
			((ParallelIterator<Bindings>) itb).close();
		}

		this.setTriplePatterns(ztp);
		return min;
	}

	/**
	 * <p>getMax.</p>
	 *
	 * @param tp a {@link lupos.engine.operators.tripleoperator.TriplePattern} object.
	 * @param pos a int.
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 */
	public Literal getMax(final TriplePattern tp, final int pos) {
		Literal max = null;
		try {

			// get the graph constraint from the super class.
			// If it is null, a default graph is used, if not null a named one
			// is used
			final Item graphConstraintItem = this.getGraphConstraint();

			// get a collection of indices using the determined graph constraint
			final Collection<Indices> indicesC = this.root.dataset.indexingRDFGraphs(
					graphConstraintItem, false, false, this.root);
			if ((indicesC != null) && !(indicesC.isEmpty())) {
				final Triple key = getKey(tp, null);
				final Collection<URILiteral> namedGraphs = new ArrayList<URILiteral>();

				// if the graph constraint is not null (which means that a named
				// graph is used)
				if (graphConstraintItem != null) {

					if (graphConstraintItem instanceof Variable) {

						final Variable graphConstraint = (Variable) graphConstraintItem;

						// check if named graphs were provided at query time
						if (this.root.namedGraphs != null
								&& !this.root.namedGraphs.isEmpty()) {

							// Convert the named graphs' names into URILiterals
							// to be applicable
							// later on
							for (final String name : this.root.namedGraphs) {

								final Indices indices = this.root.dataset
										.getNamedGraphIndices(LiteralFactory
												.createURILiteralWithoutLazyLiteral(name));

								final URILiteral rdfName = indices.getRdfName();
								if (namedGraphs.contains(rdfName)) {
									final TriplePattern ztp = new TriplePattern(
											graphConstraint
													.equals(tp.getPos(0)) ? rdfName
													: tp.getPos(0),
											graphConstraint
													.equals(tp.getPos(1)) ? rdfName
													: tp.getPos(1),
											graphConstraint
													.equals(tp.getPos(2)) ? rdfName
													: tp.getPos(2));
									final Triple zkey = getKey(ztp, null);
									final Literal intermediateMax = this.getMaxLiteral(
											(SixIndices) indices, zkey, pos);
									if (intermediateMax != null
											&& (max == null || max
													.compareToNotNecessarilySPARQLSpecificationConform(intermediateMax) < 0)) {
										max = intermediateMax;
									}
								}

							}

						}

						// otherwise there might have been named graphs added
						// during the evaluation
						else {

							// get all indices of named graphs and bind them to
							// the graph constraint
							final Collection<Indices> dataSetIndices = this.root.dataset
									.getNamedGraphIndices();
							if (dataSetIndices != null) {

								for (final Indices indices : dataSetIndices) {
									final TriplePattern ztp = new TriplePattern(
											graphConstraint
													.equals(tp.getPos(0)) ? indices
													.getRdfName()
													: tp.getPos(0),
											graphConstraint
													.equals(tp.getPos(1)) ? indices
													.getRdfName()
													: tp.getPos(1),
											graphConstraint
													.equals(tp.getPos(2)) ? indices
													.getRdfName()
													: tp.getPos(2));
									final Triple zkey = getKey(ztp, null);
									final Literal intermediateMax = this.getMaxLiteral(
											(SixIndices) indices, zkey, pos);
									if (intermediateMax != null
											&& (max == null || max
													.compareToNotNecessarilySPARQLSpecificationConform(intermediateMax) < 0)) {
										max = intermediateMax;
									}
								}
							}
						}
					}

					// if the graph constraint is an URILiteral fetch the
					// matching indices object
					// but do not bind anything
					else {
						for (final Indices indices : indicesC) {

							final URILiteral rdfName = indices.getRdfName();
							if (namedGraphs.contains(rdfName)) {
								final Literal intermediateMax = this.getMaxLiteral(
										(SixIndices) indices, key, pos);
								if (intermediateMax != null
										&& (max == null || max
												.compareToNotNecessarilySPARQLSpecificationConform(intermediateMax) < 0)) {
									max = intermediateMax;
								}
							}

						}
					}
				} else {
					if (indicesC != null) {
						// deal with special case: several default graphs!
						// if (triplePatterns.size() != 1) {
						// log.error("Can only process one triple pattern!");
						// }

						for (final Indices indices : indicesC) {
							final Literal intermediateMax = this.getMaxLiteral(
									(SixIndices) indices, key, pos);
							if (intermediateMax != null
									&& (max == null || max
											.compareToNotNecessarilySPARQLSpecificationConform(intermediateMax) < 0)) {
								max = intermediateMax;
							}
						}

						return max;
					}
				}
			}
		} catch (final Exception e) {
			System.err.println("Error while joining triple patterns: "+ e);
			return null;
		}
		// in all other case call the method of the super class, which will
		// finally call join(final Indices indices, final Bindings bindings)!
		return max;
	}

	private Literal getMaxLiteral(final SixIndices sixIndices,
			final Triple key, final int pos) {
		DBBPTree<TripleKey, Triple> dbbptree = null;
		final PrefixSearchMinMax<TripleKey, Triple> index = sixIndices.getIndex(this.collationOrder);
		if(index instanceof OptimizedDBBPTreeGeneration){
			dbbptree = ((OptimizedDBBPTreeGeneration<TripleKey, Triple>) index).getDBBPTree();
		} else if(index instanceof DBBPTree){
			dbbptree = (DBBPTree<TripleKey, Triple>) index;
		}
		if(dbbptree!=null){
			return dbbptree.getMaximum(new TripleKey(key, this.collationOrder)).getPos(pos);
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Map<Variable, VarBucket> getVarBuckets(final TriplePattern tp,
			final Class<? extends Bindings> classBindings,
			final Collection<Variable> joinPartners,
			final Map<Variable, Literal> minima,
			final Map<Variable, Literal> maxima) {
		if (Indices.usedDatastructure != Indices.DATA_STRUCT.DBBPTREE
				|| (LiteralFactory.getMapType() != LiteralFactory.MapType.LAZYLITERAL && LiteralFactory
						.getMapType() != LiteralFactory.MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP)) {
			return super.getVarBuckets(tp, classBindings,
					joinPartners, minima, maxima);
		}
		// use B+-tree with statistics about number of triples in subtree
		// and distinct literals at the subject, predicate or object position!
		final HashSet<Variable> joinPartnersTP = new HashSet<Variable>();
		joinPartnersTP.addAll(joinPartners);
		joinPartnersTP.retainAll(tp.getVariables());
		Map<Variable, VarBucket> intermediate = null;
		String keyHistogram = null;
		if (Indices.usedDatastructure == Indices.DATA_STRUCT.DBBPTREE) {
			keyHistogram = getKey(tp);
			// System.out.println(key);
			final VarBucket[] vba = histograms.get(keyHistogram);
			if (vba != null) {
				intermediate = getVarBuckets(tp, vba);
				if (intermediate.keySet().containsAll(joinPartnersTP)) {
					boolean flag = true;
					for (final Variable v : joinPartnersTP) {
						final Literal min = (minima == null) ? null : minima
								.get(v);
						if (intermediate.get(v) == null
								|| min != null
								&& (intermediate.get(v).minimum == null || min
										.compareToNotNecessarilySPARQLSpecificationConform(intermediate
												.get(v).minimum) != 0)) {
							flag = false;
							break;
						}
						final Literal max = (maxima == null) ? null : maxima
								.get(v);
						if (max != null
								&& (intermediate.get(v).maximum == null || max
										.compareToNotNecessarilySPARQLSpecificationConform(intermediate
												.get(v).maximum) != 0)) {
							flag = false;
							break;
						}
					}
					if (flag) {
						intermediate.keySet().retainAll(joinPartnersTP);
						// if (classBindings == BindingsArrayVarMinMax.class) {
						for (final Variable v : intermediate.keySet()) {
							final VarBucket vb = intermediate.get(v);
							final Literal l[] = new Literal[vb.selectivityOfInterval
									.size()];
							int indexLiteral = 0;
							for (final Entry entry : vb.selectivityOfInterval) {
								l[indexLiteral] = entry.literal;
								indexLiteral++;
							}
							tp.addHistogram(v, l, vb.getSum());
						}
						// }
						return intermediate;
					}
				}
			}
		}
		final Map<Variable, VarBucket> result = new HashMap<Variable, VarBucket>();
		for (final Variable v : joinPartnersTP) {
			final Literal min = (minima == null) ? null : minima.get(v);
			final Literal max = (maxima == null) ? null : maxima.get(v);
			if (intermediate != null && intermediate.containsKey(v)) {
				boolean flag = true;
				if (intermediate.get(v) == null
						|| min != null
						&& (intermediate.get(v).minimum == null || min
								.compareToNotNecessarilySPARQLSpecificationConform(intermediate
										.get(v).minimum) != 0)) {
					flag = false;
				}
				if (max != null
						&& (intermediate.get(v).maximum == null || max
								.compareToNotNecessarilySPARQLSpecificationConform(intermediate
										.get(v).maximum) != 0)) {
					flag = false;
				}
				if (flag) {
					result.put(v, intermediate.get(v));
					continue;
				}
			}
			try {

				// get the graph constraint from the super class.
				// If it is null, a default graph is used, if not null a named
				// one
				// is used
				final Item graphConstraintItem = this.getGraphConstraint();

				// get a collection of indices using the determined graph
				// constraint
				final Collection<Indices> indicesC = this.root.dataset.indexingRDFGraphs(
						graphConstraintItem, false, false, this.root);
				if ((indicesC != null) && !(indicesC.isEmpty())) {
					final Triple key = getKey(tp, null);
					final Collection<URILiteral> namedGraphs = new ArrayList<URILiteral>();

					// if the graph constraint is not null (which means that a
					// named
					// graph is used)
					if (graphConstraintItem != null) {

						if (graphConstraintItem instanceof Variable) {

							final Variable graphConstraint = (Variable) graphConstraintItem;

							// check if named graphs were provided at query time
							if (this.root.namedGraphs != null
									&& !this.root.namedGraphs.isEmpty()) {

								// Convert the named graphs' names into
								// URILiterals
								// to be applicable
								// later on
								for (final String name : this.root.namedGraphs) {

									final Indices indices = this.root.dataset
											.getNamedGraphIndices(LiteralFactory
													.createURILiteralWithoutLazyLiteral(name));

									final URILiteral rdfName = indices
											.getRdfName();
									if (namedGraphs.contains(rdfName)) {
										final TriplePattern ztp = new TriplePattern(
												graphConstraint.equals(tp
														.getPos(0)) ? rdfName
														: tp.getPos(0),
												graphConstraint.equals(tp
														.getPos(1)) ? rdfName
														: tp.getPos(1),
												graphConstraint.equals(tp
														.getPos(2)) ? rdfName
														: tp.getPos(2));
										final Triple zkey = getKey(ztp, null);
										final Triple keyMinimum = this.getKey(ztp,
												null, minima);
										final Triple keyMaximum = this.getKey(ztp,
												null, maxima);

										final VarBucket vb = this.getVarBucket(v,
												ztp, zkey, keyMinimum,
												keyMaximum,
												(SixIndices) indices);
										if (vb != null) {
											final VarBucket previous_vb = result
													.get(v);
											if (previous_vb != null) {
												vb.add(previous_vb);
											}
											vb.minimum = (minima == null) ? null
													: minima.get(v);
											vb.maximum = (maxima == null) ? null
													: maxima.get(v);
											result.put(v, vb);
										}
									}

								}

							}

							// otherwise there might have been named graphs
							// added
							// during the evaluation
							else {

								// get all indices of named graphs and bind them
								// to
								// the graph constraint
								final Collection<Indices> dataSetIndices = this.root.dataset
										.getNamedGraphIndices();
								if (dataSetIndices != null) {

									for (final Indices indices : dataSetIndices) {
										final TriplePattern ztp = new TriplePattern(
												graphConstraint.equals(tp
														.getPos(0)) ? indices
														.getRdfName() : tp
														.getPos(0),
												graphConstraint.equals(tp
														.getPos(1)) ? indices
														.getRdfName() : tp
														.getPos(1),
												graphConstraint.equals(tp
														.getPos(2)) ? indices
														.getRdfName() : tp
														.getPos(2));
										final Triple zkey = getKey(ztp, null);
										final Triple keyMinimum = this.getKey(ztp,
												null, minima);
										final Triple keyMaximum = this.getKey(ztp,
												null, maxima);
										final VarBucket vb = this.getVarBucket(v,
												ztp, zkey, keyMinimum,
												keyMaximum,
												(SixIndices) indices);
										if (vb != null) {
											final VarBucket previous_vb = result
													.get(v);
											if (previous_vb != null) {
												vb.add(previous_vb);
											}
											vb.minimum = (minima == null) ? null
													: minima.get(v);
											vb.maximum = (maxima == null) ? null
													: maxima.get(v);
											result.put(v, vb);
										}
									}
								}
							}
						}

						// if the graph constraint is an URILiteral fetch the
						// matching indices object
						// but do not bind anything
						else {
							final Triple keyMinimum = this.getKey(tp, null, minima);
							final Triple keyMaximum = this.getKey(tp, null, maxima);
							for (final Indices indices : indicesC) {

								final URILiteral rdfName = indices.getRdfName();
								if (namedGraphs.contains(rdfName)) {
									final VarBucket vb = this.getVarBucket(v, tp,
											key, keyMinimum, keyMaximum,
											(SixIndices) indices);
									if (vb != null) {
										final VarBucket previous_vb = result
												.get(v);
										if (previous_vb != null) {
											vb.add(previous_vb);
										}
										vb.minimum = (minima == null) ? null
												: minima.get(v);
										vb.maximum = (maxima == null) ? null
												: maxima.get(v);
										result.put(v, vb);
									}
								}

							}
						}
					} else {
						if (indicesC != null) {
							// deal with special case: several default graphs!
							// if (triplePatterns.size() != 1) {
							//log.error("Can only process one triple pattern!");
							// }
							final Triple keyMinimum = this.getKey(tp, null, minima);
							final Triple keyMaximum = this.getKey(tp, null, maxima);

							for (final Indices indices : indicesC) {
								final VarBucket vb = this.getVarBucket(v, tp, key,
										keyMinimum, keyMaximum,
										(SixIndices) indices);
								if (vb != null) {
									vb.minimum = (minima == null) ? null
											: minima.get(v);
									vb.maximum = (maxima == null) ? null
											: maxima.get(v);
									final VarBucket previous_vb = result.get(v);
									if (previous_vb != null) {
										vb.add(previous_vb);
									}
									result.put(v, vb);
								}
							}
						}
					}
				}
			} catch (final Exception e) {
				System.err.println("Error while joining triple patterns: "+ e);
				e.printStackTrace();
				return null;
			}

		}
		// if (classBindings == BindingsArrayVarMinMax.class) {
		for (final Variable v : result.keySet()) {
			final VarBucket vb = result.get(v);
			final Literal l[] = new Literal[vb.selectivityOfInterval.size()];
			int indexLiteral = 0;
			for (final Entry entry : vb.selectivityOfInterval) {
				l[indexLiteral] = entry.literal;
				indexLiteral++;
			}
			tp.addHistogram(v, l, vb.getSum());
		}
		// }
		// if (intermediate != null)
		// storeVarBuckets(tp, intermediate, keyHistogram);
		// else
		// if (intermediate != null) {
		// if (!result.keySet().containsAll(intermediate.keySet())) {
		// final Map<Variable, VarBucket> result2 = new HashMap<Variable,
		// VarBucket>();
		// result2.putAll(result);
		// for (final java.util.Map.Entry<Variable, VarBucket> entry :
		// intermediate
		// .entrySet()) {
		// if (!result2.containsKey(entry.getKey()))
		// result2.put(entry.getKey(), entry.getValue());
		// }
		// storeVarBuckets(tp, result2, keyHistogram);
		// return result;
		// }
		// }
		if (!result.isEmpty()) {
			this.storeVarBuckets(tp, result, keyHistogram);
			return result;
		} else {
			return null;
		}
	}

	private VarBucket getVarBucket(final Variable v, final TriplePattern tp,
			final Triple key, final Triple keyMinimum, final Triple keyMaximum,
			final SixIndices indices) {
		int pos = 0;
		for (; pos < 3; pos++) {
			if (tp.getItems()[pos].isVariable() && v.equals(tp.getItems()[pos])) {
				break;
			}
		}
		final Collection<Variable> cv = new LinkedList<Variable>();
		cv.add(v);
		int i = 0;
		for (final Item item : tp) {
			if (item.isVariable() && keyMinimum != null
					&& keyMinimum.getPos(i) != null && !cv.contains(item)) {
				cv.add((Variable) item);
			}
			i++;
		}
		final CollationOrder order = getCollationOrder(tp, cv);
		return indices.getDBBPTreeStatistics(order).getVarBucket(
				new TripleKey(key, order),
				keyMinimum == null ? null : new TripleKey(keyMinimum, order),
				keyMaximum == null ? null : new TripleKey(keyMaximum, order),
				pos);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "RDF3X"
				+ super.toString()
				+ ((this.collationOrder == null) ? "" : "\nCollationOrder:"
						+ CollationOrder.values()[this.collationOrder.ordinal()]);
	}

	/** {@inheritDoc} */
	@Override
	public String toString(final lupos.rdf.Prefix prefixInstance) {
		return "RDF3X "
				+ super.toString(prefixInstance)
				+ ((this.collationOrder == null) ? "" : "\nCollationOrder:"
						+ CollationOrder.values()[this.collationOrder.ordinal()]);
	}
}
