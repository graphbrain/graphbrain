package com.graphbrain.nlp

object InputSyntax {
	val hashRegex = """#""".r
	val disambigRegex = """(\()(.+?)(\))""".r
	val leftParenthPosTag = """LRB""".r
	val rightParenthPosTag = """RRB""".r
	val quoteRegex = """(\")(.+?)(\")""".r;

	//Returns a list of tuples where the first tuple is the text and the second is the disambiguation if it is there.
	def hashedWords(text: String): List[(String, String)] = {

		val words = """\s""".r.split(text);
		return Nil
	}

	def checkParenthClosed(annotatedText: List[(String, String)]): Boolean = {
		for(an <- annotatedText) {
			if(rightParenthPosTag.findAllIn(an._2)==1) {
				return true;
			}
		}
		return false;
	}

	def  disambig(nodeText: String, disambigs: List[(String, String)], annotatedText: List[(String, String)], quoteAnnotatedText: List[(String, String)]): (List[(String, String)], List[(String, String)], List[(String, String)]) = {
		var anText = annotatedText;
		var qAnText = quoteAnnotatedText;
		var disAmb = disambigs;
		
		//Check both parentheses present. If only one is present, ignore and just return the remaining annotated text with the left-bracketed text processed.
		if(checkParenthClosed(annotatedText)==false) {
			return (disAmb, anText.tail, qAnText.tail);
		}
		var disAmbText = ""
		while(rightParenthPosTag.findAllIn(anText.head._2)!=1) {
			disAmbText += anText.head + " "
			anText = anText.tail;
			qAnText = qAnText.tail
		}
		disAmbText = disAmbText.trim

		//Don't remove closing bracket since it will be handled in loop
		disAmb = (nodeText, disAmbText) :: disAmb;

		return (disAmb, anText, qAnText);
	}

	def quoteTag(text:String): List[(String, String)] = {
  	  var quoteTagged: List[(String, String)] = List();
  	  val quotes = quoteRegex.findAllIn(text).toArray;
      var words = """\s""".r.split(text);
      //Need to make sure the sentence word order is respected so we don't have reparsing in cases of repetition 
      //i.e. if there is a match up to a particular point in the sentence, all non-matches before these points can
      //be safely assumed to be non-quotes and do not have to be checked against the other quotes.
      var textRemaining = text;
      var wordsRemaining = words;
  
      for(quote <- quotes) {

    	val currentSplitQuote = """\s""".r.split(quote)
    	val quoteLocation = textRemaining.indexOf(quote)
    	val quoteEnd = quoteLocation + quote.length;
    	if(quoteLocation > 1) {
      	val precedingStrings = """\s""".r.split(textRemaining.substring(0, quoteLocation).trim)
      	for(ps <- precedingStrings) {
        	quoteTagged = (TextFormatting.deQuoteAndTrim(ps), "NonQuote") :: quoteTagged
        	wordsRemaining = wordsRemaining.tail;
      	}

      }
      for(sq <- currentSplitQuote) {
        quoteTagged = (TextFormatting.deQuoteAndTrim(sq), "InQuote") :: quoteTagged
        wordsRemaining = wordsRemaining.tail
      }
      textRemaining = textRemaining.substring(quoteEnd, textRemaining.length)
    }
    for(wr <- wordsRemaining) {
      quoteTagged = (TextFormatting.deQuoteAndTrim(wr), "NonQuote") :: quoteTagged
    }
    return quoteTagged.reverse;
  }

def quoteAndParenthTag(text:String): List[(String, String)] = {
  	  var quoteTagged: List[(String, String)] = List();
  	  val quotes = quoteRegex.findAllIn(text).toArray;
      var words = """\s""".r.split(text);
      //Need to make sure the sentence word order is respected so we don't have reparsing in cases of repetition 
      //i.e. if there is a match up to a particular point in the sentence, all non-matches before these points can
      //be safely assumed to be non-quotes and do not have to be checked against the other quotes.
      var textRemaining = text.replace("(", "( ").replace(")", ") ");
      var wordsRemaining = words;
  
      for(quote <- quotes) {

    	val currentSplitQuote = """\s""".r.split(quote)
    	val quoteLocation = textRemaining.indexOf(quote)
    	val quoteEnd = quoteLocation + quote.length;
    	if(quoteLocation > 1) {
      	val precedingStrings = """\s""".r.split(textRemaining.substring(0, quoteLocation).trim)
      	for(ps <- precedingStrings) {
        	quoteTagged = (TextFormatting.deQuoteAndTrim(ps), "NonQuote") :: quoteTagged
        	wordsRemaining = wordsRemaining.tail;
      	}

      }
      for(sq <- currentSplitQuote) {
        quoteTagged = (TextFormatting.deQuoteAndTrim(sq), "InQuote") :: quoteTagged
        wordsRemaining = wordsRemaining.tail
      }
      textRemaining = textRemaining.substring(quoteEnd, textRemaining.length)
    }
    for(wr <- wordsRemaining) {
      quoteTagged = (TextFormatting.deQuoteAndTrim(wr), "NonQuote") :: quoteTagged
    }
    return quoteTagged.reverse;
  }

}