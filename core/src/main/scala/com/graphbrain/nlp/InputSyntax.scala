package com.graphbrain.nlp

 

object InputSyntax {
	val hashRegex = """#""".r
	val disambigRegex = """(\()(.+?)(\))""".r
	val leftParenthPosTag = """-LRB-""".r
	val rightParenthPosTag = """-RRB-""".r
	val quoteRegex = """(\")(.+?)(\")""".r;
	val urlRegex = """([\d\w]+?:\/\/)?([\w\d\.\-]+)(\.\w+)(:\d{1,5})?(\/\S*)?""".r // See: http://stackoverflow.com/questions/8725312/javascript-regex-for-url-when-the-url-may-or-may-not-contain-http-and-www-words?lq=1
    val urlStrictRegex = """(http|ftp|https):\/\/[\w\-_]+(\.[\w\-_]+)+([\w\-\.,@?^=%&amp;:/~\+#]*[\w\-\@?^=%&amp;/~\+#])?""".r

    def quoteURL(nodeText: String): String = {
    	val urls = urlRegex.findAllIn(nodeText).toArray;
    	var returnText = nodeText;
    	for(url <- urls) {
    		returnText.replace(url, "\""+url+"\"")
    	}
    	return returnText;
    }

	//Returns a list of tuples where the first tuple is the text and the second is the disambiguation if it is there.
	def hashedWords(nodeText: String, disambigs: List[(String, String)], annotatedText: List[(String, String, String)], quoteAnnotatedText: List[(String, String)]): (List[(String, String)], List[(String, String, String)], List[(String, String)]) = {
		var anText = annotatedText;
		var qAnText = quoteAnnotatedText;
		var disAmb = disambigs;
		val disAmbText = nodeText.replace("#", "").trim;
		disAmb = (annotatedText.head._1, disAmbText) :: disAmb;
		//anText and qAnText stay the same
		return (disAmb, anText, qAnText)
		
	}

	def checkParenthClosed(annotatedText: List[(String, String, String)]): Boolean = {
		for(an <- annotatedText) {
			if(rightParenthPosTag.findAllIn(an._2) == 1) {
				return true
			}
		}
		false
	}

	/*def parseURL(url: String, annotatedText: List[(String, String, String)], quoteAnnotatedText: List[(String, String)]: (List[(String, String)], List[(String, String, String)], List[(String, String)]) = {

	}*/

	def  disambig(nodeText: String, disambigs: List[(String, String)], annotatedText: List[(String, String, String)], quoteAnnotatedText: List[(String, String)]): (List[(String, String)], List[(String, String, String)], List[(String, String)]) = {
		var anText = annotatedText;
		var qAnText = quoteAnnotatedText;
		var disAmb = disambigs;
		
		//Check both parentheses present. If only one is present, ignore and just return the remaining annotated text with the left-bracketed text processed.
		if(checkParenthClosed(annotatedText)==false) {
			return (disAmb, anText.tail, qAnText.tail);
		}
		var disAmbText = ""
		while(rightParenthPosTag.findAllIn(anText.head._2).toArray.length!=1) {
			disAmbText += anText.head._1 + " "
			anText = anText.tail;
			qAnText = qAnText.tail
		}
		disAmbText = disAmbText.trim

		//Don't remove closing bracket since it will be handled in loop
		disAmb = (nodeText, disAmbText) :: disAmb;

		return (disAmb, anText, qAnText);
	}
	def resolveURL(urlText: String, annotatedText: List[(String, String, String)], quoteAnnotatedText: List[(String, String)]): List[(String, String, String)] = {
		var anText = annotatedText;
		var qAnText = quoteAnnotatedText;
		var url = urlText
		
		
		
		for(an <- annotatedText) {
			val text = an._1.replace("-LRB-", "(").replace("-RRB-", ")").replace("\\", "");
			for(character <- text) {
				if(character==url.head) {
					url=url.tail;
				}
				else{
					return annotatedText;
				}

			}
			anText = anText.tail;
			if(url.length==0) {
				return anText;
			}


		}

		return anText;
	}

