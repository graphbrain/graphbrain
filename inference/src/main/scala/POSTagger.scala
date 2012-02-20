package com.graphbrain.inference

import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

object POSTagger {

	def tagText(stringToTag:String):List[(String, String)]=
	{
		return taggedTokens(addTagToText(stringToTag))
	}

	def getTokenSequence(stringToTag:String):String=
	{
		var tokenSeq = ""
		val tokenTags=tagText(stringToTag)
		for (tokenTag <- tokenTags)
		{
			tokenTag match {
				case (word, tag) => tokenSeq=tokenSeq+tag+" ";
			}
		}
		return tokenSeq.trim;
	}

	//Returns the tagged tokens as a list of (word, tag) tuples.
	private def taggedTokens(taggedString:String):List[(String, String)]=
	{
		var taggedTokens:List[(String, String)]=List()
		val wordTagPairs = taggedString.split(" ")	
		for (wordTag <- wordTagPairs)
		{
			taggedTokens=(wordTag.split("/")(0), wordTag.split("/")(1))::taggedTokens
		}
		return taggedTokens.reverse
	}

	private def addTagToText(stringToTag:String):String=
	{
		val tagger = new MaxentTagger("pos_models/english-left3words-distsim.tagger");
		return(tagger.tagString(stringToTag))
	}


  
	def main(args: Array[String])
  	{

    	val taggedToks=tagText("John's dog")


    	for(tag <- taggedToks)
    	{
    		tag match{
    			case (a,b) => println (a + ", " +  b)
    		}
    	}
    	println(getTokenSequence("John's dog"))


	}

}
