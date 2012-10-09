package com.graphbrain.nlp

import java.net.URLDecoder;
import scala.collection.immutable.HashMap
import scala.util.Sorting
import com.graphbrain.hgdb.VertexStore
import com.graphbrain.hgdb.OpLogging
import com.graphbrain.hgdb.TextNode
import com.graphbrain.hgdb.Edge
import com.graphbrain.hgdb.URLNode
import com.graphbrain.hgdb.UserNode
import com.graphbrain.hgdb.EdgeType
import com.graphbrain.hgdb.Vertex
import com.graphbrain.hgdb.ID
import com.graphbrain.hgdb.SearchInterface

class SentenceParser (storeName:String = "gb") {

  val store = new VertexStore(storeName)

  val quoteRegex = """(\")(.+?)(\")""".r
  val nodeRegex = """(\[)(.+?)(\])""".r
  val urlRegex = """([\d\w]+?:\/\/)?([\w\d\.\-]+)(\.\w+)(:\d{1,5})?(\/\S*)?""".r // See: http://stackoverflow.com/questions/8725312/javascript-regex-for-url-when-the-url-may-or-may-not-contain-http-and-www-words?lq=1
  val urlStrictRegex = """(http|ftp|https):\/\/[\w\-_]+(\.[\w\-_]+)+([\w\-\.,@?^=%&amp;:/~\+#]*[\w\-\@?^=%&amp;/~\+#])?""".r
  val gbNode = store.createTextNode(namespace="1", text="GraphBrain")

  val verbRegex = """VB[A-Z]?""".r
  val adverbRegex = """RB[A-Z]?""".r
  val prepositionRegex = """(IN[A-Z]?)|TO""".r
  val relRegex = """(VB[A-Z]?)|(RB[A-Z]?)|(IN[A-Z]?)|TO|DT""".r
  val nounRegex = """NN[A-Z]?""".r
  val imageExt = List("""[^\s^\']+(\.(?i)(jpg))""".r, """[^\s^\']+(\.(?i)(jpeg))""".r, """[^\s^\']+(\.(?i)(gif))""".r, """[^\s^\']+(\.(?i)(tif))""".r, """[^\s^\']+(\.(?i)(png))""".r, """[^\s^\']+(\.(?i)(bmp))""".r, """[^\s^\']+(\.(?i)(svg))""".r)
  val videoExt = List("""http://www.youtube.com/watch?.+""".r, """http://www.vimeo.com/.+""".r, """http://www.dailymotion.com/video.+""".r)
  val gbNodeExt = """(http://)?graphbrain.com/node/""".r
  val rTypeMapping = HashMap("is the opposite of "->"is the opposite of", "means the same as " -> "means_the_same_as", "is a "->"is_a", "is an "->"is_an",  "is a type of "->"is_a_type_of", "has a "->"has_a")

  //"is" needs to be combined with POS to determine whether it is a property, state, or object that is being referred to.
  val questionWords = List("do", "can", "has", "did", "where", "when", "who", "why", "will", "how")

  val searchWords = List("search", "find", "search for", "look for", "look up")

  //val posTagger = new POSTagger()
  val lemmatiser = new Lemmatiser()

  val si = new SearchInterface(store)

  //Returns a float value indicating level of certainty (relative to other values)
  def isQuestion(text:String): Double = {
    //Quite stringent - checks to see whether entire text is a question i.e. starts with a question mark, ends with a question mark.
    for(qWord <- questionWords) {
      if(text.toLowerCase.startsWith(qWord)) {

        if(text.endsWith("?")) {
          1
        }
      }
    }
    0
  }

  def isSearch(text: String): (Double, String) = {
    for(searchWord <- searchWords) {
      if(text.toLowerCase.startsWith(searchWord)) {

        return (1, text.substring(searchWord.length, text.length).replace("`", "").replace("\"", "").replace("'", "").trim)
      }
    }
    val posTags = lemmatiser.posTag(text);
    for(tag <- posTags) {
      if(verbRegex.findAllIn(tag._2).hasNext) {
        (0, "")
      }
    }
    (0.5, text)
    
  }
  def specialNodeCases(inNodeText: String, root: Vertex = store.createTextNode(namespace="", text="GBNoneGB"), user: Option[UserNode]=None): Vertex = {
    user match {
      case Some(u:UserNode) =>
        if(u.username == inNodeText || u.name == inNodeText || inNodeText == "I" || inNodeText == "me") {
          return u;
        }
      case _ => 
    }

    root match {
      case a: TextNode =>
        if(a.text == inNodeText || a.text.toLowerCase.indexOf(inNodeText.toLowerCase)==0 || inNodeText.toLowerCase.indexOf(a.text.toLowerCase) == 0) {
          return a;
        }
        //Check whether already in database - global and user; create new node if necessary
        user match {
          case Some(u:UserNode) => 
            val userThingID = ID.usergenerated_id(u.username, a.text)
            
            if(nodeExists(userThingID)) {
              if(inNodeText==a.text) {
                return a;
              
              }
            }
          case _ => 
        }
      case _ =>
    }
    return textToNode(inNodeText, user=user)(0);

  }


