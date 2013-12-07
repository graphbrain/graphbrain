package com.graphbrain.nlp

import edu.stanford.nlp.tagger.maxent.MaxentTagger

class POSTagger {

	val tagger = new MaxentTagger("pos_models/english-left3words-distsim.tagger")
  //val tagger = new MaxentTagger("pos_models/french.tagger")

	def tagText(stringToTag: String): List[(String, String)] =
		taggedTokens(addTagToText(stringToTag))

	def getTokenSequence(stringToTag: String): String = {
		var tokenSeq = ""
		val tokenTags=tagText(stringToTag)
		for (tokenTag <- tokenTags) {
			tokenTag match {
				case (word, tag) => tokenSeq = tokenSeq + tag + " "
			}
		}
		tokenSeq.trim
	}

	//Returns the tagged tokens as a list of (word, tag) tuples.
	private def taggedTokens(taggedString:String):List[(String, String)]=
	{
		var taggedTokens:List[(String, String)]=List()
		val wordTagPairs = taggedString.split(" ")	
		for (wordTag <- wordTagPairs) {
			//println(wordTag);
			taggedTokens=(wordTag.split("_")(0), wordTag.split("_")(1))::taggedTokens
		}
		taggedTokens.reverse
	}

	private def addTagToText(stringToTag: String): String =
	  tagger.tagString(stringToTag)
}

object POSTagger {
	def main(args: Array[String]) = {
    val taggerTest = new POSTagger()

    val taggedToks = taggerTest.tagText("John's dog")


    for(tag <- taggedToks) {
    	tag match{
    		case (a,b) => println (a + ", " +  b)
    	}
    }
    println(taggerTest.getTokenSequence("John's dog"))
	}
}


