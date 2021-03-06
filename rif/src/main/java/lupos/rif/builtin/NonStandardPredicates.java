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
package lupos.rif.builtin;

import lupos.datastructures.items.Item;
import lupos.datastructures.items.literal.AnonymousLiteral;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.TypedLiteral;
import lupos.datastructures.items.literal.URILiteral;

@Namespace(value = "http://www.w3.org/2007/rif-builtin-predicate#")
public class NonStandardPredicates {
	private NonStandardPredicates() {
	}

	/**
	 * <p>is_literal.</p>
	 *
	 * @param arg a {@link lupos.rif.builtin.Argument} object.
	 * @return a {@link lupos.rif.builtin.BooleanLiteral} object.
	 */
	@Builtin(Name = "is-literal")
	public static BooleanLiteral is_literal(final Argument arg) {
		Item arg0 = arg.arguments.get(0);
		if(arg0 instanceof LazyLiteral){
			arg0 = ((LazyLiteral) arg0).getLiteral();
		}
		return BooleanLiteral.create(!(arg0 instanceof AnonymousLiteral || arg0 instanceof URILiteral));
	}

	/**
	 * <p>is_blanknode.</p>
	 *
	 * @param arg a {@link lupos.rif.builtin.Argument} object.
	 * @return a {@link lupos.rif.builtin.BooleanLiteral} object.
	 */
	@Builtin(Name = "is-blanknode")
	public static BooleanLiteral is_blanknode(final Argument arg) {
		Item arg0 = arg.arguments.get(0);
		if(arg0 instanceof LazyLiteral){
			arg0 = ((LazyLiteral) arg0).getLiteral();
		}
		return BooleanLiteral.create(arg0 instanceof AnonymousLiteral);
	}

	/**
	 * <p>is_uri.</p>
	 *
	 * @param arg a {@link lupos.rif.builtin.Argument} object.
	 * @return a {@link lupos.rif.builtin.BooleanLiteral} object.
	 */
	@Builtin(Name = "is-uri")
	public static BooleanLiteral is_uri(final Argument arg) {
		Item arg0 = arg.arguments.get(0);
		if(arg0 instanceof LazyLiteral){
			arg0 = ((LazyLiteral) arg0).getLiteral();
		}
		return BooleanLiteral.create(arg0 instanceof URILiteral);
	}

	/**
	 * <p>is_integer_expression.</p>
	 *
	 * @param arg a {@link lupos.rif.builtin.Argument} object.
	 * @return a {@link lupos.rif.builtin.BooleanLiteral} object.
	 */
	@Builtin(Name = "is-integer-expression")
	public static BooleanLiteral is_integer_expression(final Argument arg) {
		Item arg0 = arg.arguments.get(0);
		if(arg0 instanceof LazyLiteral){
			arg0 = ((LazyLiteral) arg0).getLiteral();
		}
		final double value = BuiltinHelper.numberFromLiteral((TypedLiteral) arg0);
		return BooleanLiteral.create(Math.round(value)==value);
	}

}
