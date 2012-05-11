package com.graphbrain.nlp

import com.graphbrain.braingenerators.ID
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
import com.graphbrain.searchengine.RiakSearchInterface

class SentenceParser (storeName:String = "gb") {

  val quoteRegex = """\".+\"""".r
  val urlRegex = """http:\/\/.+""".r
  val posTagger = new POSTagger();
  val verbRegex = """VB[A-Z]?""".r
  val adverbRegex = """RB[A-Z]?""".r
  val propositionRegex = """IN[A-Z]?""".r
  val nounRegex = """NN[A-Z]?""".r

  val si = RiakSearchInterface("gbsearch")
  
  val store = new VertexStore(storeName) with Indexing with BurstCaching


  def parseSentence(sentence: String, root: Vertex = TextNode(id="None", text="None"), parseType: String = "graph", numResults: Int = 10): List[(List[Vertex], Edge)]={
  	
    if(parseType == "graph") {
      return parseToGraph(sentence, root, numResults);
    }
    else {
    	return Nil
    }
  }

  def parseToGraph(sentence: String, root: Vertex, numResults: Int): List[(List[Vertex], Edge)]={
  	var possibleGraphs:List[(List[Vertex], Edge)] = List()

  	var possibleParses = quoteChunk(sentence, root) ++ posChunk(sentence, root);
  	possibleGraphs = findOrConvertToVertices(possibleParses, root) ++ possibleGraphs;
  	

  	
  	
  	//Highest likelihood is the quote chunk, then the parse option that has the root node as one of the nodes. 
  	//If there is more than one parse option that has the root node as one of the options, the one that has the highest number of positive searches is returned.

    return possibleGraphs.take(numResults);
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

  /*def findOrConvertToVertices(possibleParses: List[(List[String], String)], maxPossiblePerParse: Int = 5, userID:String="gb_anon", rootNode: Vertex): List[(List[Vertex], Edge)]={

  	//possibleParses contains the parses that are consistent with the root being a node from a linguistic point of view
  	var possibleGraphs: List[(List[Vertex], Edge)] = List()

  	rootNode match {
  		case a: TextNode => val rootText = a.text.r;
  		for (g <- possibleParses) {
		    			
  		}
  	}
  	return possibleGraphs
  }*/




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
  }


  def findOrConvertToVertices(possibleParses: List[(List[String], String)], root: Vertex, maxPossiblePerParse: Int = 5, userID:String="gb_anon"): List[(List[Vertex], Edge)]={
	var possibleGraphs:List[(List[Vertex], Edge)] = List()
	for (pp <- possibleParses) {
		pp match {
			case (nodeTexts: List[String], edgeText: String) => 
			var nodesForEachNodeText = new Array[List[Vertex]](nodeTexts.length)
			val edge = Edge(edgeText)
			var edgesForEdgeText: List[Edge] = List()
			var textNum = 0;
			for (nodeText <- nodeTexts) {
				var searchResults = si.query(nodeText);
				var fuzzySearchResults = si.query(nodeText+"*");
				val results = searchResults.ids ++ fuzzySearchResults.ids
				//fuzzy search results are second in priority
				var currentNodesForNodeText:List[Vertex] = List() 
				for(i <- 0 to math.min(maxPossiblePerParse, searchResults.numResults)-1) {
				  val result = try {results(i) } catch { case e => ""}
				  val resultNode = getOrCreateTextNode(id=result, userID=userID, textString=nodeText)

				  currentNodesForNodeText = resultNode :: currentNodesForNodeText;
				}
				nodesForEachNodeText(textNum) = currentNodesForNodeText;
				textNum += 1;

			}
		for (i <- 0 to maxPossiblePerParse-1) {
			//i indexes the search result
			var entryNodes:List[Vertex] = List();
			for(j <- 0 to nodesForEachNodeText.length-1) {
				//j indexes the node text
				val resultSet = nodesForEachNodeText(j)
				entryNodes = resultSet(i) :: entryNodes;
			}
			val entry = (entryNodes, edge)
			possibleGraphs = entry :: possibleGraphs
		}


	  }
	}
	return possibleGraphs.reverse
  }
  

def getOrCreateTextNode(id:String, userID:String, textString:String):Vertex={
  try{
	  return store.get(id);
  }
  catch{
  	  case e => val newNode = TextNode(id=ID.usergenerated_id(userID), text=textString);
	  return newNode;
  }
}

}


object SentenceParser {
  def main(args: Array[String]) {
  	  val sentenceParser = new SentenceParser()
  	  val sentence = args.reduceLeft((w1:String, w2:String) => w1 + " " + w2)
      sentenceParser.parseSentence(sentence)


	}
}
