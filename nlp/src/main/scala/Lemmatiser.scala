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

//import edu.northwestern.at.utils.corpuslinguistics.inflector.conjugator.EnglishConjugator;



class Lemmatiser {
	val urlRegex = """([\d\w]+?:\/\/)?([\w\d\.\-]+)(\.\w+)(:\d{1,5})?(\/\S*)?""".r // See: http://stackoverflow.com/questions/8725312/javascript-regex-for-url-when-the-url-may-or-may-not-contain-http-and-www-words?lq=1
	val props = new Properties()
	props.put("annotators", "tokenize, ssplit, pos, lemma");
	val pipeline = new StanfordCoreNLP(props);
	val s = new StanfordLemmatizer();
	val posTagger = new POSTagger();
	val conjugator = new EnglishConjugator();
	


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
  def conjugate(stemToConjugate: String, tense: VerbTense = VerbTense.PRESENT, person: Person = Person.THIRD_PERSON_SINGULAR): String = {
	return conjugator.conjugate(stemToConjugate, tense, person);
  }
}



object Lemmatiser {

	def main(args: Array[String])
  	{

  		val clSentence = args.reduceLeft((w1:String, w2:String) => w1 + " " + w2)
  		//val s = "Telmo Menezes has http://telmomenezes.com"
  		val s = "like"
  		val l = new Lemmatiser()
  		println("From main: " + s)

    	val annotated = l.annotate(s)
    	val annotatedCL = l.annotate(clSentence)

    	for (a <- annotated) {
    		println(a);
    	}
    	println("From command line: " + clSentence)

    	for ( a <- annotatedCL) {
    		println(a)
    		println(l.conjugate(a._3))
    	}




	}

}


