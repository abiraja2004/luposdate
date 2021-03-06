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
package lupos.gui.operatorgraph.visualeditor.queryeditor.operators;

import java.util.HashSet;
import java.util.LinkedList;

import lupos.gui.operatorgraph.prefix.Prefix;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.queryeditor.guielements.RetrieveDataPanel;
import lupos.gui.operatorgraph.visualeditor.util.ModificationException;
import lupos.misc.util.OperatorIDTuple;
import lupos.sparql1_1.ASTQuotedURIRef;
import lupos.sparql1_1.ASTVar;
import lupos.sparql1_1.SimpleNode;
public abstract class RetrieveDataWithProjectionAndSolutionModifier extends RetrieveDataWithSolutionModifier {
	protected LinkedList<String> projectionElements = new LinkedList<String>();

	/**
	 * <p>Constructor for RetrieveDataWithProjectionAndSolutionModifier.</p>
	 *
	 * @param prefix a {@link lupos.gui.operatorgraph.prefix.Prefix} object.
	 */
	protected RetrieveDataWithProjectionAndSolutionModifier(Prefix prefix) {
		super(prefix);
	}


	/**
	 * <p>parseProjectionElement.</p>
	 *
	 * @param projectionElement a {@link java.lang.String} object.
	 * @return a {@link lupos.sparql1_1.SimpleNode} object.
	 * @throws java.lang.Throwable if any.
	 */
	protected abstract SimpleNode parseProjectionElement(String projectionElement) throws Throwable;

	/**
	 * <p>addProjectionElement.</p>
	 *
	 * @param projectionElement a {@link java.lang.String} object.
	 * @throws lupos.gui.operatorgraph.visualeditor.util.ModificationException if any.
	 */
	public void addProjectionElement(String projectionElement) throws ModificationException {
		try {
			SimpleNode node = this.parseProjectionElement(projectionElement);

			if(node instanceof ASTVar) {
				this.projectionElements.add(((ASTVar) node).toQueryString());
			}
			else if(node instanceof ASTQuotedURIRef) {
				this.projectionElements.add(((ASTQuotedURIRef) node).toQueryString());
			}
		}
		catch(Throwable t) {
			this.handleParseError(t);
		}
	}

	/**
	 * <p>setProjectionElement.</p>
	 *
	 * @param index a int.
	 * @param projectionElement a {@link java.lang.String} object.
	 * @throws lupos.gui.operatorgraph.visualeditor.util.ModificationException if any.
	 */
	public void setProjectionElement(int index, String projectionElement) throws ModificationException {
		try {
			// new element...
			if(this.projectionElements.size() == index) {
				this.projectionElements.add("");
			}

			// parse new value
			SimpleNode node = this.parseProjectionElement(projectionElement);

			// remove old value...
			if(this.projectionElements.get(index) != null) {
				this.projectionElements.remove(index);
			}

			// add new value...
			if(node instanceof ASTVar) {
				this.projectionElements.add(index, ((ASTVar) node).toQueryString());
			}
			else if(node instanceof ASTQuotedURIRef) {
				this.projectionElements.add(index, ((ASTQuotedURIRef) node).toQueryString());
			}
		}
		catch(Throwable t) {
			this.handleParseError(t);
		}
	}

	/**
	 * <p>removeProjectionElement.</p>
	 *
	 * @param index a int.
	 */
	public void removeProjectionElement(int index) {
		if(index == this.projectionElements.size()) {
			return;
		}

		this.projectionElements.remove(index);
	}

	/**
	 * <p>Getter for the field <code>projectionElements</code>.</p>
	 *
	 * @return a {@link java.util.LinkedList} object.
	 */
	public LinkedList<String> getProjectionElements() {
		return this.projectionElements;
	}

	/**
	 * <p>clearProjectionList.</p>
	 */
	public void clearProjectionList() {
		this.projectionElements = new LinkedList<String>();
	}


	/**
	 * <p>serializeProjections.</p>
	 *
	 * @return a {@link java.lang.StringBuffer} object.
	 */
	public StringBuffer serializeProjections() {
		StringBuffer ret = new StringBuffer();

		if(this.projectionElements.isEmpty() || !((RetrieveDataPanel) this.panel).getElementStatus("projection")) {
			ret.append("*");
		}
		else {
			for(String projectionElement : this.projectionElements) {
				ret.append(projectionElement + " ");
			}
		}

		ret.append("\n");

		return ret;
	}


	/** {@inheritDoc} */
	public boolean variableInUse(String variable, HashSet<Operator> visited) {
		if(visited.contains(this)) {
			return false;
		}

		visited.add(this);


		if(super.variableInUse(variable, visited)) {
			return true;
		}

		for(String projElement : this.projectionElements) {
			if(projElement.equalsIgnoreCase(variable)) {
				return true;
			}
		}

		for(OperatorIDTuple<Operator> opIDT : this.succeedingOperators) {
			if(opIDT.getOperator().variableInUse(variable, visited)) {
				return true;
			}
		}

		return false;
	}
}
