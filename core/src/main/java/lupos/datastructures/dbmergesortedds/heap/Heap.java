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
package lupos.datastructures.dbmergesortedds.heap;

import java.util.Iterator;

import lupos.datastructures.dbmergesortedds.tosort.ToSort;
import lupos.misc.util.ImmutableIterator;
public abstract class Heap<E extends Comparable<E>> extends ToSort<E> {

	public enum HEAPTYPE {
		DEFAULT, SEQUENTIAL, OPTIMIZEDSEQUENTIAL, PARALLEL, PARALLEL8, PARALLEL16, PARALLEL32, LAZYOPTIMIZEDSEQUENTIAL, LAZYPARALLEL, LAZYPARALLEL8, LAZYPARALLEL16, LAZYPARALLEL32, SORTANDMERGEHEAP, SORTANDMERGEHEAPUSINGMERGESORT
	}

	/** Constant <code>heapType</code> */
	protected static HEAPTYPE heapType = HEAPTYPE.SEQUENTIAL;

	/**
	 * <p>maxLength.</p>
	 *
	 * @return a int.
	 */
	public abstract int maxLength();

	/**
	 * <p>peek.</p>
	 *
	 * @return a E object.
	 */
	public abstract E peek();

	/**
	 * <p>pop.</p>
	 *
	 * @return a E object.
	 */
	public abstract E pop();

	/** {@inheritDoc} */
	@Override
	public abstract int size();

	/** {@inheritDoc} */
	@Override
	public Iterator<E> emptyDatastructure() {
		return new ImmutableIterator<E>() {

			@Override
			public boolean hasNext() {
				return !Heap.this.isEmpty();
			}

			@Override
			public E next() {
				return Heap.this.pop();
			}
		};
	}

	/**
	 * <p>getContent.</p>
	 *
	 * @return an array of {@link java.lang.Object} objects.
	 */
	protected abstract Object[] getContent();

	/** {@inheritDoc} */
	@Override
	public abstract String toString();

	/** {@inheritDoc} */
	public static<T extends Comparable<T>> Heap<T> createInstance(final int height) {
		return createInstance(height, Heap.heapType);
	}

	/**
	 * <p>createInstance.</p>
	 *
	 * @param height a int.
	 * @param heapType_param a {@link lupos.datastructures.dbmergesortedds.heap.Heap.HEAPTYPE} object.
	 * @param <T> a T object.
	 * @return a {@link lupos.datastructures.dbmergesortedds.heap.Heap} object.
	 */
	public static<T extends Comparable<T>> Heap<T> createInstance(final int height, final HEAPTYPE heapType_param) {
		switch (heapType_param) {
		default:
		case SEQUENTIAL:
			return new SequentialHeap<T>(height);
		case OPTIMIZEDSEQUENTIAL:
			return new OptimizedSequentialHeap<T>(height);
		case PARALLEL:
			return new ParallelHeap<T>(height);
		case PARALLEL8:
			return new ParallelHeap<T>(height, 3);
		case PARALLEL16:
			return new ParallelHeap<T>(height, 4);
		case PARALLEL32:
			return new ParallelHeap<T>(height, 5);
		case LAZYOPTIMIZEDSEQUENTIAL:
			return new LazyBuildingSequentialHeap<T>(height);
		case LAZYPARALLEL:
			return new LazyBuildingParallelHeap<T>(height);
		case LAZYPARALLEL8:
			return new LazyBuildingParallelHeap<T>(height, 3);
		case LAZYPARALLEL16:
			return new LazyBuildingParallelHeap<T>(height, 4);
		case LAZYPARALLEL32:
			return new LazyBuildingParallelHeap<T>(height, 5);
		case SORTANDMERGEHEAP:
			return new SortAndMergeHeap<T>(1 << (height + 1));
		case SORTANDMERGEHEAPUSINGMERGESORT:
			return new SortAndMergeHeap<T>(1 << (height + 1), false);
		}
	}

	/**
	 * <p>createInstance.</p>
	 *
	 * @param length_or_height a int.
	 * @param length a boolean.
	 * @param <T> a T object.
	 * @return a {@link lupos.datastructures.dbmergesortedds.heap.Heap} object.
	 */
	public static<T extends Comparable<T>> Heap<T> createInstance(final int length_or_height, final boolean length) {
		return createInstance(length_or_height, length, Heap.heapType);
	}