  def parseSentenceGeneral(inSent: String, root: Vertex = store.createTextNode(namespace="", text="GBNoneGB"), user: Option[UserNode]=None): List[ResponseType] = {
    var inSentence = inSent;
    var solutions : List[(List[Vertex], Vertex)] = List();
    var responses : List[ResponseType] = List();

    val search = isSearch(inSentence)
    val question = isQuestion(inSentence)

    if(question > search._1 && question > 0.5) {
      responses = HardcodedResponse(List("Sorry, I don't understand questions yet.")) :: responses
    }
    else if (search._1 > 0.5){
      responses = SearchResponse(List(search._2)) :: responses
    }
 
    //Only remove full stop or comma at the end of sentence (allow other punctuation since likely to be part of proper name e.g. film titles)
    if(inSentence.endsWith(".")) {
      inSentence = inSentence.slice(0, inSentence.length-1)
    }

    //Try segmenting with quote marks, then with known splitters
    var parses = strictChunk(inSentence, root);
    //quoteChunkGeneral(inSentence, root);
    //++ logChunkGeneral(inSentence, root);

    //Only parse with POS if nothing returned:
    if(parses == Nil) {
      parses = posChunkGeneral(inSentence, root)
    }

    for(parse <- parses) {
      var nodeTexts = parse._1;
      var relText = parse._2;
      var nodes: List[Vertex] = List()
      val sepRelations = """~""".r.split(relText)
      var i = 0;
      
      var newRelation = ""
          
      for(nodeText <- nodeTexts) {

        
        if(nodeText.toLowerCase == "you" || nodeText.toUpperCase == "I") {
          if(nodeText.toLowerCase == "you") {
            nodes = gbNode :: nodes;
          } 
          else {
            user match {
              case Some(u: UserNode) => nodes = u :: nodes;
              case _ => 
            }
          }
          
          if(i < sepRelations.length) {
            
            val annotatedRelation = lemmatiser.annotate(sepRelations(i))    
            for (a <- annotatedRelation) {
              if(verbRegex.findAllIn(a._2).hasNext) {
                newRelation += lemmatiser.conjugate(a._3) + " "
              }
              else {
                newRelation += a._1 + " "
              }

            }
            
          }

        }
        else {
          nodes = specialNodeCases(nodeText, root, user) :: nodes;
          if(i < sepRelations.length) {
            newRelation += sepRelations(i) + " ";  
          }
          
        }
        newRelation = newRelation.trim;
        if(i < sepRelations.length-1) {
          newRelation += "~"
        }
        
        i += 1;
      }
      relText = newRelation.trim.slice(0, newRelation.length).trim;
      var relationV = store.createEdgeType(id = ID.reltype_id(relText), label = relText)
      solutions = (nodes.reverse, relationV) :: solutions

    }
    responses = GraphResponse(solutions) :: responses

    if(question > search._1 && question <= 0.5) {
      responses = HardcodedResponse(List("Sorry, I don't understand questions yet.")) :: responses
    }
    else if (search._1 <= 0.5){
      responses = SearchResponse(List(search._2)) :: responses
    }

    //This will be the first in the list - so parsing favours graph responses over hardcoded etc.
    
    return responses.reverse;
    
    
  }

