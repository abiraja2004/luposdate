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
package lupos.engine.evaluators;

import java.util.Collection;
import java.util.Date;

import lupos.datastructures.dbmergesortedds.heap.Heap;
import lupos.datastructures.dbmergesortedds.tosort.ToSort;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.index.BasicIndexScan;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.index.Indices;
import lupos.engine.operators.index.memoryindex.MemoryIndexRoot;
import lupos.engine.operators.index.memoryindex.SevenMemoryIndices;
import lupos.misc.Tuple;
public class MemoryIndexQueryEvaluator extends BasicIndexQueryEvaluator {

	public enum Optimizations {
		NONE, MOSTRESTRICTIONS, MOSTRESTRICTIONSLEASTENTRIES, LEASTENTRIES, BINARY, BINARYSTATICANALYSIS;
	}

	/**
	 * <p>Constructor for MemoryIndexQueryEvaluator.</p>
	 *
	 * @throws java.lang.Exception if any.
	 */
	public MemoryIndexQueryEvaluator() throws Exception {
		super();
	}

	/**
	 * <p>Constructor for MemoryIndexQueryEvaluator.</p>
	 *
	 * @param arguments an array of {@link java.lang.String} objects.
	 * @throws java.lang.Exception if any.
	 */
	public MemoryIndexQueryEvaluator(final String[] arguments) throws Exception {
		super(arguments);
	}
	
	/**
	 * <p>Constructor for MemoryIndexQueryEvaluator.</p>
	 *
	 * @param debug a DEBUG object.
	 * @param multiplequeries a boolean.
	 * @param compare a compareEvaluator object.
	 * @param compareoptions a {@link java.lang.String} object.
	 * @param times a int.
	 * @param dataset a {@link java.lang.String} object.
	 * @param type a {@link java.lang.String} object.
	 * @param externalontology a {@link java.lang.String} object.
	 * @param inmemoryexternalontologyinference a boolean.
	 * @param rdfs a RDFS object.
	 * @param codemap a {@link lupos.datastructures.items.literal.LiteralFactory.MapType} object.
	 * @param tmpDirs an array of {@link java.lang.String} objects.
	 * @param loadindexinfo a boolean.
	 * @param parallelOperands a PARALLELOPERANDS object.
	 * @param blockwise a boolean.
	 * @param limit a int.
	 * @param jointhreads a int.
	 * @param joinbuffer a int.
	 * @param heap a {@link lupos.datastructures.dbmergesortedds.heap.Heap.HEAPTYPE} object.
	 * @param tosort a {@link lupos.datastructures.dbmergesortedds.tosort.ToSort.TOSORT} object.
	 * @param indexheap a int.
	 * @param mergeheapheight a int.
	 * @param mergeheaptype a {@link lupos.datastructures.dbmergesortedds.heap.Heap.HEAPTYPE} object.
	 * @param chunk a int.
	 * @param mergethreads a int.
	 * @param yagomax a int.
	 * @param resulttype a {@link lupos.datastructures.queryresult.QueryResult.TYPE} object.
	 * @param storage a STORAGE object.
	 * @param join a JOIN object.
	 * @param optional a JOIN object.
	 * @param sort a SORT object.
	 * @param distinct a DISTINCT object.
	 * @param merge_join_optional a MERGE_JOIN_OPTIONAL object.
	 * @param encoding a {@link java.lang.String} object.
	 * @param datastructure a {@link lupos.engine.operators.index.Indices.DATA_STRUCT} object.
	 * @param datasetsort a {@link lupos.engine.operators.index.Dataset.SORT} object.
	 * @param optimization a {@link lupos.engine.evaluators.MemoryIndexQueryEvaluator.Optimizations} object.
	 */
	public MemoryIndexQueryEvaluator(DEBUG debug, boolean multiplequeries, compareEvaluator compare, String compareoptions, int times, String dataset,
			final String type, final String externalontology,
			final boolean inmemoryexternalontologyinference, final RDFS rdfs,
			final LiteralFactory.MapType codemap, final String[] tmpDirs,
			final boolean loadindexinfo,
			final PARALLELOPERANDS parallelOperands, final boolean blockwise,
			final int limit, final int jointhreads, final int joinbuffer,
			final Heap.HEAPTYPE heap, final ToSort.TOSORT tosort,
			final int indexheap, final int mergeheapheight,
			final Heap.HEAPTYPE mergeheaptype, final int chunk,
			final int mergethreads, final int yagomax,
			final QueryResult.TYPE resulttype, final STORAGE storage,
			final JOIN join, final JOIN optional, final SORT sort,
			final DISTINCT distinct,
			final MERGE_JOIN_OPTIONAL merge_join_optional, final String encoding,
			final lupos.engine.operators.index.Indices.DATA_STRUCT datastructure,
			final Dataset.SORT datasetsort,
			final Optimizations optimization){
		super(debug, multiplequeries, compare, compareoptions, times, dataset,
				type, externalontology,inmemoryexternalontologyinference, rdfs, codemap, tmpDirs, loadindexinfo,
				parallelOperands, blockwise,
				limit, jointhreads, joinbuffer,
				heap, tosort, indexheap, mergeheapheight, mergeheaptype, chunk, mergethreads, yagomax,
				resulttype, storage, join, optional, sort, distinct,
				merge_join_optional, encoding,
				datastructure, datasetsort);
		init(datastructure, optimization);
	}

