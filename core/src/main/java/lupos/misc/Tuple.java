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
package lupos.misc;
public class Tuple<T1, T2> {

	protected T1 t1;
	protected T2 t2;

	/**
	 * <p>Constructor for Tuple.</p>
	 *
	 * @param t1 a T1 object.
	 * @param t2 a T2 object.
	 */
	public Tuple(final T1 t1, final T2 t2) {
		this.t1 = t1;
		this.t2 = t2;
	}

	/**
	 * <p>getFirst.</p>
	 *
	 * @return a T1 object.
	 */
	public T1 getFirst() {
		return t1;
	}

	/**
	 * <p>setFirst.</p>
	 *
	 * @param t1 a T1 object.
	 */
	public void setFirst(final T1 t1) {
		this.t1 = t1;
	}

	/**
	 * <p>getSecond.</p>
	 *
	 * @return a T2 object.
	 */
	public T2 getSecond() {
		return t2;
	}

	/**
	 * <p>setSecond.</p>
	 *
	 * @param t2 a T2 object.
	 */
	public void setSecond(final T2 t2) {
		this.t2 = t2;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object object) {
		if (object instanceof Tuple) {
			final Tuple tuple = (Tuple) object;
			if (!this.t1.equals(tuple.t1))
				return false;
			return this.t2.equals(tuple.t2);
		} else
			return false;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return (int) (((long) t1.hashCode() + t2.hashCode()) % Integer.MAX_VALUE);
	}
	
	/**
	 * <p>toString.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toString(){
		return "(" + t1.toString()+", "+t2.toString()+")";
	}
}
