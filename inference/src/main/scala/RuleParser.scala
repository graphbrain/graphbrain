package com.graphbrain.inference

object RuleParser{

	/*val RuleRegex=""".+\(.+\):.+\(.+\)>.+\(.+\)""".r
	val POSRegex="""POS\(.+\)""".r
	val StringExpressionRegex="""STRING\(.+\)""".r
	val REGEXRegex="""REGEX\(.+\)""".r
	val PLACEHOLDERRegex="""PLACEHOLDER\(.+\)""".r
	val GRAPH2Regex="""GRAPH2\(.+\)""".r
	val COMPOSITERegex="""\(.+\)""".r
	val GRAPH2PAIRRegex="""\(.+\)""".r


	
	def parse(expString:String):RuleExpression={
		
		if(RuleRegex.findAllIn(ruleString).length==1){
			return parseRule(expString);
		}
		else if(POSRegex.findAllIn(ruleString).length==1){
			return parsePOS(expString);
		}
		else if(StringExpressionRegex.findAllIn(ruleString).length==1){
			return parseString(expString);
		}
		else if(REGEXRegex.findAllIn(ruleString).length==1){
			return parseREGEX(expString);
		}
		else if(PLACEHOLDERRegex.findAllIn(ruleString).length==1){
			return parsePLACEHOLDER(expString);
		}
		else if(GRAPH2Regex.findAllIn(ruleString).length==1){
			return parseGRAPH2(expString);
		}
		else if(COMPOSITERegex.findAllIn(ruleString).length==1){
			return parseCOMPOSITE(expString);
		}
		else if(GRAPH2PAIRRegex.findAllIn(ruleString).length==1){
			return parseGRAPH2PAIR(expString);
		}

	}

	def parseRule(ruleString:String):Rule={
		
		
		
		val rule = ruleString.next;
		val condString = rule.split(":")(0)
		val sourceString = rule.split(":")(1).split(">")(0)
		val targetString = rule.split(":")(1).split(">")(1)	
		val condition =
		val source = 
		val target =
		return Rule(condition, source, target)

  }

  def parseREGEX(expString:String):REGEX={
  	
  }

  def parsePOS(expString:String):POS={
  	
  }

  def parseGRAPH2(expString:String):GRAPH2={
  	
  }

  def parsePLACEHOLDER(expString:String):PLACEHOLDER={
  	
  }

  def parseCOMPOSITE(expString:String):COMPOSITE={
  	
  }

  def parseString(expString:String):StringExpression={
  	
  }

  def parseGRAPH2PAIR(expString:String):GRAPH2PAIR={
  	
  }*/


	
}
