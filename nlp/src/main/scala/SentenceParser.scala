package com.graphbrain.nlp

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.net.URLDecoder;
import scala.collection.immutable.HashMap
import com.graphbrain.hgdb.VertexStore
import com.graphbrain.hgdb.BurstCaching
import com.graphbrain.hgdb.OpLogging
import com.graphbrain.hgdb.TextNode
import com.graphbrain.hgdb.ImageNode
import com.graphbrain.hgdb.Edge
import com.graphbrain.hgdb.SourceNode
import com.graphbrain.hgdb.URLNode
import com.graphbrain.hgdb.SVGNode
import com.graphbrain.hgdb.Vertex
import com.graphbrain.searchengine.Indexing


class SentenceParser {

  val quoteRegex = """\".+\"""".r
  val urlRegex = """http:\/\/.+""".r
  val posTagger = new POSTagger();
  val verbRegex = """VB[A-Z]?""".r
  val adverbRegex = """RB[A-Z]?""".r
  val propositionRegex = """IN[A-Z]?""".r
  val nounRegex = """NN[A-Z]?""".r



  def parseSentence(sentence: String, root: Vertex = TextNode(id="None", text="None"), parseType: String = "graph"): List[(List[Vertex], Edge)]={
  	var possibleGraphs: List[(List[Vertex], Edge)] = List()//Convert to Vertex at end when all searches complete
    if(parseType == "graph") {
      parseToGraph(sentence, root);
    }
    return possibleGraphs
  }

  def parseToGraph(sentence: String, root: Vertex): List[(List[String], String)]={
  	var possibleParses: List[(List[String], String)] = List()
    possibleParses ++= posChunk(sentence, root)	
    return possibleParses
  }

  def quoteChunk(sentence: String, root: Vertex): List[(List[String], String)]={
  	//Find the quotes:
  	var possibleParses: List[(List[String], String)] = List()

  	//Make sure the quotation marks are not around the whole string:
  	quoteRegex.findFirstIn(sentence) match {
  		case Some(exp) => 
  		//I'm assigning these in case we want to do something with them later, e.g. infer hypergraphs vs. multi-bi-graphs
  		val numQuotes = quoteRegex.findAllIn(sentence).length;
  		val numNonQuotes = quoteRegex.split(sentence).length;

  		if (exp.length == sentence.length) {
	  	  return possibleParses;
	  	}
	  	
  		
  		else if (numQuotes >=2 && numNonQuotes == numNonQuotes-1) {
  		  val nodes = quoteRegex.findAllIn(sentence);
  		  val edges = quoteRegex.split(sentence);
  		  var nodeTexts:List[String] = List();
  		  for(i <- 0 to nodes.length-2) {
  		  	val current = nodes.next;
  		  	val next = nodes.next;
  		  	val edge = edges(i);
  		  	possibleParses = (List(current, next), edge) :: possibleParses; 

  		  }
  		  return possibleParses;
		} 
  		case None => return possibleParses;
  	}

  	return possibleParses

  }

  def graphChunk(sentence: String, root: Vertex): List[(List[String], String)]={
  	var possibleParses: List[(List[String], String)] = List()
  	return possibleParses
  }

  def posChunk(sentence: String, root: Vertex): List[(List[String], String)]={
    val taggedSentence = posTagger.tagText(sentence)
    var possibleParses: List[(List[String], String)] = List()
    for(i <- 0 to taggedSentence.length-3) {
      

  	  val current = taggedSentence(i)
  	  
  	  val lookahead1 = taggedSentence(i+1)
  	  val lookahead2 = taggedSentence(i+2)
      
      (current, lookahead1, lookahead2) match{
    		  case ((word1, tag1), (word2, tag2), (word3, tag3)) => 
    		  println(word2 + ", " + tag2)
    		  if(verbRegex.findAllIn(tag2).length == 1) {
    		  	println("verb: " + word2)
    		  
    		  if(verbRegex.findAllIn(tag1).length == 0 && verbRegex.findAllIn(tag3).length == 0 && adverbRegex.findAllIn(tag3).length == 0) {
    		  	  val edgeindex = i+1
    		  	  //Anything before the edge goes into node 1
    		  	  var node1Text = ""
    		  	  var edgeText = word2
    		  	  var node2Text = ""
    		  	  for (j <- 0 to i) {
    		  	  	taggedSentence(j) match {
    		  	  		case (word, tag) => node1Text = node1Text + word + " "

    		  	  	}

    		  	  }
    		  	  for (j <- i+2 to taggedSentence.length-1) {
    		  		taggedSentence(j) match {
    		  	  		case (word, tag) => node2Text = node2Text + word + " "

    		  	  	}
    		  	  }
    		  	  println(node1Text + " ," + edgeText + " ," + node2Text)
    		  	  val nodes = List(node1Text, node2Text)
    		  	  possibleParses = (nodes, edgeText) :: possibleParses
    		  	}
				    		  	
    		  }


    	  }
    }
    return possibleParses.reverse;
    //Node[Non-verb], Edge[verb/two verbs of different tenses], Node[non-verb/verb of same tense as previous verb] 
    //> 

    //
  }
  def findOrConvertToVertices(possibleParses: List[(List[String], String)]): List[(List[Vertex], Edge)]={
	var possibleGraphs:List[(List[Vertex], Edge)] = List()
	return possibleGraphs
  }
  
}




object SentenceParser {
  def main(args: Array[String]) {
  	  val sentenceParser = new SentenceParser()
  	  val sentence = args.reduceLeft((w1:String, w2:String) => w1 + " " + w2)
      sentenceParser.parseSentence(sentence)


	}
}