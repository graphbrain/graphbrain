package com.graphbrain.inference

object RuleParser{

	val RuleRegex=""".+\(.+\):.+\(.+\)>.+\(.+\)""".r
	val POSRegex="""POS\(.+\)""".r
	val StringExpressionRegex="""STRING\(.+\)""".r
	val REGEXRegex="""REGEX\(.+\)""".r
	val PLACEHOLDERRegex="""PLACEHOLDER\(.+\)""".r
	val GRAPH2Regex="""GRAPH2\(.+\)""".r
	val COMPOSITERegex="""\(.+\)""".r
	val GRAPH2PAIRRegex="""\(.+\)""".r


	

	def parse(ruleString:String):Rule={
		val ruleString = RuleRegex.findAllIn(ruleString);
		if(ruleString.length==1)
		{
			val rule = ruleString.next;
			val cond = rule.split(":")(0)
			val source = rule.split(":")(1).split(">")(0)
			val target = rule.split(":")(1).split(">")(1)

			
		}

  }
	
}
