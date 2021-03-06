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
package lupos.owl2rl.owlToRif;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.evaluators.CommonCoreQueryEvaluator;
import lupos.engine.evaluators.MemoryIndexQueryEvaluator;
import lupos.engine.operators.index.Indices;
import lupos.owl2rl.parser.ParserResults;
import lupos.rif.BasicIndexRuleEvaluator;
import lupos.rif.datatypes.Predicate;
public class InferenceRulesGenerator {

	// the inference rules
	private String output;

	/**results from the TemplateRuleParser**/
	private ParserResults parsedValues;
	/**whether print is on or Off**/
	private boolean printOn = false;

	/**number of emitted rules (not counting fixed rules)**/
	public static int rulesEmitted;

	/**
	 * <p>Constructor for InferenceRulesGenerator.</p>
	 *
	 * @param print a boolean.
	 */
	public InferenceRulesGenerator(final boolean print) {
		this.printOn = print;
	}

	/**
	 *Return the inference rules with prefixes and Document/Group brackets*
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getOutputRules() {
		return lupos.owl2rl.tools.Tools.document(this.parsedValues.getPrefixes()
						+ lupos.owl2rl.tools.Tools.group(this.parsedValues.getFixedrules()
								+ this.output));
	}

	/** Calls the Emitter Classes with the Results and returns the inference rules**/
	private String emit(
			final HashMap<Integer, LinkedList<BoundVariable>> variables,
			final HashMap<Integer, LinkedList<String>> ListOfOccuringOWLLists,
			final HashMap<Integer, LinkedHashMap<String, LinkedList<BoundVariable>>> ListRuleResults) {

		if (this.printOn) {
			this.printSomeInfo();
		}

		/**collects the inference rules**/
		final StringBuilder outputString = new StringBuilder();

		/*
		 *for property rules
		 */
		LinkedList<BoundVariable> variablesInList;
		for (int i = 0; i <= hit; i++) {

			/* variables, that were returned as query result for a rifquery*/
			variablesInList = variables.get(i);
			if (variablesInList == null){
				continue;
			}
			this.invokeEmitterMethodByName(variablesInList, outputString);
		}

		outputString.append("\n");

		/*
		 * Rules that need "special treatment" because of list structures etc...
		 */
		LinkedHashMap<String, LinkedList<BoundVariable>> list;
		// for all ListRules
		for (int i = 1; i <= RuleResultCounter; i++) {
			/* list = Lists of Variables, by Name of the List*/
			list = ListRuleResults.get(i);

			if (list == null) {
				continue;
			}

			// For all Lists from Ontology that were returned by query
			for (final String s : ListOfOccuringOWLLists.get(i)) {
				variablesInList = list.get(s); // All variables in the list s
				this.invokeEmitterMethodByName(variablesInList, outputString);
			}
		}
		ListRuleResults.clear();
		ListOfOccuringOWLLists.clear();
		return outputString.toString();
	}

