package com.graphbrain.nlp

import java.net.URLDecoder;
import scala.collection.immutable.HashMap
import scala.util.Sorting
import com.graphbrain.hgdb.VertexStore
import com.graphbrain.hgdb.BurstCaching
import com.graphbrain.hgdb.OpLogging
import com.graphbrain.hgdb.TextNode
import com.graphbrain.hgdb.ImageNode
import com.graphbrain.hgdb.VideoNode
import com.graphbrain.hgdb.Edge
import com.graphbrain.hgdb.URLNode
import com.graphbrain.hgdb.UserNode
import com.graphbrain.hgdb.EdgeType
import com.graphbrain.hgdb.Vertex
import com.graphbrain.searchengine.Indexing
import com.graphbrain.searchengine.RiakSearchInterface
import com.graphbrain.hgdb.ID

class SentenceParser (storeName:String = "gb") {

  val quoteRegex = """(\")(.+?)(\")""".r
  val urlRegex = """([\d\w]+?:\/\/)?([\w\d\.\-]+)(\.\w+)(:\d{1,5})?(\/\S*)?""".r // See: http://stackoverflow.com/questions/8725312/javascript-regex-for-url-when-the-url-may-or-may-not-contain-http-and-www-words?lq=1
  val urlStrictRegex = """(http|ftp|https):\/\/[\w\-_]+(\.[\w\-_]+)+([\w\-\.,@?^=%&amp;:/~\+#]*[\w\-\@?^=%&amp;/~\+#])?""".r
  //"""http:\/\/.+""".r
  val posTagger = new POSTagger()
  val verbRegex = """VB[A-Z]?""".r
  val adverbRegex = """RB[A-Z]?""".r
  val propositionRegex = """IN[A-Z]?""".r
  val nounRegex = """NN[A-Z]?""".r
  val imageExt = List("""[^\s^\']+(\.(?i)(jpg))""".r, """[^\s^\']+(\.(?i)(jpeg))""".r, """[^\s^\']+(\.(?i)(gif))""".r, """[^\s^\']+(\.(?i)(tif))""".r, """[^\s^\']+(\.(?i)(png))""".r, """[^\s^\']+(\.(?i)(bmp))""".r, """[^\s^\']+(\.(?i)(svg))""".r)
  val videoExt = List("""http://www.youtube.com/watch?.+""".r, """http://www.vimeo.com/.+""".r, """http://www.dailymotion.com/video.+""".r)
  val gbNodeExt = """(http://)?graphbrain.com/node/""".r
  val rTypeMapping = HashMap("is the opposite of"->"gb_antonymy", "means the same as" -> "gb_synonymy", "is a"->"gb_subtype", "is an"->"gb_subtype",  "is a type of"->"gb_subtype", "has a"->"gb_possessive")
  //"is" needs to be combined with POS to determine whether it is a property, state, or object that is being referred to.

  

  val si = RiakSearchInterface("gbsearch")
  
  val store = new VertexStore(storeName) with Indexing with BurstCaching
  

  //Only handles two-node graphs at the moment
  def parseSentence(inSentence: String, root: Vertex  = TextNode(id="GBNoneGB", text="GBNoneGB"), user: Option[UserNode]=None): (List[Vertex], List[Vertex], List[Vertex]) = {
    var sources: List[Vertex] = List()
    var relations: List[Vertex] = List()
    var targets: List[Vertex] = List()

    
    //Try segmenting with quote marks, then with known splitters
    var parses = quoteChunkStrict(inSentence, root) ++ logChunk(inSentence, root);

    //Only parse with POS if nothing returned:
    if(parses == Nil) {
      parses = posChunk(inSentence, root)
    }

    for (parse <- parses) {

      val nodeTexts = parse._1;
      val relation = parse._2.trim;
      
      if(nodeTexts.length==2) {

        var relationV = EdgeType(id = ID.reltype_id(relation), label = relation)
        //println(relationV.id)

        root match {
          case a: TextNode =>
            //Check whether already in database - global and user; create new node if necessary
            user match {
              case Some(u:UserNode) => 
                val userThingID = ID.usergenerated_id(u.username, a.text)
                //val userRelationID = ID.usergenerated_id(u.username, relationV.id)
                
                if(nodeExists(userThingID)) {
                  if(nodeTexts(1)==a.text) {
                    targets = a :: targets
                    //targets = getOrCreate(userThingID) :: targets   
                  }
                  if(nodeTexts(0)==a.text) {
                    sources = a :: sources
                    //sources = getOrCreate(userThingID) :: sources
                  }
                }
                //val relationUV = EdgeType(id = ID.usergenerated_id(u.username, relationV.id), label = relation)
                //relations = relationUV :: relations

              case _ => 
            }
            val wikiThingID = ID.wikipedia_id(a.text)
            if(nodeExists(wikiThingID))  {
              val wikiNode = getOrCreate(wikiThingID)
              wikiNode match {
                case w: TextNode =>
                  if(nodeTexts(1) == w.text) {
                    targets = w :: targets
                  }
                  if(nodeTexts(0) == w.text) {
                    sources = w :: sources
                  }
              }
            }
          case _ =>
        }
        user match {
          case Some(u:UserNode) =>

            if(u.username == nodeTexts(1) || u.name == nodeTexts(1)) {
              targets = u :: targets
            }
            if(u.username == nodeTexts(0) || u.name == nodeTexts(0)) {
              sources = u :: sources
            }
          case _ =>
        }



        sources = sources.reverse ++ textToNode(nodeTexts(0))
        targets = targets.reverse ++ textToNode(nodeTexts(1))
        relations = relationV :: relations;


        
        return (sources, relations.reverse, targets)

      }
      else {
        throw TooManyNodes("Too many nodes: " + nodeTexts.length)
      }

    }
        


    //If no results returned, parse with POS.


    //If root matches the source or target, it is given priority one



    return (sources, relations, targets)
  }


  

