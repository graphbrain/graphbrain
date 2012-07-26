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


class Lemmatiser {
	val props = new Properties()
	props.put("annotators", "tokenize, ssplit, pos, lemma");
	val pipeline = new StanfordCoreNLP(props);
	val s = new StanfordLemmatizer();
	val posTagger = new POSTagger();


	/**
	Returns the list of lemmas associated with the words in the string input.
	*/
	def lemmatise(stringToLemmatise:String): List[(String, String)]= {
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

	/**
	Returns the list of parts of speech associated with the words in the string input.
	*/
	def posTag(stringToTag:String): List[(String, String)] = {
		return posTagger.tagText(stringToTag)
	}  

	/**
	Returns a list of annotated words: (word, pos, lemma)
	*/
	def annotate(stringToAnnotate: String): List[(String, String, String)] = {
	  var annotated: List[(String, String, String)] = List();
	  val posTags = posTagger.tagText(stringToAnnotate);
	  val lemmas = lemmatise(stringToAnnotate);
	  if(lemmas.length != posTags.length) {
		return annotated;
	  }
	  else {
	  	var counter = 0;
	    for (entry <- lemmas) {
		  val word = entry._1
		  val lemma = entry._2
		  val pos = posTags(counter)._2
		  annotated = (word, pos, lemma) :: annotated
		  counter = counter + 1;
		}
		return annotated.reverse;

	  }

	}
}
object Lemmatiser {

	def main(args: Array[String])
  	{

  		
  		val l = new Lemmatiser()
    	val annotated = l.annotate("Chih-Chun was a mushroom")

    	for (a <- annotated) {
    		println(a);
    	}
    	




	}

}


