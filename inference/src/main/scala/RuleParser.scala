package com.graphbrain.inference

object RuleParser{

	val RuleRegex=""".+\(.+\):\s*.+\(.+\)\s*>\s*.+\(.+\)""".r
	val POSRegex="""POS\(.+\)""".r
	val StringExpressionRegex="""STRING\(.+\)""".r
	val REGEXRegex="""REGEX\(.+\)""".r
	val PLACEHOLDERRegex="""PLACEHOLDER\(.+\)""".r
	val GRAPH2Regex="""GRAPH2\(.+\)""".r
	val COMPOSITERegex="""COMPOSITE\(.+\)""".r
	val GRAPH2PAIRRegex="""GRAPH2PAIR\(.+\)""".r
	val graphRegex="""\(?.+\)""".r
	val twoGraphRegex="""\(.+\),\s*\(.+\)""".r


	def parseInput(inString:String):Any = {

		if (twoGraphRegex.findAllIn(inString).length==1) {
			val graphs=inString.drop(1).dropRight(1).split(",");
			return (parseInGraph(graphs(0).trim), parseInGraph(graphs(1).trim))
		}
		if (graphRegex.findAllIn(inString).length==1) {
			return parseInGraph(inString)
		}
		else {
			return inString;
		}
	}

	def parseInGraph(inString:String):(String, String, String) = {
		val parts = inString.drop(1).dropRight(1).split(",");
		val in = parts(0).trim
		val rel = parts(1).trim
		val out = parts(2).trim
		return (in, rel, out)
	}
	
	def parse(expString:String):RuleExpression={
		
	  if(RuleRegex.findAllIn(expString).length==1) {
			return parseRule(expString);
		}
		else if (COMPOSITERegex.findAllIn(expString).length == 1) {
			return parseCOMPOSITE(expString);
		}
		else if (GRAPH2PAIRRegex.findAllIn(expString).length==1) {
			return parseGRAPH2PAIR(expString);
		}

		else if (GRAPH2Regex.findAllIn(expString).length==1){
			return parseGRAPH2(expString);
		}
		else if(POSRegex.findAllIn(expString).length==1){
			return parsePOS(expString);
		}
		else if(StringExpressionRegex.findAllIn(expString).length==1){
			return parseString(expString);
		}
		else if(REGEXRegex.findAllIn(expString).length==1){
			return parseREGEX(expString);
		}
		else if (PLACEHOLDERRegex.findAllIn(expString).length==1){
			return parsePLACEHOLDER(expString);
		}
		else {
			return StringExpression("FAILED")
		}

	}

	def parseRule(ruleString:String):RULE={
		
		val condString = ruleString.split(":")(0)
		val sourceString = ruleString.split(":")(1).split(">")(0)
		val targetString = ruleString.split(":")(1).split(">")(1)	
		val condition = parse(condString)
		val source = parse(sourceString)
		val target =parse(targetString)
		return RULE(condition, source, target)

  }

  def parseREGEX(expString:String):REGEX={
  	
  	return REGEX(expString.split("REGEX")(1).drop(1).dropRight(1))

  }

  def parsePOS(expString:String):POS={
  	return POS(expString.split("POS\\(")(1).split("\\)")(0))	
  }

  def parseGRAPH2(expString:String):GRAPH2={
  	val graph2=expString.split("GRAPH2")(1).drop(1).dropRight(1).split(",")
  	val source = graph2(0).trim
  	val relation = graph2(1).trim
  	val target = graph2(2).trim
  	return GRAPH2(parse(source), parse(relation), parse(target))
  }

  def parsePLACEHOLDER(expString:String):PLACEHOLDER={
  	return PLACEHOLDER(expString.split("PLACEHOLDER\\(")(1).split("\\)")(0))
  }

  def parseCOMPOSITE(expString: String):COMPOSITE={
  	val composite = expString.split("COMPOSITE")(1).drop(1).dropRight(1).split(",")
  	val exp1 = composite(0).trim
  	val rel = composite(1).trim
  	val exp2 = composite(2).trim
  	return COMPOSITE(parse(exp1), rel, parse(exp2))
  }

  def parseString(expString:String):StringExpression = {
  	return StringExpression(expString.split("STRING\\(")(1).split("\\)")(0))
  }

  def parseGRAPH2PAIR(expString: String):GRAPH2PAIR={
  	val composite = expString.split("GRAPH2PAIR")(1).drop(1).dropRight(1).split(",")
  	val g1=composite(0).trim
  	val g2=composite(1).trim
  	return GRAPH2PAIR(parseGRAPH2(g1), parseGRAPH2(g2))
  }

 


	def main(args: Array[String])
  	{
  		//val test="POS(ABC): STRING(Hello)>STRING(GOODBYE)"
  		//val test = parse(args(0));
  		val test="POS(PRP VBP DT NN): REGEX(.*(is\\sa).*)>GRAPH2(PLACEHOLDER(source), StringExpression(relation), PLACEHOLDER(target))"

    	println("Rule expressions: " + parse(test))

    	if(args.length==2)
    	{
    		println("Input: " + args(1))
    		val input = parseInput(args(1))
    		val rule = parse(test)
    		println("Parsed input: " + input);
    		println("Rule expresssions: " + rule)
    		rule match {
    			case r:RULE => println("Output: " + RuleEngine.applyRule(r, input))
    		}
    		
    	}


	}
	
}
