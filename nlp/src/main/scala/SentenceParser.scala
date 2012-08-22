package com.graphbrain.nlp

import java.net.URLDecoder;
import scala.collection.immutable.HashMap
import scala.util.Sorting
import com.graphbrain.hgdb.VertexStore
import com.graphbrain.hgdb.BurstCaching
import com.graphbrain.hgdb.OpLogging
import com.graphbrain.hgdb.TextNode
import com.graphbrain.hgdb.Edge
import com.graphbrain.hgdb.URLNode
import com.graphbrain.hgdb.UserNode
import com.graphbrain.hgdb.EdgeType
import com.graphbrain.hgdb.Vertex
import com.graphbrain.hgdb.ID
import com.graphbrain.hgdb.EdgeSet
import com.graphbrain.hgdb.SearchInterface

class SentenceParser (storeName:String = "gb") {

  val quoteRegex = """(\")(.+?)(\")""".r
  val urlRegex = """([\d\w]+?:\/\/)?([\w\d\.\-]+)(\.\w+)(:\d{1,5})?(\/\S*)?""".r // See: http://stackoverflow.com/questions/8725312/javascript-regex-for-url-when-the-url-may-or-may-not-contain-http-and-www-words?lq=1
  val urlStrictRegex = """(http|ftp|https):\/\/[\w\-_]+(\.[\w\-_]+)+([\w\-\.,@?^=%&amp;:/~\+#]*[\w\-\@?^=%&amp;/~\+#])?""".r
  val gbNode = TextNode(id = ID.text_id("GraphBrain", 1), text = "GraphBrain")

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
  
  //val posTagger = new POSTagger()
  val lemmatiser = new Lemmatiser()
  
  val store = new VertexStore(storeName) with BurstCaching

  val si = new SearchInterface(store)

  def isQuestion(text:String): Boolean = {
    //Quite stringent - checks to see whether entire text is a question i.e. starts with a question mark, ends with a question mark.
    for(qWord <- questionWords) {
      if(text.toLowerCase.startsWith(qWord)) {

        if(text.endsWith("?")) {
          return true
        }
      }
    }
    return false;
  }

  //Only handles two-node graphs at the moment
  def parseSentence(inSent: String, root: Vertex  = TextNode(id="GBNoneGB", text="GBNoneGB"), user: Option[UserNode]=None): (List[Vertex], List[Vertex], List[Vertex]) = {
    
    var inSentence = inSent;

    if(isQuestion(inSentence)) {
      throw QuestionException("Sorry, I don't understand questions yet.")
    }

    //Only remove full stop or comma at the end of sentence (allow other punctuation since likely to be part of proper name e.g. film titles)
    if(inSentence.endsWith(".")) {
      inSentence = inSentence.slice(0, inSentence.length-1)
    }

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

        

        if(nodeTexts(0) == "you") {
          sources = gbNode :: sources;
          val annotatedRelation = lemmatiser.annotate(relation)
          var newRelation = ""
          for (a <- annotatedRelation) {
            if(verbRegex.findAllIn(a._2).hasNext) {
              newRelation += lemmatiser.conjugate(a._3) + " "
            }
            else {
              newRelation += a._1 + " "
            }
          }

          relationV = EdgeType(id = ID.reltype_id(newRelation.trim), label = newRelation.trim)
        }

        if(nodeTexts(1) == "you") {
          targets = gbNode :: targets;
        }
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
            if(nodeTexts(0) == "I") {
              sources = u :: sources;

              val annotatedRelation = lemmatiser.annotate(relation)
              var newRelation = ""
              for (a <- annotatedRelation) {
                if(verbRegex.findAllIn(a._2).hasNext) {
                  newRelation += lemmatiser.conjugate(a._3) + " "
                }
                else {
                  newRelation += a._1 + " "
                }
              }

              relationV = EdgeType(id = ID.reltype_id(newRelation.trim), label = newRelation.trim)
            }
            if(nodeTexts(1) == "me") {
              targets = u :: targets;
            }
          case _ =>
        }



