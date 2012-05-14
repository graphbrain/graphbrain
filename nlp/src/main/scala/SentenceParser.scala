package com.graphbrain.nlp

import com.graphbrain.braingenerators.ID
import java.net.URLDecoder;
import scala.collection.immutable.HashMap
import com.graphbrain.hgdb.VertexStore
import com.graphbrain.hgdb.BurstCaching
import com.graphbrain.hgdb.OpLogging
import com.graphbrain.hgdb.TextNode
import com.graphbrain.hgdb.ImageNode
import com.graphbrain.hgdb.Edge
import com.graphbrain.hgdb.URLNode
import com.graphbrain.hgdb.Vertex
import com.graphbrain.searchengine.Indexing
import com.graphbrain.searchengine.RiakSearchInterface

class SentenceParser (storeName:String = "gb") {

  val quoteRegex = """(\")(.+?)(\")""".r
  val urlRegex = """http:\/\/.+""".r
  val posTagger = new POSTagger();
  val verbRegex = """VB[A-Z]?""".r
  val adverbRegex = """RB[A-Z]?""".r
  val propositionRegex = """IN[A-Z]?""".r
  val nounRegex = """NN[A-Z]?""".r

  val si = RiakSearchInterface("gbsearch")
  
  val store = new VertexStore(storeName) with Indexing with BurstCaching


  def parseSentence(inSentence: String, root: Vertex = TextNode(id="None", text="None"), parseType: String = "graph", numResults: Int = 10): List[(List[Vertex], Edge)]={
  	val sentence = inSentence.trim;
  	//I'm envisioning that in the future we may have other parsing purposes where we might have other parsing rules 
  	//(e.g. ignoring non-rootparses, different rule precedences) so I've kept the graph creation parsing as just one option.
    if(parseType == "graph") {
      return parseToGraph(sentence, root, numResults);
    }
    else {
    	return Nil
    }
  }

  def parseToGraph(sentence: String, root: Vertex, numResults: Int): List[(List[Vertex], Edge)]={

  	var possibleParses = quoteChunk(sentence, root) ++ posChunk(sentence, root);
  	var possibleGraphs = findOrConvertToVertices(possibleParses, root, numResults);
  	
    return possibleGraphs.take(numResults);
  }


