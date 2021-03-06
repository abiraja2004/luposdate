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
package lupos.engine.operators.messages;

import java.util.Collection;
import java.util.HashSet;

import lupos.datastructures.items.Variable;
import lupos.engine.operators.BasicOperator;
public class BoundVariablesMessage extends Message {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6641732433881606823L;

	private Collection<Variable> variables = new HashSet<Variable>();

	/**
	 * <p>Constructor for BoundVariablesMessage.</p>
	 */
	public BoundVariablesMessage() {
	}

	/**
	 * <p>Constructor for BoundVariablesMessage.</p>
	 *
	 * @param msg a {@link lupos.engine.operators.messages.Message} object.
	 */
	public BoundVariablesMessage(final Message msg) {
		super(msg);
		// variables.addAll(((BoundVariablesMessage)msg).variables);
	}

	/** {@inheritDoc} */
	@Override
	public Message postProcess(final BasicOperator op) {
		return op.postProcessMessage(this);
	}

	/** {@inheritDoc} */
	@Override
	public Message preProcess(final BasicOperator op) {
		return op.preProcessMessage(this);
	}

	/** {@inheritDoc} */
	@Override
	public Message merge(final Collection<Message> msgs, final BasicOperator op) {
		return op.mergeMessages(msgs, this);
	}

	/**
	 * <p>Getter for the field <code>variables</code>.</p>
	 *
	 * @return a {@link java.util.Collection} object.
	 */
	public Collection<Variable> getVariables() {
		return variables;
	}

	/**
	 * <p>Setter for the field <code>variables</code>.</p>
	 *
	 * @param variables a {@link java.util.Collection} object.
	 */
	public void setVariables(final Collection<Variable> variables) {
		this.variables = variables;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return super.toString() + " " + variables;
	}

	/** {@inheritDoc} */
	@Override
	public Message clone() {
		final BoundVariablesMessage msg = new BoundVariablesMessage(this);
		msg.variables = new HashSet<Variable>();
		msg.variables.addAll(variables);
		msg.visited = (HashSet<BasicOperator>) visited.clone();
		return msg;
	}

}
