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
public class Triple<T1, T2, T3> extends Tuple<T1, T2>{
	protected T3 t3;

	/**
	 * <p>Constructor for Triple.</p>
	 *
	 * @param t1 a T1 object.
	 * @param t2 a T2 object.
	 * @param t3 a T3 object.
	 */
	public Triple(final T1 t1, final T2 t2, final T3 t3) {
		super(t1, t2);
		this.t3 = t3;
	}

	/**
	 * <p>getThird.</p>
	 *
	 * @return a T3 object.
	 */
	public T3 getThird() {
		return this.t3;
	}

	/**
	 * <p>setThird.</p>
	 *
	 * @param t3 a T3 object.
	 */
	public void setThird(final T3 t3) {
		this.t3 = t3;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object object) {
		if(object instanceof Triple) {
			if(!super.equals(object)) {
				return false;
			}

			@SuppressWarnings("unchecked")
			final Triple<T1, T2, T3> triple = (Triple<T1, T2, T3>) object;

			return this.t3.equals(triple.t3);
		}
		else {
			return false;
		}
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return (int) (((long) this.t1.hashCode() + this.t2.hashCode() + this.t3.hashCode()) % Integer.MAX_VALUE);
	}

	/** {@inheritDoc} */
	@Override
	public String toString(){
		return "(" + this.t1.toString()+", "+this.t2.toString()+", "+this.t3.toString()+")";
	}
}