  def quoteChunk(sentence: String, root: Vertex): List[(List[String], String)]={
  	//Find the quotes:
  	var possibleParses: List[(List[String], String)] = List()

  	
  	quoteRegex.findFirstIn(sentence) match {

  		case Some(exp) => 
  		
  		//Make sure the quotation marks are not around the whole string:
  		  //I'm assigning these in case we want to do something with them later, e.g. infer hypergraphs vs. multi-bi-graphs
  		  val numQuotes = quoteRegex.findAllIn(sentence).length;
  		  
  		  var nonQuotes = quoteRegex.split(sentence);
  		  var nqEdges:List[String] = List()
  		  var numNonQuotes = 0;
  		  
  		  for (i <- 0 to nonQuotes.length-1) {
  		  	val nq = nonQuotes(i)
  		  	if(nq!="") {
  		  		numNonQuotes+=1;
  		  		nqEdges = nq::nqEdges;
  		  	}
  		  }
  		  
  		  if (exp.length == sentence.length) {
	  	    return Nil;
	  	  }
  		  else if (numQuotes >=2 && numNonQuotes == numQuotes-1) {
  		    val nodes = quoteRegex.findAllIn(sentence).toArray;
  		    val edges = nqEdges.reverse.toArray
  		    
  		    for(i <- 0 to nodes.length-2) {
  		  	  val current = nodes(i);
  		  	  val next = nodes(i+1);
  		  	  val edge = edges(i);
  		  	  possibleParses = (List(current, next), edge) :: possibleParses; 
  		  	  //println("Quote Chunk: " + current + ", " + edge + ", " + next)

  		    }
  		    return possibleParses;
		} 
  		case None => return possibleParses;
  	}
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
    		  //println(word2 + ", " + tag2)
    		  if(verbRegex.findAllIn(tag2).length == 1) {
    		  	//println("verb: " + word2)
    		  
    		  if(verbRegex.findAllIn(tag1).length == 0 && verbRegex.findAllIn(tag3).length == 0 && adverbRegex.findAllIn(tag3).length == 0) {
    		  	  val edgeindex = i+1
    		  	  //Anything before the edge goes into node 1
    		  	  var node1Text = ""
    		  	  var edgeText = word2
    		  	  var node2Text = ""
    		  	  for (j <- 0 to i) {
    		  	  	taggedSentence(j) match {
    		  	  		case (word, tag) => node1Text = node1Text + " " + word

    		  	  	}

    		  	  }
    		  	  for (j <- i+2 to taggedSentence.length-1) {
    		  		taggedSentence(j) match {
    		  	  		case (word, tag) => node2Text = node2Text + " " + word 

    		  	  	}
    		  	  }
    		  	  //println("POS Chunk: " + node1Text + " ," + edgeText + " ," + node2Text)
    		  	  val nodes = List(node1Text, node2Text)
    		  	  possibleParses = (nodes, edgeText) :: possibleParses

    		  	}
				    		  	
    		  }


    	  }
    }
    return possibleParses.reverse;
  }


  def findOrConvertToVertices(possibleParses: List[(List[String], String)], root: Vertex, maxPossiblePerParse: Int = 10, userID:String="gb_anon"): List[(List[Vertex], Edge)]={

	var possibleGraphs:List[(List[Vertex], Edge)] = List()
	val sortedParses = sortRootParsesPriority(possibleParses, root)
	
	for (pp <- sortedParses) {
		pp match {
			case (nodeTexts: List[String], edgeText: String) => 
			var nodesForEachNodeText = new Array[List[Vertex]](nodeTexts.length)
			
			var edgesForEdgeText: List[Edge] = List()
			var textNum = 0;
			for (nodeText <- nodeTexts) {

				var searchResults = try{si.query(nodeText).ids} catch {case e => Nil};
				var fuzzySearchResults = try{si.query(nodeText+"*").ids} catch{case e => Nil};
				val results = try{searchResults ++ fuzzySearchResults} catch {case e => Nil}
				
				//fuzzy search results are second in priority
				var currentNodesForNodeText:List[Vertex] = List() 
				val limit = if (maxPossiblePerParse < results.length) maxPossiblePerParse else results.length + 1;
				for(i <- 0 to limit-1) {
				  val result = try {results(i) } catch { case e => ""}
				  val resultNode = getOrCreateTextNode(id=result, userID=userID, textString=nodeText)
				  println("Node: " + resultNode.id)

				  currentNodesForNodeText = resultNode :: currentNodesForNodeText;
				}
				nodesForEachNodeText(textNum) = currentNodesForNodeText;
				textNum += 1;

			}

		    for (i <- 0 to nodesForEachNodeText(0).length-1) {
		      var entryNodes:List[Vertex] = List();
			  var entryIDs:List[String] = List();
			  entryNodes = nodesForEachNodeText(0)(i) :: entryNodes;
			  entryNodes = nodesForEachNodeText(1)(i) :: entryNodes;
			  entryIDs = nodesForEachNodeText(0)(i).id :: entryIDs
			  entryIDs = nodesForEachNodeText(1)(i).id :: entryIDs

			  val edge = new Edge(ID.relation_id(edgeText), entryIDs.reverse.toArray)
			  println("Edge: " + edge.id)
			  val entry = (entryNodes, edge)
			  
			  possibleGraphs = entry :: possibleGraphs
			 }
			  
			  
		   }

	    }
	    return possibleGraphs.reverse
	}
	
  
  
  /**
Sorts the parses so that only the ones consistent with the root node being one of the nodes is returned
If returnAll is false, only return the ones that satisfy the root as node constraint, if true, return all results sorted
*/
  def sortRootParsesPriority(possibleParses: List[(List[String], String)], rootNode: Vertex, returnAll: Boolean = true): List[(List[String], String)]={

  	//possibleParses contains the parses that are consistent with the root being a node from a linguistic point of view
  	var rootParses: List[(List[String], String)] = List()
  	var optionalParses: List[(List[String], String)] = List()
  	rootNode match {
  		case a: TextNode => val rootText = a.text.r;
  		for (g <- possibleParses) {
  			g match {
  				case (nodeTexts: List[String], edgeText: String) => 
  				  for(nodeText <- nodeTexts) {
  				  	if (nodeText==rootText) {
  				  		rootParses = g::rootParses
  				  	}
  				  	else {
  				  		 optionalParses = g::optionalParses
  				  	}
  				  }
  			}
		  //Check whether rootText matches one of the node texts:
		  	    			
  		}
  	  
  	}
  	if(returnAll) {
  	    return rootParses.reverse++optionalParses.reverse
  	  }
  	  else {
  		return rootParses.reverse;
  	  }
  }


def getOrCreateTextNode(id:String, userID:String, textString:String):Vertex={
  try{
	  return store.get(id);
  }
  catch{
  	  case e => val newNode = TextNode(id=ID.usergenerated_id(userID, textString), text=textString);
	  return newNode;
  }
}

}


object SentenceParser {
  def main(args: Array[String]) {
  	  val sentenceParser = new SentenceParser()
  	  val sentence1 = "\"Chih-Chun\" is a \"toad\""
  	  val sentence2 = args.reduceLeft((w1:String, w2:String) => w1 + " " + w2)
  	  println("From main: " + sentence1)
      val results1 = sentenceParser.parseSentence(sentence1)
      println("From command line: " + sentence2)
      val results2 = sentenceParser.parseSentence(sentence2)


	}
}