        sources = sources.reverse ++ textToNode(nodeTexts(0), user=user)
        targets = targets.reverse ++ textToNode(nodeTexts(1), user=user)
        relations = relationV :: relations;


        
        return (sources, relations.reverse, targets)

      }
      else {
        throw TooManyNodes("Too many nodes: " + nodeTexts.length)
      }

    }
        




    return (sources, relations, targets)
  }

  def specialNodeCases(inNodeText: String, root: Vertex  = TextNode(id="GBNoneGB", text="GBNoneGB"), user: Option[UserNode]=None): Vertex = {
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
        /*val wikiThingID = ID.wikipedia_id(a.text)
        if(nodeExists(wikiThingID))  {
          val wikiNode = getOrCreate(wikiThingID)
          wikiNode match {
            case w: TextNode =>
              if(inNodeText == w.text) {
                return w;
              }
            }
          }*/         
      case _ =>
    }
    return textToNode(inNodeText, user=user)(0);

  }


  def parseSentenceGeneral(inSent: String, root: Vertex  = TextNode(id="GBNoneGB", text="GBNoneGB"), user: Option[UserNode]=None): (List[(List[Vertex], Vertex)]) = {
    var inSentence = inSent;
    var solutions: List[(List[Vertex], Vertex)] = List();

    if(isQuestion(inSentence)) {
      throw QuestionException("Sorry, I don't understand questions yet.")
    }

    //Only remove full stop or comma at the end of sentence (allow other punctuation since likely to be part of proper name e.g. film titles)
    if(inSentence.endsWith(".")) {
      inSentence = inSentence.slice(0, inSentence.length-1)
    }

    //Try segmenting with quote marks, then with known splitters
    var parses = quoteChunkGeneral(inSentence, root);
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
        newRelation = newRelation.trim + "~";
        
        i += 1;
      }
      relText = newRelation.trim.slice(0, newRelation.length-2);
      var relationV = EdgeType(id = ID.reltype_id(relText), label = relText)
      solutions = (nodes.reverse, relationV) :: solutions

    }
    return solutions.reverse;
    
  }

  def textToNode(text:String, node: Vertex= TextNode(id="GBNoneGB", text="GBNoneGB"), user:Option[Vertex]=None): List[Vertex] = {
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
      val urlID = ID.url_id(url = text)
      if(userName == "") {
        results = URLNode(id = urlID, url = text) :: results  
      }
      else {
        results = URLNode(id = ID.usergenerated_id(userName, urlID), url = text) :: results  
      }
      
    }
    val textPureID = ID.text_id(text, 1)
    val wikiID = ID.wikipedia_id(text)

    
    if(nodeExists(textPureID)) {
      results = getOrCreate(textPureID) :: results;  
    }
    
    var i = 1;
    while(nodeExists(ID.text_id(text, i)))
    {
      results = TextNode(id = ID.text_id(text, i), text=text) :: results;
      i += 1;
        
    }
    if(i==1) {
      results = TextNode(id = textPureID, text = text) :: results;
    }
    

    
    return results.reverse;
  }

  /**
  Returns conjugated version of the lemma if present in database. Otherwise
  simply returns the lemma
  */
  def conjugatefromExisting(lemma: String, pos: String): String = {

    val targetESetID = ID.text_id(lemma) + "/0/" + pos; 

    //Get the lemma node:
    if(store.exists(targetESetID)) {
      val posEdgeSet = store.get(ID.text_id(lemma) + "/1/" + pos);
      posEdgeSet match {
        case p: EdgeSet => return lemma 
        case _ => 
      }

    }
    return lemma; 
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
    val posSentence = lemmatiser.annotate(sentence);
    var lemma = "";
    var poslabel = "";
    for (rType <- allRelTypes) {
      
      val splitRelType = """\s""".r.split(rType);

      for(i <- 0 to splitRelType.length-1) {
        val relTypeComp = splitRelType(i)
        for (tagged <- posSentence) {
          if(tagged._1 == relTypeComp) {
            poslabel += tagged._2 + "_";
            lemma += tagged._3 + "_";
          }
        }
      }
      poslabel = poslabel.slice(0, poslabel.length - 1) + "~"
      lemma = lemma.slice(0, lemma.length - 1) + "~"

    //Remove the last "_"
    }
    poslabel = poslabel.slice(0, poslabel.length - 1)
    lemma = lemma.slice(0, lemma.length - 1)
     
    val lemmaNode = TextNode(id = ID.text_id(lemma, 1), text = lemma)
    val lemmaRelType = EdgeType(id = ID.reltype_id(poslabel), label = poslabel)
    return (relType, (lemmaNode, lemmaRelType));
    
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

def logChunkGeneral(sentence:String, root:Vertex): List[(List[String], String)] = {
  logChunk(sentence, root);

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
          //val edge = getBaseRel(edges(i).trim);
          val edge = edges(i).trim;
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
    val taggedSentence = lemmatiser.posTag(sentence)
    val untaggedSentence = """\s""".r.split(sentence);
    var possibleParses: List[(List[String], String)] = List()

    
    for(i <- 0 to taggedSentence.length-3) {
      
  	  val current = taggedSentence(i)
  	  
  	  val lookahead1 = taggedSentence(i+1)
  	  val lookahead2 = taggedSentence(i+2)
      
      (current, lookahead1, lookahead2) match{
    		  case ((word1, tag1), (word2, tag2), (word3, tag3)) => 
    		  //println(word2 + ", " + tag2)
    		  if(relRegex.findAllIn(tag2).length == 1) {
    		  	//println("verb: " + word2)
            var nodeTexts: List[String] = List()
            var node1Text = "" 
            var edgeText = ""
            var node2Text = ""
            
    		  
    		    if(relRegex.findAllIn(tag1).length == 0) {
    		  	  
    		  	  //Anything before the edge goes into node 1
    		  	  //inEdge = 1;
    		  	  edgeText += untaggedSentence(i+1)

    		  	  
    		  	  for (j <- 0 to i) {
    		  	  	taggedSentence(j) match {
    		  	  		case (word, tag) => 
                  
                    node1Text = node1Text + " " + untaggedSentence(j)  

                  
                  

    		  	  	}

    		  	  }
              

    		  	  for (j <- i+2 to taggedSentence.length-1) {
    		  		taggedSentence(j) match {
    		  	  		case (word, tag) => 
                    if(relRegex.findAllIn(tag).length==0) {
                      node2Text = node2Text + " " + untaggedSentence(j)  
                      //Increment the edgeTextCounter by 1 to indicate that a further role is being introduced:
                      
                      //inEdge = 0;
                   
                    }
                    else {
                      
                      edgeText = edgeText + " " + untaggedSentence(j);
                    }

    		  	  	}
    		  	  }
    		  	  println("POS Chunk: " + node1Text.trim + " ," + edgeText.trim + " ," + node2Text.trim)
    		  	  val nodes = List(node1Text.replace("`", "").replace("'", "").trim, node2Text.replace("`", "").replace("'", "").trim)
    		  	  possibleParses = (nodes, edgeText) :: possibleParses
              

    		  	}

          }


    	  }
    }
    return possibleParses.reverse;
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


def getOrCreate(id:String, user:Option[Vertex] = None, textString:String = "", root:Vertex = TextNode("", "")):Vertex={
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
      
      val rootNode = TextNode(id=ID.usergenerated_id("chihchun_chen", "toad"), text="toad")
      val userNode = UserNode(id="user/chihchun_chen", username="chihchun_chen", name="Chih-Chun Chen")
  	  val sentence = args.reduceLeft((w1:String, w2:String) => w1 + " " + w2)
      println("From command line with general: " + sentence)
      val parses1 = sentenceParser.parseSentence(sentence, user = Some(userNode))
      for(node1 <- parses1._1) {
        node1 match {
          case n: Vertex => println(n.id)
          case _ =>
        }
      }
      for(edge <- parses1._2) {
        edge match {
          case n: Vertex => println(n.id)
          case _ =>
        }
      }
      for(node2 <- parses1._3) {
        node2 match {
          case n: Vertex => println(n.id)
          case _ =>
        }
      }

      
      val parses = sentenceParser.parseSentenceGeneral(sentence, user = Some(userNode))
      
      for(parse <- parses) {
        parse match {
          case (n: List[Vertex], r: Vertex) =>
          for(node <- n) {
            println("Node: " + node.id);
          }
          println("Rel: " + r.id)
        }
      }
	}
}