	def urlCheck(text: String): (String, String) = {
		if(urlRegex.findAllIn(text).toArray.length==1) {
      		return (TextFormatting.deQuoteAndTrim(text), "URL")
      		
      	}
      	else {
      		return (TextFormatting.deQuoteAndTrim(text), "NonQuote")
      	}
	}



def quoteAndDisambigTag(text:String): List[(String, String)] = {
  	  var quoteTagged: List[(String, String)] = List();
  	  val quotes = quoteRegex.findAllIn(text).toArray;
      var words = """\s""".r.split(text);
      //Need to make sure the sentence word order is respected so we don't have reparsing in cases of repetition 
      //i.e. if there is a match up to a particular point in the sentence, all non-matches before these points can
      //be safely assumed to be non-quotes and do not have to be checked against the other quotes.
      def emphSyntax(t: String): String={t.replace("(", "( ").replace(")", ") ").replace("#", " #");}
      var textRemaining = text
      var wordsRemaining = words;
  
      for(quote <- quotes) {

    	val currentSplitQuote = """\s""".r.split(quote)
    	val quoteLocation = textRemaining.indexOf(quote)
    	val quoteEnd = quoteLocation + quote.length;
    	if(quoteLocation > 1) {
      	val precedingStrings = """\s""".r.split(textRemaining.substring(0, quoteLocation).trim)
      	for(ps <- precedingStrings) {
      		if(urlRegex.findAllIn(ps).toArray.length==1) {
      		  quoteTagged = (TextFormatting.deQuoteAndTrim(ps), "URL") :: quoteTagged;
      		  wordsRemaining = wordsRemaining.tail
      		}
      		else {

        	  quoteTagged = (TextFormatting.deQuoteAndTrim(emphSyntax(ps)), "NonQuote") :: quoteTagged
        	  wordsRemaining = wordsRemaining.tail;

      		}
      	
      	}

      }
      for(sq <- currentSplitQuote) {
      		quoteTagged = (TextFormatting.deQuoteAndTrim(sq), "InQuote") :: quoteTagged;
        	wordsRemaining = wordsRemaining.tail

      }
      textRemaining = textRemaining.substring(quoteEnd, textRemaining.length)
    }
    for(wr <- wordsRemaining) {
    	if(urlRegex.findAllIn(wr).toArray.length==1) {
      		 quoteTagged = (TextFormatting.deQuoteAndTrim(wr), "URL") :: quoteTagged;
      		 wordsRemaining = wordsRemaining.tail
      	}
      	else {
			quoteTagged = (TextFormatting.deQuoteAndTrim(emphSyntax(wr)), "NonQuote") :: quoteTagged      		
      	}

    }
    return quoteTagged.reverse;
  }
def quoteTag(text:String): List[(String, String)] = {
  	  var quoteTagged: List[(String, String)] = List();
  	  val quotes = quoteRegex.findAllIn(text).toArray;
      var words = """\s""".r.split(text);
      //Need to make sure the sentence word order is respected so we don't have reparsing in cases of repetition 
      //i.e. if there is a match up to a particular point in the sentence, all non-matches before these points can
      //be safely assumed to be non-quotes and do not have to be checked against the other quotes.
      var textRemaining = text.replace("(", "( ").replace(")", ") ").replace("#", " #");
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
        if(urlRegex.findAllIn(sq).toArray.length==1) {
      		quoteTagged = (TextFormatting.deQuoteAndTrim(sq), "URL") :: quoteTagged;
      		wordsRemaining = wordsRemaining.tail
      	}
      	else {
			quoteTagged = (TextFormatting.deQuoteAndTrim(sq), "InQuote") :: quoteTagged;
        	wordsRemaining = wordsRemaining.tail

      	}

      }
      textRemaining = textRemaining.substring(quoteEnd, textRemaining.length)
    }
    for(wr <- wordsRemaining) {
      quoteTagged = (TextFormatting.deQuoteAndTrim(wr), "NonQuote") :: quoteTagged
    }
    return quoteTagged.reverse;
  }
}