	private void invokeEmitterMethodByName(
			final LinkedList<BoundVariable> variablesInList,
			final StringBuilder outputString) {
		// get MethodName from XML and use to Invoke Method with
		// Reflection api

		final Class<?>[] types = new Class<?>[] { variablesInList.getClass(), outputString.getClass(), this.parsedValues.getTemplateRulesmap().getClass() };
		final Object[] args = new Object[] { variablesInList, outputString, this.parsedValues.getTemplateRulesmap() };
		try {
			Class.forName(variablesInList.getLast().getClassName())
					.getDeclaredMethod(
							variablesInList.getLast().getMethodName(), types)
					.invoke(null, args);

		} catch (final Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * Handle Rule Results, that do not have bindings and are of the form
	 * :predicate(?x ?y ...)
	 *
	 * @param qr
	 * @param currentTemplateRule
	 * @param ListOfOccuringOWLLists
	 * @param ListRuleResults
	 */
	private void handleRuleResults(
			final lupos.rif.datatypes.RuleResult qr,
			final TemplateRule currentTemplateRule,
			final HashMap<Integer, LinkedList<String>> ListOfOccuringOWLLists,
			final HashMap<Integer, LinkedHashMap<String, LinkedList<BoundVariable>>> ListRuleResults) {

		RuleResultCounter++;
		final Collection<Predicate> pCollection = qr.getPredicateResults(); // triples of results
		String originalString = "";
		for (final Predicate p : pCollection) {

			// the predicate local.rulenames is a filter to avoid unneccessary
			// handling of the helperfunctions in the
			// rif-queries
			if (p.getName().toString().startsWith("<local.rulenames#")) {

				String listName = "";
				final List<Literal> literals = p.getParameters();

				for (final Literal l: literals) {
					if(!l.isBlank()){
						originalString = l.originalString();
					} else {
						originalString="External(func:bnode-from-string(\""+l.originalString()+"\"))";
					}
					if ((literals.indexOf(l) == 0)) {// first literal is the
														// name of the list
						listName = originalString;

						/* if the map does not contain  the  map that contains the lists...*/
						if (ListRuleResults.get(RuleResultCounter) == null)
						{
							// ...one is created.
							ListRuleResults.put(RuleResultCounter, new LinkedHashMap<String, LinkedList<BoundVariable>>());
							ListOfOccuringOWLLists.put(RuleResultCounter, new LinkedList<String>());

						}
						if (ListRuleResults.get(RuleResultCounter).get(listName) == null) {
							// If no list of variables exists one is created and
							// the name is saved seperately for reference;
							ListRuleResults.get(RuleResultCounter).put(listName, new LinkedList<BoundVariable>());

							ListOfOccuringOWLLists.get(RuleResultCounter).add(listName);
						}
					} else {
						// the variable is put in the corresponding list.
						ListRuleResults
								.get(RuleResultCounter)
								.get(listName)
								.add(new BoundVariable("", listName,
										currentTemplateRule, originalString));
						// debugString+="\nnr der Regel: "+RuleResultCounter+"; Name der Liste: "+listName
						// +"; Variable: "+value_Without_Prefix;
					}
				}
			}
		}
	}

	/**
	 * handles results that have bindings
	 */
	private void handleGraphResults(final QueryResult qr,
			final TemplateRule currentTemplateRule,
			final HashMap<Integer, LinkedList<BoundVariable>> variables) {
		final Iterator<Bindings> it_query = qr.oneTimeIterator();

		while (it_query.hasNext()) {
			// get next solution of the query...
			final Bindings bindings = it_query.next();
			variables.put(hit, new LinkedList<BoundVariable>());
			String originalString="";
			for (final Variable v : bindings.getVariableSet()) {
				// Save the results
				if(bindings.get(v).isBlank()){
					originalString="External(func:bnode-from-string(\""+bindings.get(v).originalString()+"\"))";
				} else{
					originalString=bindings.get(v).originalString();
				}
				variables.get(hit).add(new BoundVariable(v.toString(), "", currentTemplateRule,originalString ));
				}
			hit++;
		}
	}

	/**
	 * Performs Query and calls method for handling of result
	 *
	 * @param evaluator
	 * @param query
	 * @throws Exception
	 */
	private final void evaluateQuery(
			final BasicIndexRuleEvaluator evaluator,
			final String query,
			final TemplateRule currentTemplateRule,
			final HashMap<Integer, LinkedList<BoundVariable>> variables,
			final HashMap<Integer, LinkedList<String>> ListOfOccuringOWLLists,
			final HashMap<Integer, LinkedHashMap<String, LinkedList<BoundVariable>>> ListRuleResults)
			throws Exception {
		// evaluate query:
		final QueryResult qr = evaluator.getResult(query);
		// Leave method if no matches
		if (qr == (null) || qr.isEmpty()) {
			return;
		}

		if (this.printOn) {
			System.out.println("QueryResult: " + qr);
		}

		if (qr instanceof lupos.rif.datatypes.RuleResult) {
			this.handleRuleResults((lupos.rif.datatypes.RuleResult) qr,
					currentTemplateRule, ListOfOccuringOWLLists,
					ListRuleResults);
		} else {
			this.handleGraphResults(qr, currentTemplateRule, variables);
		}
	}

	/**
	 * Initializes Evaluator with ontology and arguments
	 *
	 * @return
	 */
	private BasicIndexRuleEvaluator initAndGetEvaluator(final String ontology) {
		try {
			// set up parameters of evaluators and initialize the evaluators...
			final MemoryIndexQueryEvaluator evaluator = new MemoryIndexQueryEvaluator();
			evaluator.setupArguments();
			evaluator.getArgs().set("type", "Turtle");
			evaluator.getArgs().set("result", QueryResult.TYPE.MEMORY);
			evaluator.getArgs().set("codemap", LiteralFactory.MapType.TRIEMAP);
			evaluator.getArgs().set("distinct",
					CommonCoreQueryEvaluator.DISTINCT.HASHSET);
			evaluator.getArgs().set("join",
					CommonCoreQueryEvaluator.JOIN.HASHMAPINDEX);
			evaluator.getArgs().set("optional",
					CommonCoreQueryEvaluator.JOIN.HASHMAPINDEX);
			evaluator.getArgs().set("datastructure",
					Indices.DATA_STRUCT.HASHMAP);
			evaluator.init();

			final LinkedList<URILiteral> dataIRIs = new LinkedList<URILiteral>();

			dataIRIs.add(LiteralFactory.createStringURILiteral("<inlinedata:"
					+ ontology + ">"));

			final BasicIndexRuleEvaluator rifEvaluator = new BasicIndexRuleEvaluator(
					evaluator);
			rifEvaluator.prepareInputData(dataIRIs,
					new LinkedList<URILiteral>());

			System.out.println("Evaluate rules...");

			return rifEvaluator;

		} catch (final Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return null;
	}

	/** Constant <code>hit</code> */
	public static int hit;
	private static int RuleResultCounter;
	/** Constant <code>debugString=""</code> */
	public static String debugString = "";

	/**
	 * Prints number of emitted Rules,Runtime, etc for debug purposes
	 */
	private void printAdditionalInfoAfterEmit() {
		// System.out.println("Rule Results: "+RuleResultCounter);
		// System.out.println("\n\nHits:"+(hitIdentity+RuleResultCounter)); //

		System.out.println(this.getOutputRules());

		System.out.println("\nEmitted Rules:" + rulesEmitted); //

		System.out.println("\n#################################################################\n");

		// Possibility for Testing speeds
		// System.out.println("Parsingtime: "+parsing_finishTime+"ms");
		// System.out.println("Querytime: "+query_finishTime+"ms");
		System.out.println(debugString);
	}

	/**
	 *debug etc.
	 */
	private void printSomeInfo() {
		final StringBuilder print = new StringBuilder();

		print.append("\n#################################################################\n \n");
		System.out.println(print);
	}



	/**
	 * Set the results from the rule template parser
	 *
	 * @param results a {@link lupos.owl2rl.parser.ParserResults} object.
	 */
	public void setParserResults(final ParserResults results) {
		this.parsedValues = results;
	}

	/**
	 * set debug info to System.out on or Off
	 *
	 * @param printOn a boolean.
	 */
	public void setPrintOnOrOff(final boolean printOn) {
		this.printOn = printOn;
	}

	/**
	 * start the evaluation and emitting of inference rules
	 *
	 * @param ontology a {@link java.lang.String} object.
	 */
	public void start(final String ontology) {
		final BasicIndexRuleEvaluator ruleEvaluator=this.initAndGetEvaluator(ontology);
		this.start(ruleEvaluator);
	}

	/**
	 * start the evaluation and emitting of inference rules
	 *
	 * @param evaluator a {@link lupos.rif.BasicIndexRuleEvaluator} object.
	 */
	public void start(final BasicIndexRuleEvaluator evaluator) {
		/**Results from Engine for GraphResults (Results with bindings)**/
		final HashMap<Integer, LinkedList<BoundVariable>> resultVariables = new HashMap<Integer, LinkedList<BoundVariable>>();
		/**Results from Engine for RuleResults (e.g. for query :predicate(?x ?y))**/
		final HashMap<Integer, LinkedHashMap<String, LinkedList<BoundVariable>>> ListRuleResults = new LinkedHashMap<Integer, LinkedHashMap<String, LinkedList<BoundVariable>>>();
		/**HashMap containing the listnames from RuleResults **/
		final HashMap<Integer, LinkedList<String>> ListOfOccuringOWLLists = new HashMap<Integer, LinkedList<String>>();

		/* reset static variables */
		this.output = "";
		rulesEmitted = 0;
		hit = 0;
		RuleResultCounter = 0;

		/*Send Queries to Engine, Evaluate an Sort Results in corresponding List */
		this.evaluate(resultVariables, evaluator, ListOfOccuringOWLLists, ListRuleResults);

		/* Let Emitter Construct the Inference Rules and Return Inference Rules */
		this.output = this.emit(resultVariables, ListOfOccuringOWLLists, ListRuleResults);

		//Optional
		if (this.printOn){
			this.printAdditionalInfoAfterEmit();
		}
	}

/**evaluate rif rules
 *
 * @param resultVariables
 * @param evaluator
 * @param listOfOccuringOWLLists
 * @param listRuleResults
 */
	private void evaluate(
			final HashMap<Integer, LinkedList<BoundVariable>> resultVariables,
			final BasicIndexRuleEvaluator evaluator,
			final HashMap<Integer, LinkedList<String>> listOfOccuringOWLLists,
			final HashMap<Integer, LinkedHashMap<String, LinkedList<BoundVariable>>> listRuleResults) {
			final BasicIndexRuleEvaluator rifEvaluator = evaluator;
			String query = "";
			// query_startTime=System.currentTimeMillis();
			TemplateRule currentTemplateRule;
			// for every Name
			for (int i = 0; i < this.parsedValues.getNames().length; i++) {

				// use as current Pattern/template Object
				currentTemplateRule = this.parsedValues.getTemplateRulesmap().get(
						this.parsedValues.getNames()[i]);

				// get query String
				query = currentTemplateRule.getPatternQuery();

				// evaluate Query
				try {
					this.evaluateQuery(rifEvaluator, query, currentTemplateRule,
							resultVariables, listOfOccuringOWLLists, listRuleResults);
				} catch (final Exception e) {

					e.printStackTrace();
				}
			}
			// query_finishTime = System.currentTimeMillis() - query_startTime;
		}
}