  def textToNode(text:String, node: Vertex= TextNode(id="GBNoneGB", text="GBNoneGB"), user:Option[Vertex]=None): List[Vertex] = {
    var userName = "";
    
    user match {
        case Some(u:UserNode) => userName = u.username;
        case _ => 

    }

    var results: List[Vertex] = List()
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



    //Only make special content types with respect to a "thing" (TextNode)
    /*node match {
      case r: TextNode => 
        
        for (imageE <- imageExt) {
          if (imageE.findAllIn(text).hasNext && r.id != "GBNoneGB") {             
            val imageID = ID.image_id(image_name = r.text, image_url = text); 
            results =  ImageNode(id = ID.usergenerated_id(userName, imageID, brainName), url = text) :: results;
          }
        }

        for (videoE <- videoExt) {
          if (videoE.findAllIn(text).hasNext && r.text != "GBNoneGB") {
            val videoID = ID.video_id(video_name = r.text, video_url = text)
            results = VideoNode(id = ID.usergenerated_id(userName, videoID, brainName), url = text) :: results;
          }
        }
      case _ =>
      }*/

    if (urlRegex.findAllIn(text).hasNext) {
      val urlID = ID.url_id(url = text)
      results = URLNode(id = ID.usergenerated_id(userName, urlID)) :: results
    }

    //val textID = ID.text_id(removeDeterminers(text))
    //results = TextNode(id = ID.usergenerated_id(userName, textID, brainName), text=removeDeterminers(text)) :: results;
    val wikiID = ID.wikipedia_id(text)
    //Add the wikipedia node as a possible entry if found
    if(nodeExists(wikiID)) {results = getOrCreate(wikiID) :: results}
    
    val textPureID = ID.text_id(text, 1)
    if(userName==""){
      results = TextNode(id = textPureID, text=text) :: results;
      var i = 2;
      while(nodeExists(ID.text_id(text, i)))
      {
        results = TextNode(id = ID.text_id(text, i), text=text) :: results;
        i +=1;
        
      }
    }
    else {
      results = TextNode(id = ID.usergenerated_id(userName, textPureID), text=text) :: results;
      var i=2
      while(nodeExists(ID.usergenerated_id(userName, ID.text_id(text, i))))
      {
        results = TextNode(id = ID.usergenerated_id(userName, ID.text_id(text, i))) :: results
      }
    }
    
    //if(text!=textPureID) {results = TextNode(id = ID.usergenerated_id(userName, textPureID), text=text) :: results;}
    return results.reverse;
  }

  


  
/**
Only returns results where both source and target are in quotes, or where there is a known 
splitter
*/
def quoteChunkStrict(sentence: String, root: Vertex): List[(List[String], String)] = {
  var possibleParses: List[(List[String], String)] = List()

  quoteRegex.findFirstIn(sentence) match {

    case Some(exp) => 


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
      
      //Make sure the quotation marks are not around the whole string:
      if (exp.length == sentence.length) {
          return Nil;
      }
      else if (numQuotes >= 2 && numNonQuotes == 1) {
        val nodes = quoteRegex.findAllIn(sentence).toArray;
        val edges = nqEdges.reverse.toArray
          
        for(i <- 0 to nodes.length-2) {
          val current = nodes(i);
          val next = nodes(i+1);
          var edge = getBaseRel(edges(i).trim);

          possibleParses = (List(current.replace("\"", "").trim, next.replace("\"", "").trim), edge) :: possibleParses; 
          println("Quote Chunk: " + current + ", " + edge + ", " + next)

        }
        return possibleParses;
      } 
      
      case None => return possibleParses;
    }
    return possibleParses

}
def logChunk(sentence: String, root: Vertex): List[(List[String], String)]={
    val knownSplitters = rTypeMapping.keys
    var possibleParses: List[(List[String], String)] = List()
    for (splitter <- knownSplitters) {
      val nodes = sentence.split(splitter)
      if(nodes.length==2) {
        val source = nodes(0).trim
        val target = nodes(1).trim
        possibleParses = (List(source, target), rTypeMapping(splitter)) :: possibleParses
      }
    }
    return possibleParses.reverse;
  }