	private void init(final Indices.DATA_STRUCT datastructure_param,
			final Optimizations optimization) {
		Indices.setUsedDatastructure(datastructure_param);
		switch (optimization) {
		case MOSTRESTRICTIONS:
			this.opt = BasicIndexScan.MOSTRESTRICTIONS;
			break;
		case MOSTRESTRICTIONSLEASTENTRIES:
			this.opt = BasicIndexScan.MOSTRESTRICTIONSLEASTENTRIES;
			break;
		case LEASTENTRIES:
			this.opt = BasicIndexScan.LEASTENTRIES;
			break;
		case BINARY:
			this.opt = BasicIndexScan.BINARY;
			break;
		case BINARYSTATICANALYSIS:
			this.opt = BasicIndexScan.BINARYSTATICANALYSIS;
			break;
		default:
			this.opt = BasicIndexScan.NONE;
			break;
		}
	}

	/** {@inheritDoc} */
	@Override
	public void init() throws Exception {
		super.init();
		// IndexMaps.setUsedDatastructure((IndexMaps.DATA_STRUCT)args.getEnum(
		// "datastructure"));
		init((Indices.DATA_STRUCT) this.args.getEnum("datastructure"),
				(Optimizations) this.args.getEnum("optimization"));
	}

	/** {@inheritDoc} */
	@Override
	public void setupArguments() {
		this.defaultOptimization = Optimizations.MOSTRESTRICTIONSLEASTENTRIES;
		super.setupArguments();
	}

	/** {@inheritDoc} */
	@Override
	public long prepareInputData(final Collection<URILiteral> defaultGraphs,
			final Collection<URILiteral> namedGraphs) throws Exception {
		final Date a = new Date();
		super.prepareInputData(defaultGraphs, namedGraphs);
		this.dataset = new Dataset(defaultGraphs, namedGraphs, this.type,
				getMaterializeOntology(), this.opt, new Dataset.IndicesFactory() {
			@Override
			public Indices createIndices(final URILiteral uriLiteral) {
				return new SevenMemoryIndices(uriLiteral);
			}

			@Override
			public lupos.engine.operators.index.Root createRoot() {
				MemoryIndexRoot ic=new MemoryIndexRoot();
				ic.dataset=MemoryIndexQueryEvaluator.this.dataset;
				return ic;
			}
		}, this.debug != DEBUG.NONE, this.inmemoryexternalontologyinference);
		this.dataset.buildCompletelyAllIndices();
		final long prepareTime = ((new Date()).getTime() - a.getTime());
		return prepareTime;
	}

	/**
	 * <p>getRoot.</p>
	 *
	 * @return a {@link lupos.engine.operators.index.memoryindex.MemoryIndexRoot} object.
	 */
	public MemoryIndexRoot getRoot() {
		return (MemoryIndexRoot) this.root;
	}

	/**
	 * <p>main.</p>
	 *
	 * @param args an array of {@link java.lang.String} objects.
	 */
	public static void main(final String[] args) {
		_main(args, MemoryIndexQueryEvaluator.class);
	}

	/** {@inheritDoc} */
	@Override
	public lupos.engine.operators.index.Root createRoot() {
		MemoryIndexRoot ic=new MemoryIndexRoot();
		ic.dataset=this.dataset;
		return ic;
	}

	/** {@inheritDoc} */
	@Override
	public long prepareInputDataWithSourcesOfNamedGraphs(
			Collection<URILiteral> defaultGraphs,
			Collection<Tuple<URILiteral, URILiteral>> namedGraphs)
			throws Exception {
		final Date a = new Date();
		super.prepareInputDataWithSourcesOfNamedGraphs(defaultGraphs, namedGraphs);
		this.dataset = new Dataset(defaultGraphs, namedGraphs,
				getMaterializeOntology(), this.type, this.opt, new Dataset.IndicesFactory() {
			@Override
			public Indices createIndices(final URILiteral uriLiteral) {
				return new SevenMemoryIndices(uriLiteral);
			}

			@Override
			public lupos.engine.operators.index.Root createRoot() {
				MemoryIndexRoot ic=new MemoryIndexRoot();
				ic.dataset=MemoryIndexQueryEvaluator.this.dataset;
				return ic;
			}
		}, this.debug != DEBUG.NONE, this.inmemoryexternalontologyinference);
		this.dataset.buildCompletelyAllIndices();
		final long prepareTime = ((new Date()).getTime() - a.getTime());
		return prepareTime;
	}
}
