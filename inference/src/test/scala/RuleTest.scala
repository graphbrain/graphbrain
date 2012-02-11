import org.scalatest.FunSuite


class RuleTest extends FunSuite {

	val posExpression = new POS("(POS)") 
	val regexExpression = new REGEX(".*(is\\sa).*")
	val graph2Expression = new GRAPH2("source", "relation", "target")
	val pos_regex_graph2 = new RULE(posExpression, regexExpression, graph2Expression)
	
	

	test("Initialise POS:REGEX->GRAPH2 rule"){		
		
		assert(pos_regex_graph2.condition==posExpression)
		assert(pos_regex_graph2.input==regexExpression)
		assert(pos_regex_graph2.output==graph2Expression)
	}

	test("checkMatch for different rule expression types"){
		
		assert(RuleEngine.checkMatch(posExpression, "POS"))
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

}