  def getBaseRel(rel: String): String = {
    for (key <- rTypeMapping.keys) {
      if(key == rel) {
        return rTypeMapping(key)
      }
    }
    return rel;
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
    		  	  println("POS Chunk: " + node1Text + " ," + edgeText + " ," + node2Text)
    		  	  val nodes = List(node1Text.replace("`", "").replace("'", "").trim, node2Text.replace("`", "").replace("'", "").trim)
    		  	  possibleParses = (nodes, edgeText) :: possibleParses

    		  	}
				    		  	
    		  }


    	  }
    }
    return possibleParses.reverse;
  }


  def findOrConvertToVertices(possibleParses: List[(List[String], String)], root: Vertex, user:UserNode, maxPossiblePerParse: Int = 10): List[(List[Vertex], Edge)]={
    
    var userID = user.username
    /*user match {
        case u:UserNode => userID = u.username;
        case _ => 

    }*/
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

				var searchResults = try{si.query(nodeText).ids} catch {case e => Nil};
				var fuzzySearchResults = try{si.query(nodeText+"*").ids} catch{case e => Nil};

				val results = try{searchResults ++ fuzzySearchResults} catch {case e => Nil}
				
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
  if(posTagger==null) return null
  val posTagged = posTagger.tagText(text);
  
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
    if(posTagger==null) return null
    for (g <- possibleParses) {
      g match {
        case (nodeTexts: List[String], edgeText: String) => 
        var newNodes = nodeTexts.toArray;
        for(i <- 0 to nodeTexts.length-1) {
          val nodeText = nodeTexts(i)
          val posTagged = posTagger.tagText(nodeText);
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


def getOrCreate(id:String, user:Vertex = UserNode("", ""), textString:String = "", root:Vertex = TextNode("", "")):Vertex={
  if(id != "") {
    try{
      return store.get(id);
    }
    catch{
      case e => val newNode = textToNode(textString, root, Some(user))(0);
      //TextNode(id=ID.usergenerated_id(userID, textString), text=textString);
      return newNode;
    }
  }
  else {
    val newNode = textToNode(textString, root, Some(user))(0);
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

//Abandoned code:

 /* def parseSentence(inSentence: String, root: Vertex = TextNode(id="GBNoneGB", text="GBNoneGB"), parseType: String = "graph", numResults: Int = 10, user:Option[UserNode]=None): List[(List[Vertex], Edge)]={
    val sentence = inSentence.trim;
    //I'm envisioning that in the future we may have other parsing purposes where we might have other parsing rules 
    //(e.g. ignoring non-rootparses, different rule precedences) so I've kept the graph creation parsing as just one option.
    if(parseType == "graph") {
      var userIn = UserNode(anon_username, anon_username)
      user match {
        case Some(u:UserNode) => userIn = u;
        case _ => 
      }
      return parseToGraph(sentence, root, numResults, userIn);
    }
    else {
      return Nil
    }
  }

def parseToGraph(sentence: String, root: Vertex, numResults: Int, user: UserNode): List[(List[Vertex], Edge)]={

    var possibleParses = quoteChunkStrict(sentence, root) ++ posChunk(sentence, root);
    println("Possible parses: " + possibleParses.length)
    var possibleGraphs = findOrConvertToVertices(possibleParses, root, user, numResults);
    println("Possible graphs: " + possibleGraphs.length)
    return possibleGraphs.take(numResults);
  }

def textToNode(text:String): List[Vertex] = {
    var results: List[Vertex] = List()
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

    /*for (imageE <- imageExt) {
      if (imageE.findAllIn(text).hasNext) {             
        val imageID = ID.image_id(image_name = text, image_url = text); 
        results =  ImageNode(id = brainID + "/" +  imageID, url = text) :: results;
      }
    }

    for (videoE <- videoExt) {
      if (videoE.findAllIn(text).hasNext) {
        val videoID = ID.video_id(video_name = text, video_url = text)
        results = VideoNode(id = brainID + "/" + videoID, url = text) :: results;
      }
    }*/
      
    if (urlRegex.findAllIn(text).hasNext) {
      val urlID = ID.url_id(url = text)
      results = URLNode(id = urlID, url=text) :: results
    }
    
    if(results.length>=1) return results.reverse;

    //val textID = ID.text_id(removeDeterminers(text))
    //results = TextNode(id = brainID + "/" + textID, text=removeDeterminers(text)) :: results;
    val textPureID = ID.text_id(text)

    results = TextNode(id = textPureID, text=text) :: results;

    return results.reverse;
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
            possibleParses = (List(current.replace("\"", "").trim, next.replace("\"", "").trim), edge) :: possibleParses; 
            println("Quote Chunk: " + current + ", " + edge + ", " + next)

          }
          return possibleParses;
    } 
      case None => return possibleParses;
    }
    return possibleParses

  }*/



}


object SentenceParser {
  def main(args: Array[String]) {
  	  val sentenceParser = new SentenceParser()
      
      val rootNode = TextNode(id=ID.usergenerated_id("chihchun_chen", "toad"), text="toad")
      val userNode = UserNode(id="user/chihchun_chen", username="chihchun_chen", name="Chih-Chun Chen")
  	  val sentence1 = "\"Chih-Chun Chen\" is a \"toad\""
  	  val sentence2 = args.reduceLeft((w1:String, w2:String) => w1 + " " + w2)
      val videoURL1 = "http://www.youtube.com/watch?v=_e_zcoDDiwc&feature=related"
      val videoURL2 = "http://vimeo.com/34948855"
      val videoURL3 = "http://www.dailymotion.com/video/xmehe4_millenium-tv_videogames"
      val imageURL1 = "http://www.flickr.com/photos/londonmummy/471232270/"
      val imageURL2 = "http://en.wikipedia.org/wiki/File:Crohook.jpg"
  	  println("From main: " + sentence1)
      val mParses = sentenceParser.parseSentence(sentence1, user=Some(userNode))
      val mSources = mParses._1;
      val mRelations = mParses._2;
      val mTargets = mParses._3;
      for(mSource <- mSources) {
        println("Source: " + mSource.id)
      }
      for(mRelation <- mRelations) {
        println("Relation: " + mRelation.id)
      }
      for(mTarget <- mTargets) {
        println("Target: " + mTarget.id)
      }
      //sentenceParser.parseSentence(sentence1)
      //println("From main with root: " + sentence1)
      //sentenceParser.parseSentence(sentence1, rootNode)
      //println("From main with root with user: " + sentence1)
      //sentenceParser.parseSentence(sentence1, rootNode, user=Some(userNode))

      //println("Video with root with user: " + videoURL1)
      //println(sentenceParser.textToNode(videoURL1, rootNode, user=Some(userNode)))
      //println("Video with root with user: " + videoURL2)
      //println(sentenceParser.textToNode(videoURL2, rootNode, user=Some(userNode)))
      //println("Video with root with user: " + videoURL3)
      //println(sentenceParser.textToNode(videoURL3, rootNode, user=Some(userNode)))

      //println("Image with root with user: " + imageURL1)
      //println(sentenceParser.textToNode(imageURL1, rootNode, user=Some(userNode)))
      //println("Image with root with user: " + imageURL2)
      //println(sentenceParser.textToNode(imageURL2, rootNode, user=Some(userNode)))
      


      println("From command line: " + sentence2)
      val parses = sentenceParser.parseSentence(sentence2)
      val sources = parses._1;
      val relations = parses._2;
      val targets = parses._3;
      for(source <- sources) {
        println("Source: " + source.id)
      }
      for(relation <- relations) {
        println("Relation: " + relation.id)
      }
      for(target <- targets) {
        println("Target: " + target.id)
      }
      //println("From command line with root: " + sentence2)
      //sentenceParser.parseSentence(sentence2, rootNode)
      //println("From command line with root with user: " + sentence2)
      //sentenceParser.parseSentence(sentence2, rootNode, user=Some(userNode))
      
      
      /*val text = "Some Magic Cookies"
      println("Text: " + text)
      println(sentenceParser.textToNode(text)(0).id)
      
      
      val videoURL = "http://www.youtube.com/watch?v=_e_zcoDDiwc&feature=related"
      println("Video: " + videoURL)
      println(sentenceParser.textToNode(videoURL))


      val imageURL = "http://www.flickr.com/photos/londonmummy/471232270/"
      println("Image: " + imageURL)
      println(sentenceParser.textToNode(imageURL))


      val url = "https://github.com/graphbrain/graphbrain"
      println("Image: " + url)
      println(sentenceParser.textToNode(url))

      val existing = "wikipedia/aristotle"
      println("Existing: " + existing)
      println(sentenceParser.textToNode(existing)(0).id)

      val existingURL = "graphbrain.com/node/wikipedia/aristotle";
      println("Existing URL: " + existingURL)
      println(sentenceParser.textToNode(existingURL)(0).id)*/





	}
}
