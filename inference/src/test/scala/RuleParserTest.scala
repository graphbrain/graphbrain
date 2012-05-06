package com.graphbrain.inference

import org.scalatest.FunSuite


class RuleParserTest extends FunSuite {


	val posExpression = POS("PRP VBP DT NN") 
	val stringExpression = StringExpression("John Smith")
	val regexExpression = REGEX(".*(is\\sa).*")
	val placeholderExpression = PLACEHOLDER("A")
	val graph2Expression = GRAPH2(PLACEHOLDER("source"), StringExpression("relation"), PLACEHOLDER("target"))
	val test="POS(PRP VBP DT NN): REGEX(.*(is\\sa).*)>GRAPH2(PLACEHOLDER(source), STRING(relation), PLACEHOLDER(target))"
	val pos_regex_graph2 = RuleParser.parse(test);


	

  test("Parse POS:REGEX->GRAPH2 rule"){		

    pos_regex_graph2 match {
      case prg:RULE => 
        prg.condition match {
      	  case a:POS => assert(a == posExpression);
        }
        prg.input match {
          case a:REGEX => assert(a == regexExpression);
        }
        prg.output match {
          case a:GRAPH2 => assert(a == graph2Expression);
        }

    }	
		
  }

	test("Parse POS, REGEX, GRAPH2"){
		
		val parsedPOS = RuleParser.parse("POS(PRP VBP DT NN)")
		val parsedRegex = RuleParser.parse("REGEX(.*(is\\sa).*)")
		val parsedGRAPH2 = RuleParser.parse("GRAPH2(PLACEHOLDER(source), STRING(relation), PLACEHOLDER(target))")

		assert(RuleEngine.checkMatch(parsedPOS, "I am a person"))
		assert(RuleEngine.checkMatch(parsedRegex, "Tom is a apple"))
		assert(RuleEngine.checkMatch(parsedGRAPH2, ("any1", "relation", "any2")))

	}

	test("Parse text replacement rule"){
		val parsedRule=RuleParser.parse("REGEX(.*(is\\sa).*):STRING(is a)>STRING(is an)")

		val condition = REGEX(".*(is\\sa).*")
		val toReplace = StringExpression("is a")
		val replaceBy = StringExpression("is an")


		val testSentence = "Tom is a apple"
		val expectedOutput="Tom is an apple"

		parsedRule match {

		  case a:RULE => println(a);
		  	assert(a.condition==condition)
		  	assert(a.input==toReplace)
		  	assert(a.output==replaceBy)
		  	assert(RuleEngine.checkMatch(a.condition, testSentence))

			println(RuleEngine.applyRule(a, testSentence));
			assert(RuleEngine.transform(a.input, a.output, testSentence)==expectedOutput)
			assert(RuleEngine.applyRule(a, testSentence)==expectedOutput)	
		}

	}



  test("Parse graph reciprocate rule") {
  	val parsedRule=RuleParser.parse("GRAPH2(PLACEHOLDER(A), STRING(is colleagues with), PLACEHOLDER(B)): GRAPH2(PLACEHOLDER(A), STRING(is colleagues with), PLACEHOLDER(B)) > GRAPH2(PLACEHOLDER(B), STRING(is colleagues with), PLACEHOLDER(A))")
	val inGraphExp=GRAPH2(PLACEHOLDER("A"), StringExpression("is colleagues with"), PLACEHOLDER("B"))
	val outGraphExp=GRAPH2(PLACEHOLDER("B"), StringExpression("is colleagues with"), PLACEHOLDER("A"))
	val testInGraph=("John", "is colleagues with", "Mary")
	val expectedOutGraph=("Mary", "is colleagues with", "John")


	parsedRule match {
		case a:RULE => println(a)
		  assert(a.condition == inGraphExp)
		  assert(a.input == inGraphExp)
		  assert(a.output == outGraphExp)
		  assert(RuleEngine.transform(a.input, a.output, testInGraph)==expectedOutGraph)
		  assert(RuleEngine.applyRule(a, testInGraph)==expectedOutGraph)
	}

  }

	/*test("graph reverse and replace transform") {
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

	}*/

  test("Parse Composite rules") {

  	val compRuleExp1 = RuleParser.parse("COMPOSITE(GRAPH2(PLACEHOLDER(A), STRING(is a), PLACEHOLDER(B)) AND GRAPH2(PLACEHOLDER(B), StringExpression(is a), PLACEHOLDER(C))): GRAPH2PAIR(GRAPH2(PLACEHOLDER(A), STRING(is a), PLACEHOLDER(B)), GRAPH2(PLACEHOLDER(B), StringExpression(is a), PLACEHOLDER(C))) > GRAPH2(PLACEHOLDER(A), StringExpression(is a), PLACEHOLDER(C))");

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

	compRuleExp1 match {
		case a:RULE => println(a)
		  //assert(a.condition == compositeExp)
		  //assert(a.input == graphPair)
		  assert(a.output == outExp)
		  //assert(RuleEngine.transform(a.input, a.output, testInGraph)==expectedOutGraph)
		  //assert(RuleEngine.applyRule(a, testInGraph)==expectedOutGraph)
	}

		assert(RuleEngine.checkMatch(compositeExp, (testGraph1, testGraph2)))
		assert(RuleEngine.checkMatch(orderedExp, (testGraph1, testGraph2))==false)
		assert(RuleEngine.checkMatch(graphPair, (testGraph1, testGraph2)))
		assert(RuleEngine.transform(graphPair, outExp, (testGraph1, testGraph2))==testGraph3)
		assert(RuleEngine.applyRule(rule1, (testGraph1, testGraph2))==testGraph3)

	}




}