  def textToNode(text:String, node: Vertex = store.createTextNode(namespace="", text="GBNoneGB"), user:Option[Vertex]=None): List[Vertex] = {
    var userName = "";
    var results: List[Vertex] = List()
    user match {
        case Some(u:UserNode) => 
          userName = u.username;
          val name = u.name
          if(text.toLowerCase.indexOf(userName.toLowerCase) == 0 || userName.toLowerCase.indexOf(text.toLowerCase) == 0 ||text.toLowerCase.indexOf(name.toLowerCase) == 0 || name.toLowerCase.indexOf(text.toLowerCase) == 0 ) {
            results = u :: results;
          }
        case _ => 

    }
   
    if(nodeExists(text)) {
      try{
        results = store.get(text) :: results;        
      }
      catch {case e =>}

    }

    if(gbNodeExt.split(text).length==2) {
      val gbID = gbNodeExt.split(text)(1)
      
      if(nodeExists(gbID)) {
        
        try {
          results = store.get(gbID) :: results;
        }
      }
    }

    if (urlRegex.findAllIn(text).hasNext) {
      
      val urlNode = URLNode(store, text)
      results = store.createURLNode(url = text, userId = "") :: results;
      
    }
    val textPureID = ID.text_id(text, 1)
    val wikiID = ID.wikipedia_id(text)

    
    if(nodeExists(textPureID)) {
      results = getOrCreate(textPureID) :: results;  
    }
    
    var i = 1;
    while(nodeExists(ID.text_id(text, i)))
    {
      results = store.createTextNode(namespace=i.toString, text=text) :: results;
      i += 1;
        
    }
    if(i==1) {
      results = store.createTextNode(namespace="1", text = text) :: results;
    }
    return results.reverse
    
  }

 
  /**
  Returns lemma node and pos relationship type (linking the two edge types).
  */
  def relTypeLemmaAndPOS(relType: EdgeType, sentence: String): (EdgeType, (TextNode, EdgeType)) = {
    
    /*if(relType.label == "is a"||relType.label == "is an") {
      val isLemmaNode = TextNode(id = ID.text_id("be", 1), text = "be")
      val isRelType = EdgeType(id = ID.reltype_id("VBZ"), label = "VBZ")
      return (relType, (isLemmaNode, isRelType))
    }*/
    val allRelTypes = """~""".r.split(relType.label)
    val posSentence = lemmatiser.annotate(sentence)
    var lemma = ""
    var poslabel = ""
    for (rType <- allRelTypes) {

      
      val splitRelType = """\s""".r.split(rType)

      for(i <- 0 to splitRelType.length-1) {
        val relTypeComp = splitRelType(i).trim
        
        for (tagged <- posSentence) {
        
          if(tagged._1 == relTypeComp) {
            poslabel += tagged._2 + "_";
            lemma += tagged._3 + "_";
          }
          

        }
      }
      poslabel = poslabel.slice(0, poslabel.length).trim + "~"
      lemma = lemma.slice(0, lemma.length).trim + "~"

    //Remove the last "_"
    }
    poslabel = poslabel.slice(0, poslabel.length).trim
    lemma = lemma.slice(0, lemma.length).trim
     
    val lemmaNode = store.createTextNode(namespace="1", text=lemma)
    val lemmaRelType = store.createEdgeType(id = ID.reltype_id(poslabel), label = poslabel)
    return (relType, (lemmaNode, lemmaRelType));
    
  }

def strictChunk(sentence: String, root: Vertex): List[(List[String], String)] = {
  var possibleParses: List[(List[String], String)] = List();
  val nodeTexts = nodeRegex.findAllIn(sentence);
  if(!nodeTexts.hasNext) {
    return possibleParses;
  }
  val edgeTexts = nodeRegex.split(sentence);
  var nodes: List[String] = List()
  var edge = ""
  for(nodeText <- nodeTexts) {
    nodes = nodeText.replace("[", "").replace("]", "").trim:: nodes
  }
  //Index from 1 since first element is discarded
  for(i <- 1 to edgeTexts.length-1) {
    edge += edgeTexts(i).trim.replace(" ", "_") + "~";
  }
  edge = edge.slice(0, edge.length-1)
  return List((nodes, edge))

}
def quoteChunkGeneral(sentence:String, root:Vertex): List[(List[String], String)] = {
  //quoteChunkStrict(sentence, root);
  var possibleParses: List[(List[String], String)] = List()

  quoteRegex.findFirstIn(sentence) match {

    case Some(exp) => 


      //I'm assigning these in case we want to do something with them later, e.g. infer hypergraphs vs. multi-bi-graphs
      val quotes = quoteRegex.findAllIn(sentence)
      val numQuotes = quotes.length;
      var nonQuotes = quoteRegex.split(sentence);
      var nqEdges:List[String] = List()
      var numNonQuotes = 0;

      for (i <- 0 to nonQuotes.length-1) {
        val nq = nonQuotes(i)
        if(nq!="") {
          numNonQuotes+=1;
          nqEdges = nq.trim::nqEdges;
        }
      }
      nqEdges = nqEdges.reverse
      
      //Make sure the quotation marks are not around the whole string:
      if (exp.length == sentence.length) {
          return Nil;
      }
      else if ((numQuotes - numNonQuotes) == 1) {
        
        val nodes = quoteRegex.findAllIn(sentence).toArray;
        var nodeResults: List[String] = List() 
        var edgeText = ""
          
        for(i <- 0 to nodes.length-2) {
          
          nodeResults =  nodes(i).replace("\"", "").trim :: nodeResults;
          nodeResults = nodes(i+1).replace("\"", "").trim :: nodeResults;
          edgeText += nqEdges(i) + "~"
        } 
        nodeResults = nodeResults.reverse;
        possibleParses = (nodeResults, edgeText.substring(0, edgeText.length-1)) :: possibleParses; 
        println("Quote Chunk: " + nodeResults(0) + ", " + edgeText.substring(0, edgeText.length-1) + ", " + nodeResults(1))

        
        return possibleParses;
      } 
      
      case None => return possibleParses;
    }
    return possibleParses

}


  


def posChunkGeneral(sentence: String, root: Vertex): List[(List[String], String)]={
  val taggedSentence = lemmatiser.posTag(sentence)
  val untaggedSentence = """\s""".r.split(sentence);
  var possibleParses: List[(List[String], String)] = List()

  var inEdge = false;

  var nodeTexts: List[String] = List()
  var edgeText = ""
  var nodeText = ""


  for(i <- 0 to taggedSentence.length-2) {
      
    val current = taggedSentence(i)
      
    val lookahead = taggedSentence(i+1)
      
    (current, lookahead) match{
        case ((word1, tag1), (word2, tag2)) => 


          //println(word2 + ", " + tag2)
        if((relRegex.findAllIn(tag1).length == 1)) {
          edgeText += untaggedSentence(i) + " "
            if(relRegex.findAllIn(tag2).length == 0) {
              edgeText = edgeText.substring(0, edgeText.length-1)
              edgeText += "~"
          }

          
        }
        else if (relRegex.findAllIn(tag1).length == 0) {
          nodeText += untaggedSentence(i) + " "
          if(relRegex.findAllIn(tag2).length == 1) {

            nodeTexts = nodeText.replace("`", "").replace("'", "").trim :: nodeTexts;
            nodeText = ""
          }
          
        }
        if (i == (taggedSentence.length-2)) {
          nodeText += untaggedSentence(i+1);
          nodeTexts = nodeText.replace("`", "").replace("'", "").trim :: nodeTexts;

        }
      }
    }
    nodeTexts = nodeTexts.reverse;
    //println(nodeTexts.length)
    edgeText = edgeText.substring(0, edgeText.length-1);

    possibleParses = (nodeTexts, edgeText) :: possibleParses
              
    return possibleParses.reverse;
  }

def findOrConvertToVertices(possibleParses: List[(List[String], String)], root: Vertex, user:Option[Vertex], maxPossiblePerParse: Int = 10): List[(List[Vertex], Edge)]={
    
    var userID = ""
    user match {
        case u:UserNode => userID = u.username;
        case _ => 

    }
	var possibleGraphs:List[(List[Vertex], Edge)] = List()
	val sortedParses = removeDeterminers(sortRootParsesPriority(possibleParses, root), root)

  println("Sorted parses: " + sortedParses.length)

	for (pp <- sortedParses) {
		pp match {
			case (nodeTexts: List[String], edgeText: String) => 
			var nodesForEachNodeText = new Array[List[Vertex]](nodeTexts.length)
      var countsForEachNodeText = new Array[Int](nodeTexts.length)
			
			var edgesForEdgeText: List[Edge] = List()
			var textNum = 0;
			
      for (nodeText <- nodeTexts) {
				val results = si.query(nodeText)
				
				//fuzzy search results are second in priority
				var currentNodesForNodeText:List[Vertex] = List() 
				val limit = if (maxPossiblePerParse < results.length) maxPossiblePerParse else results.length;
        println("Limit: " + limit)
				for(i <- 0 to limit-1) {
				  val result = try {results(i) } catch { case e => ""}
				  val resultNode = getOrCreate(result, user, nodeText, root)
				  println("Node: " + resultNode.id)

				  currentNodesForNodeText = resultNode :: currentNodesForNodeText;
				}
        //Result for a new node to be created
        val resultNode = getOrCreate("", user, nodeText, root)
        currentNodesForNodeText = resultNode :: currentNodesForNodeText;
				nodesForEachNodeText(textNum) = currentNodesForNodeText;
        countsForEachNodeText(textNum) = currentNodesForNodeText.length;
				textNum += 1;

			}
      Sorting.quickSort(countsForEachNodeText)
      val minNodes = (countsForEachNodeText)(0)

      //TODO Fix this properly! At the moment, I just get the minimum 
		  for (i <- 0 to minNodes-1) {

		    var entryNodes:List[Vertex] = List();
			  var entryIDs:List[String] = List();

			  entryNodes = nodesForEachNodeText(0)(i) :: entryNodes;
			  entryNodes = nodesForEachNodeText(1)(i) :: entryNodes;
			  entryIDs = nodesForEachNodeText(0)(i).id :: entryIDs
			  entryIDs = nodesForEachNodeText(1)(i).id :: entryIDs

			  val edge = new Edge(ID.relation_id(edgeText), entryIDs.reverse)
			  println("Edge: " + edge)
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
            var optComplete = false;
  				  for(nodeText <- nodeTexts) {

  				  	if (nodeText==rootText) {
  				  		rootParses = g::rootParses
                //If the root text appears in more than one node (e.g. self-referencing), allow both possibilities
  				  	}
  				  	else if(optComplete==false) {
  				  		 optionalParses = g::optionalParses
                 optComplete=true;
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

def removeDeterminers(text: String): String={
  if(lemmatiser==null) return null
  val posTagged = lemmatiser.posTag(text);
  
  var newText = ""
  for (tag <- posTagged) {
    tag match{
      case (a,b) => 
        if(b=="DT") return text.replace(a + " ", "").replace("`", "").replace("'", "").trim
        //only first determiner is removed
    }
  }
  text
 
}

def removeDeterminers(possibleParses: List[(List[String], String)], rootNode: Vertex, returnAll: Boolean = false): List[(List[String], String)]={
    var removedParses: List[(List[String], String)] = List()
    var optionalParses: List[(List[String], String)] = List()
    if(lemmatiser==null) return null
    for (g <- possibleParses) {
      g match {
        case (nodeTexts: List[String], edgeText: String) => 
        var newNodes = nodeTexts.toArray;
        for(i <- 0 to nodeTexts.length-1) {
          val nodeText = nodeTexts(i)
          val posTagged = lemmatiser.posTag(nodeText);
          var done = false
          for(tag <- posTagged)
          {
            tag match{
              case (a,b) => 
                if(b=="DT" && done==false) {
                  
                  newNodes(i)=nodeText.replace(a+" ", "").trim.replace("`", "").replace("'", "");
                  val newParse = (newNodes.toList, edgeText)
                  removedParses = newParse::removedParses;
                  done = true //Rmoves only first determiner
                }
              }
          }
        }
        optionalParses = g::optionalParses;
        
      }
      
    }
    if(returnAll) {
      
     return removedParses.reverse++optionalParses.reverse
    }
    else {
      return removedParses.reverse;
    }

}


def getOrCreate(id:String, user:Option[Vertex] = None, textString:String = "", root:Vertex = store.createTextNode("", "")):Vertex={
  if(id != "") {
    try{
      return store.get(id);
    }
    catch{
      case e => val newNode = textToNode(textString, root, user)(0);
      //TextNode(id=ID.usergenerated_id(userID, textString), text=textString);
      return newNode;
    }
  }
  else {
    val newNode = textToNode(textString, root, user)(0);
    return newNode;
  }

}
def nodeExists(id:String):Boolean =
  {
    try{

      val v = store.get(id)
      if(v.id==id) {

        return true
      }
      else {
        return false
      }

    }
    catch{
      case e => return false
    }
  }



}


object SentenceParser {
  def main(args: Array[String]) {
  	  val sentenceParser = new SentenceParser()
      
      val rootNode = sentenceParser.store.createTextNode(namespace="usergenerated/chihchun_chen", text="toad")
      val userNode = sentenceParser.store.createUserNode(id="user/chihchun_chen", username="chihchun_chen", name="Chih-Chun Chen")
  	  val sentence = args.reduceLeft((w1:String, w2:String) => w1 + " " + w2)
      println("From command line with general: " + sentence)
      val responses = sentenceParser.parseSentenceGeneral(sentence, user = Some(userNode))
        for(response <- responses) {
          response match {
            case g: GraphResponse =>
              val parses = g.hypergraphList
              for(parse <- parses) {
                parse match {
                  case (n: List[Vertex], r: Vertex) =>
                  for(node <- n) {
                    println("Node: " + node.id);
                  }
                  println("Rel: " + r.id)
                }

              }
            case r: ResponseType => println(r)

          }
 
      }

	}
}
