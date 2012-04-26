package com.graphbrain.inference

import org.scalatest.FunSuite


class RuleTest extends FunSuite {

	val posExpression = new POS("PRP VBP DT NN") 
	val regexExpression = new REGEX(".*(is\\sa).*")
	val graph2Expression = new GRAPH2(PLACEHOLDER("source"), StringExpression("relation"), PLACEHOLDER("target"))
	val pos_regex_graph2 = new RULE(posExpression, regexExpression, graph2Expression)
	
	

	test("Initialise POS:REGEX->GRAPH2 rule"){		
		
		assert(pos_regex_graph2.condition==posExpression)
		assert(pos_regex_graph2.input==regexExpression)
		assert(pos_regex_graph2.output==graph2Expression)
	}

	test("checkMatch for different rule expression types"){
		
		assert(RuleEngine.checkMatch(posExpression, "I am a person"))
		assert(RuleEngine.checkMatch(regexExpression, "Tom is a apple"))
		assert(RuleEngine.checkMatch(graph2Expression, ("any1", "relation", "any2")))

	}

	test("text replacement transform"){
		val condition = REGEX(".*(is\\sa).*")
		val toReplace = REGEX("is a")
		val replaceBy = "is an"


		val testSentence = "Tom is a apple"
		val expectedOutput="Tom is an apple"
		assert(RuleEngine.transform(toReplace, replaceBy, testSentence)==expectedOutput)

	}



	test("graph reciprocate transform") {
		//"A", "is colleagues with", "B", "B", "is colleagues with", "A"
		val inGraphExp=GRAPH2(PLACEHOLDER("A"), StringExpression("is colleagues with"), PLACEHOLDER("B"))
		val outGraphExp=GRAPH2(PLACEHOLDER("B"), StringExpression("is colleagues with"), PLACEHOLDER("A"))

		val testInGraph=("John", "is colleagues with", "Mary")
		val expectedOutGraph=("Mary", "is colleagues with", "John")

		assert(RuleEngine.transform(inGraphExp, outGraphExp, testInGraph)==expectedOutGraph)

	}

	test("graph reverse and replace transform") {
		//"A", "has a", "B", "B", "belongs to", "A"
		val inGraphExp=GRAPH2(PLACEHOLDER("A"), StringExpression("has a"), PLACEHOLDER("B"))
		val outGraphExp=GRAPH2(PLACEHOLDER("B"), StringExpression("belongs to"), PLACEHOLDER("A"))

		val testInGraph=("John", "has a", "motorbike")
		val expectedOutGraph=("motorbike", "belongs to", "John")
		assert(RuleEngine.transform(inGraphExp, outGraphExp, testInGraph)==expectedOutGraph)

	}

	test("graph simple replace transform") {
		//"A", "hates", "B", "A", "does not like", "B"
		val inGraphExp=GRAPH2(PLACEHOLDER("A"), StringExpression("hates"), PLACEHOLDER("B"))
		val outGraphExp=GRAPH2(PLACEHOLDER("A"), StringExpression("does not like"), PLACEHOLDER("B"))

		val testInGraph=("John", "hates", "Mary")
		val expectedOutGraph=("John", "does not like", "Mary")
		assert(RuleEngine.transform(inGraphExp, outGraphExp, testInGraph)==expectedOutGraph)

	}

	test("POS->(String->(String, String, String)) rule") {
		//A's B -> A has a B
		val posCondition=POS(".*(\\sPOS\\s).*")
		val inPOSExp=POS("POS")
		val outGraphExp1=GRAPH2(PLACEHOLDER("A"), StringExpression("has a"), PLACEHOLDER("B"))
		val outGraphExp2=GRAPH2(PLACEHOLDER("B"), StringExpression("belongs to"), PLACEHOLDER("A"))


		val testInString="John's dog"
		val expectedOutGraph1=("John", "has a", "dog")
		val expectedOutGraph2=("dog", "belongs to", "John")
		assert(RuleEngine.checkMatch(posCondition, testInString))
		assert(RuleEngine.transform(inPOSExp, outGraphExp1, testInString)==expectedOutGraph1)
		assert(RuleEngine.transform(inPOSExp, outGraphExp2, testInString)==expectedOutGraph2)

	}

	test("Composite rules") {
		val condExp1=GRAPH2(PLACEHOLDER("A"), StringExpression("is a"), PLACEHOLDER("B"))
		val condExp2=GRAPH2(PLACEHOLDER("B"), StringExpression("is a"), PLACEHOLDER("C"))
		val condExp3=GRAPH2(PLACEHOLDER("C"), StringExpression("is a"), PLACEHOLDER("D"))
		val outExp=GRAPH2(PLACEHOLDER("A"), StringExpression("is a"), PLACEHOLDER("C"))
		val compositeExp=COMPOSITE(condExp1, "AND", condExp2)
		val orderedExp=GRAPH2PAIR(condExp1, condExp3)
		val graphPair=GRAPH2PAIR(condExp1, condExp2)
		val rule1=RULE(compositeExp, graphPair, outExp);

		val testGraph1=("John", "is a", "surgeon")
		val testGraph2=("surgeon", "is a", "human")
		val testGraph3=("John", "is a", "human")

		assert(RuleEngine.checkMatch(compositeExp, (testGraph1, testGraph2)))
		assert(RuleEngine.checkMatch(orderedExp, (testGraph1, testGraph2))==false)
		assert(RuleEngine.checkMatch(graphPair, (testGraph1, testGraph2)))
		assert(RuleEngine.transform(graphPair, outExp, (testGraph1, testGraph2))==testGraph3)
		assert(RuleEngine.applyRule(rule1, (testGraph1, testGraph2))==testGraph3)

	}




}