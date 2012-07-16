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


	def lemmatise(stringToLemmatise:String):List[String]=
	{
		val lemmas=s.lemmatize("am a tree");
		return scala.collection.JavaConversions.asScalaBuffer(lemmas).toList
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


