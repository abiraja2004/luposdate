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
package lupos.optimizations.sparql2core_sparql;

import lupos.sparql1_1.*;
public interface SPARQL1_1ParserPathVisitorStringGenerator
{
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.SimpleNode} object.
   * @param subject a {@link java.lang.String} object.
   * @param object a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(SimpleNode node, String subject, String object);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTAVerbType} object.
   * @param subject a {@link java.lang.String} object.
   * @param object a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTAVerbType node, String subject, String object);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTPathAlternative} object.
   * @param subject a {@link java.lang.String} object.
   * @param object a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTPathAlternative node, String subject, String object);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTPathSequence} object.
   * @param subject a {@link java.lang.String} object.
   * @param object a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTPathSequence node, String subject, String object);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTInvers} object.
   * @param subject a {@link java.lang.String} object.
   * @param object a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTInvers node, String subject, String object);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTArbitraryOccurences} object.
   * @param subject a {@link java.lang.String} object.
   * @param object a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTArbitraryOccurences node, String subject, String object);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTOptionalOccurence} object.
   * @param subject a {@link java.lang.String} object.
   * @param object a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTOptionalOccurence node, String subject, String object);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTArbitraryOccurencesNotZero} object.
   * @param subject a {@link java.lang.String} object.
   * @param object a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTArbitraryOccurencesNotZero node, String subject, String object);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTNegatedPath} object.
   * @param subject a {@link java.lang.String} object.
   * @param object a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTNegatedPath node, String subject, String object);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTVar} object.
   * @param subject a {@link java.lang.String} object.
   * @param object a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTVar node, String subject, String object);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTQuotedURIRef} object.
   * @param subject a {@link java.lang.String} object.
   * @param object a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTQuotedURIRef node, String subject, String object);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTQName} object.
   * @param subject a {@link java.lang.String} object.
   * @param object a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTQName node, String subject, String object);
}
