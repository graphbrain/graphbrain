import org.scalatest.FunSuite


class RuleTest extends FunSuite {

	val posExpression = new POS("PRP VBP DT NN") 
	val regexExpression = new REGEX(".*(is\\sa).*")
	val graph2Expression = new GRAPH2("source", "relation", "target")
	val pos_regex_graph2 = new RULE(posExpression, regexExpression, graph2Expression)
	
	

	test("Initialise POS:REGEX->GRAPH2 rule"){		
		
		assert(pos_regex_graph2.condition==posExpression)
		assert(pos_regex_graph2.input==regexExpression)
		assert(pos_regex_graph2.output==graph2Expression)
	}

	test("checkMatch for different rule expression types"){
		
		assert(RuleEngine.checkMatch(posExpression, "I am a person"))
		assert(RuleEngine.checkMatch(regexExpression, "Tom is a apple"))
		assert(RuleEngine.checkMatch(graph2Expression, ("source", "relation", "target")))

	}

	test("text replacement transform"){
		val condition = REGEX(".*(is\\sa).*")
		val toReplace = REGEX("is a")
		val replaceBy = REGEX("is an")



		val testSentence = "Tom is a apple"
		val expectedOutput="Tom is an apple"
		assert(RuleEngine.transform(toReplace, replaceBy, testSentence)==expectedOutput)

	}

	test("graph reciprocate transform") {
		//"A", "is colleagues with", "B", "B", "is colleagues with", "A"
		val inGraphExp=GRAPH2("A", "is colleagues with", "B")
		val outGraphExp=GRAPH2("B", "is colleagues with", "A")

		val testInGraph=GRAPH2("John", "is colleagues with", "Mary")
		val expectedOutGraph=("Mary", "is colleagues with", "John")
		assert(RuleEngine.transform(inGraphExp, outGraphExp, testInGraph)==expectedOutGraph)

	}

	test("graph reverse and replace transform") {
		//"A", "has a", "B", "B", "belongs to", "A"
		val inGraphExp=GRAPH2("A", "has a", "B")
		val outGraphExp=GRAPH2("B", "belongs to", "A")

		val testInGraph=GRAPH2("John", "has a", "motorbike")
		val expectedOutGraph=("motorbike", "belongs to", "John")
		assert(RuleEngine.transform(inGraphExp, outGraphExp, testInGraph)==expectedOutGraph)

	}

	test("graph simple replace transform") {
		//"A", "hates", "B", "A", "does not like", "B"
		val inGraphExp=GRAPH2("A", "hates", "B")
		val outGraphExp=GRAPH2("A", "does not like", "B")

		val testInGraph=GRAPH2("John", "hates", "Mary")
		val expectedOutGraph=("John", "does not like", "Mary")
		assert(RuleEngine.transform(inGraphExp, outGraphExp, testInGraph)==expectedOutGraph)

	}

}