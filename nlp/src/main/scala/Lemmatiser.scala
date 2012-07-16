package com.graphbrain.nlp

import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.pipeline._;
import edu.stanford.nlp.util._;
import edu.stanford.nlp.ling.CoreLabel;
import java.util.Properties;
import edu.stanford.nlp.ling.CoreAnnotations._;
import scala.collection.JavaConversions._;


object Lemmatiser {
	val props = new Properties()
	props.put("annotators", "tokenize, ssplit, pos, lemma");
	val pipeline = new StanfordCoreNLP(props);
	val s = new StanfordLemmatizer();
	val posTagger = new POSTagger();


	def lemmatise(stringToLemmatise:String): List[(String, String)]=
	{
		var lemmatised:List[(String, String)]=List()
		val lemmas=scala.collection.JavaConversions.asScalaBuffer(s.lemmatize(stringToLemmatise, 0)).toList;
		val words = scala.collection.JavaConversions.asScalaBuffer(s.lemmatize(stringToLemmatise, 1)).toList;

		if(lemmas.length==words.length) {

		  for(i <- 0 to (words.length-1)) {
		    lemmatised = (words(i), lemmas(i)) :: lemmatised
		  }

		}

		return lemmatised.reverse;
	}

	def posTag(stringToTag:String): List[(String, String)] =
	{
		return posTagger.tagText(stringToTag)
	}  


	def main(args: Array[String])
  	{

  		
    	val lemmas = lemmatise("am a tree")
    	//val sLemmas = scala.collection.JavaConversions.asBuffer(lemmas)
    	for (lemma <- lemmas)
    	{
    		println(lemma)
    		
    	}
    	




	}

}


