/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe), University of Luebeck
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
/* Generated By:JJTree: Do not edit this line. ASTFloatingPoint.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package lupos.sparql1_1;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.Literal;
import lupos.engine.operators.singleinput.NotBoundException;
import lupos.engine.operators.singleinput.TypeErrorException;
import lupos.engine.operators.singleinput.ExpressionEvaluation.EvaluationVisitor;

public class ASTFloatingPoint extends SimpleNode {

  private String valueFloatingPoint;
  private Literal literal;

  public ASTFloatingPoint(int id) {
    super(id);
  }

  public ASTFloatingPoint(SPARQL1_1Parser p, int id) {
    super(p, id);
  }


  public String getValue() {
	return this.valueFloatingPoint;
  }

  public void setValue(String value) {
	this.valueFloatingPoint = value;
	this.literal = LazyLiteral.getLiteral(this, true);
  }

  public String getValueWithoutLeadingPlus(){
	  String zValue = this.valueFloatingPoint;
	  while(zValue.startsWith("+")){
		  zValue = zValue.substring(1, zValue.length());
	  }
	  return zValue;
  }
  
  public Literal getLiteral(){
	  return this.literal;
  }
  
  @Override
  public String toString() {
	return super.toString() + "  " + this.valueFloatingPoint;
  }
  
	@Override
	public void init(final SimpleNode node){
		this.setValue(((ASTFloatingPoint)node).getValue());
	}


  /** Accept the visitor. **/
  @Override
    public String accept(lupos.optimizations.sparql2core_sparql.SPARQL1_1ParserVisitorStringGenerator visitor) {
    return visitor.visit(this);
  }

    @Override
  public Object jjtAccept(SPARQL1_1ParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
  
  @Override
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public Object accept(EvaluationVisitor visitor, Bindings b, Object data) throws NotBoundException, TypeErrorException {
	    return visitor.evaluate(this, b, data);
  }
}
/* JavaCC - OriginalChecksum=bf18c743e56aed471d8117e069281a37 (do not edit this line) */