	/**
	 * <p>createInstance.</p>
	 *
	 * @param length_or_height a int.
	 * @param length a boolean.
	 * @param heapType_param a {@link lupos.datastructures.dbmergesortedds.heap.Heap.HEAPTYPE} object.
	 * @param <T> a T object.
	 * @return a {@link lupos.datastructures.dbmergesortedds.heap.Heap} object.
	 */
	public static<T extends Comparable<T>> Heap<T> createInstance(final int length_or_height, final boolean length, final HEAPTYPE heapType_param) {
		switch (heapType_param) {
		default:
		case SEQUENTIAL:
			return new SequentialHeap<T>(length_or_height, length);
		case OPTIMIZEDSEQUENTIAL:
			return new OptimizedSequentialHeap<T>(length_or_height, length);
		case PARALLEL:
			return new ParallelHeap<T>(length_or_height, length);
		case PARALLEL8:
			return new ParallelHeap<T>(length_or_height, length, 3);
		case PARALLEL16:
			return new ParallelHeap<T>(length_or_height, length, 4);
		case PARALLEL32:
			return new ParallelHeap<T>(length_or_height, length, 5);
		case LAZYOPTIMIZEDSEQUENTIAL:
			return new LazyBuildingSequentialHeap<T>(length_or_height, length);
		case LAZYPARALLEL:
			return new LazyBuildingParallelHeap<T>(length_or_height, length);
		case LAZYPARALLEL8:
			return new LazyBuildingParallelHeap<T>(length_or_height, length, 3);
		case LAZYPARALLEL16:
			return new LazyBuildingParallelHeap<T>(length_or_height, length, 4);
		case LAZYPARALLEL32:
			return new LazyBuildingParallelHeap<T>(length_or_height, length, 5);
		case SORTANDMERGEHEAP:
			return new SortAndMergeHeap<T>(length ? length_or_height : 1 << (length_or_height + 1));
		case SORTANDMERGEHEAPUSINGMERGESORT:
			return new SortAndMergeHeap<T>(length ? length_or_height : 1 << (length_or_height + 1), false);
		}
	}

	/**
	 * <p>cloneInstance.</p>
	 *
	 * @param heap a {@link lupos.datastructures.dbmergesortedds.heap.Heap} object.
	 * @param <T> a T object.
	 * @return a {@link lupos.datastructures.dbmergesortedds.heap.Heap} object.
	 */
	public static<T extends Comparable<T>> Heap<T> cloneInstance(final Heap<T> heap) {
		return Heap.cloneInstance(heap, Heap.heapType);
	}

	/**
	 * <p>cloneInstance.</p>
	 *
	 * @param heap_param a {@link lupos.datastructures.dbmergesortedds.heap.Heap} object.
	 * @param heapType_param a {@link lupos.datastructures.dbmergesortedds.heap.Heap.HEAPTYPE} object.
	 * @param <T> a T object.
	 * @return a {@link lupos.datastructures.dbmergesortedds.heap.Heap} object.
	 */
	public static<T extends Comparable<T>> Heap<T> cloneInstance(final Heap<T> heap_param, final HEAPTYPE heapType_param) {
		switch (heapType_param) {
		default:
		case SEQUENTIAL:
			return new SequentialHeap<T>(heap_param.getContent(), heap_param.size());
		case OPTIMIZEDSEQUENTIAL:
			return new OptimizedSequentialHeap<T>(heap_param.getContent(), heap_param.size());
		case PARALLEL:
			return new ParallelHeap<T>(heap_param.getContent(), heap_param.size());
		case PARALLEL8:
			return new ParallelHeap<T>(heap_param.getContent(), heap_param.size(), 3);
		case PARALLEL16:
			return new ParallelHeap<T>(heap_param.getContent(), heap_param.size(), 4);
		case PARALLEL32:
			return new ParallelHeap<T>(heap_param.getContent(), heap_param.size(), 5);
		case LAZYOPTIMIZEDSEQUENTIAL:
			return new LazyBuildingSequentialHeap<T>(heap_param.getContent(), heap_param.size());
		case LAZYPARALLEL:
			return new LazyBuildingParallelHeap<T>(heap_param.getContent(), heap_param.size());
		case LAZYPARALLEL8:
			return new LazyBuildingParallelHeap<T>(heap_param.getContent(), heap_param.size(), 3);
		case LAZYPARALLEL16:
			return new LazyBuildingParallelHeap<T>(heap_param.getContent(), heap_param.size(), 4);
		case LAZYPARALLEL32:
			return new LazyBuildingParallelHeap<T>(heap_param.getContent(), heap_param.size(), 5);
		case SORTANDMERGEHEAP:
			return new SortAndMergeHeap<T>(heap_param.getContent(), heap_param.maxLength());
		case SORTANDMERGEHEAPUSINGMERGESORT:
			return new SortAndMergeHeap<T>(heap_param.getContent(), heap_param.maxLength(), false);
		}
	}

	/**
	 * <p>Getter for the field <code>heapType</code>.</p>
	 *
	 * @return a {@link lupos.datastructures.dbmergesortedds.heap.Heap.HEAPTYPE} object.
	 */
	public static HEAPTYPE getHeapType() {
		return heapType;
	}

	/**
	 * <p>Setter for the field <code>heapType</code>.</p>
	 *
	 * @param heapType a {@link lupos.datastructures.dbmergesortedds.heap.Heap.HEAPTYPE} object.
	 */
	public static void setHeapType(final HEAPTYPE heapType) {
		Heap.heapType = heapType;
	}
}
