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
package lupos.datastructures.items;

import java.io.Serializable;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.literal.Literal;
public class Variable implements Serializable, Item, Comparable<Variable> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8944702351169595750L;

	protected final String name;

	/**
	 * <p>Constructor for Variable.</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 */
	public Variable(final String name) {
		this.name = name;
	}

	/**
	 * <p>isVariable.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isVariable() {
		return true;
	}

	/**
	 * <p>Getter for the field <code>name</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getName() {
		return name;
	}

	/** {@inheritDoc} */
	public Literal getLiteral(final Bindings b) {
		return b.get(this);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "?" + name;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return name.hashCode();
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object o) {
		return ((o instanceof Variable) && ((Variable) o).getName().compareTo(
				name) == 0);
	}

	/**
	 * <p>compareTo.</p>
	 *
	 * @param o a {@link lupos.datastructures.items.Variable} object.
	 * @return a int.
	 */
	public int compareTo(final Variable o) {
		return name.compareTo(o.getName());
	}

	/**
	 * <p>equalsNormalOrVariableInInferenceRule.</p>
	 *
	 * @param o a {@link java.lang.Object} object.
	 * @return a boolean.
	 */
	public boolean equalsNormalOrVariableInInferenceRule(final Object o) {
		return (o instanceof Variable && this.getName().compareTo(
				((Variable) o).getName()) == 0);
	}
}
