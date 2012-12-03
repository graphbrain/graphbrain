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
  val hashRegex = """#""".r
  val disambigRegex = """(\()(.+?)(\))""".r
  val urlRegex = """([\d\w]+?:\/\/)?([\w\d\.\-]+)(\.\w+)(:\d{1,5})?(\/\S*)?""".r // See: http://stackoverflow.com/questions/8725312/javascript-regex-for-url-when-the-url-may-or-may-not-contain-http-and-www-words?lq=1
  val urlStrictRegex = """(http|ftp|https):\/\/[\w\-_]+(\.[\w\-_]+)+([\w\-\.,@?^=%&amp;:/~\+#]*[\w\-\@?^=%&amp;/~\+#])?""".r
  val gbNode = store.createTextNode(namespace="1", text="GraphBrain")
  val asInRel = ID.reltype_id("as in", 1)

  val verbRegex = """VB[A-Z]?""".r
  val adverbRegex = """RB[A-Z]?""".r
  val prepositionRegex = """(IN[A-Z]?)|TO""".r
  val relRegex = """(VB[A-Z]?)|(RB[A-Z]?)|(IN[A-Z]?)|TO|DT""".r
  val nounRegex = """NN[A-Z]?""".r
  val leftParenthPosTag = """-LRB-""".r
  val rightParenthPosTag = """-RRB-""".r
  val ownRegex = """('s\s|s'\s)""".r

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
  def specialNodeCases(inNodeText: String, root: Vertex = store.createTextNode(namespace="", text="GBNoneGB"), user: Option[UserNode]=None): (Vertex, Option[(List[Vertex], Vertex)]) = {
    user match {
      case Some(u:UserNode) =>
        if(u.username == inNodeText || u.name == inNodeText || inNodeText == "I" || inNodeText == "me") {
          return (u, None);
        }
      case _ => 
    }

    root match {
      case a: TextNode =>
        if(a.text == inNodeText || a.text.toLowerCase.indexOf(inNodeText.toLowerCase)==0 || inNodeText.toLowerCase.indexOf(a.text.toLowerCase) == 0) {
          return (a, None);
        }
        //Check whether already in database - global and user; create new node if necessary
        user match {
          case Some(u:UserNode) => 
            val userThingID = ID.usergenerated_id(u.username, a.text)
            
            if(nodeExists(userThingID)) {
              if(inNodeText==a.text) {
                return (a, None);
              
              }
            }
          case _ => 
        }
      case _ =>
    }
    return textToNodes(text = inNodeText, user=user)(0);

  }

  

  def reparseGraphTexts(nodeTexts: List[String], relText: String, disambigs: List[(String, String)], root: Vertex = store.createTextNode(namespace="", text="GBNoneGB"), user: Option[UserNode]=None): (List[(Vertex, Option[(List[Vertex], Vertex)])], Vertex) = {
   //println(relText)
    var tempDisambs = disambigs;

    var nodes: List[(Vertex, Option[(List[Vertex], Vertex)])] = Nil
      
    val sepRelations = """~""".r.split(relText)
    var i = 0;
      
    var newRelation = ""
          
    for(nodeText <- nodeTexts) {
      var d = "";

      var dNode: Option[(List[Vertex], Vertex)] = None;

      if(tempDisambs.length > 0){
        if(nodeText == tempDisambs.head._1) {
          d = tempDisambs.head._2;
          val disambigEdgeType = store.createEdgeType(id = ID.reltype_id("as in"), label = "as in")
          dNode = Some(List(specialNodeCases(d, root, user)._1), disambigEdgeType)

          tempDisambs = tempDisambs.tail;
        }
      }
        
      if(nodeText.toLowerCase == "you" || nodeText.toUpperCase == "I") {
        if(nodeText.toLowerCase == "you") {
          nodes = (gbNode, dNode) :: nodes;
        } 
        else {
          user match {
            case Some(u: UserNode) => nodes = (u, dNode) :: nodes;
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
        val newNodes = specialNodeCases(nodeText, root, user)
        nodes = newNodes :: nodes 
        
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
    val newRelText = newRelation.trim.slice(0, newRelation.length).trim;
    var relationV = store.createEdgeType(id = ID.reltype_id(newRelText), label = newRelText)
    return (nodes.reverse, relationV)

  }


  def parseSentenceGeneral(inSent: String, root: Vertex = store.createTextNode(namespace="", text="GBNoneGB"), user: Option[UserNode]=None): List[ResponseType] = {
    var inSentence = inSent;
    
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

    //Try segmenting with square bracket syntax.
    var parses = strictChunk(inSentence, root);

    //Check for disambiguation syntax

    
    //Only parse with POS if nothing returned:
    if(parses==(Nil, "", Nil)) {
      parses = posChunkGeneral(inSentence, root)
    }
    val solutions = reparseGraphTexts(parses._1, parses._2, parses._3, root, user);

    responses = GraphResponse(solutions::Nil) :: responses

    if(question > search._1 && question <= 0.5) {
      responses = HardcodedResponse(List("Sorry, I don't understand questions yet.")) :: responses
    }
    else if (search._1 <= 0.5){
      responses = SearchResponse(List(search._2)) :: responses
    }

    //This will be the first in the list - so parsing favours graph responses over hardcoded etc.
    
    return responses.reverse;
    
    
  }

  def isUser(text: String, user: Option[Vertex] = None): Boolean = {
    //println("isUserText: " + text)
    user match {
      case Some(u: UserNode) =>
        val userName = u.username;
        //println("username: " + userName);
        val name = u.name;
        //println("name: " + name);
        //println("isUser: " + (text.toLowerCase.indexOf(userName.toLowerCase)==0 || userName.toLowerCase.indexOf(text.toLowerCase) ==0 || text.toLowerCase.indexOf(name.toLowerCase) ==0 || name.toLowerCase.indexOf(text.toLowerCase) == 0))
        return(text.toLowerCase.indexOf(userName.toLowerCase)==0 || userName.toLowerCase.indexOf(text.toLowerCase) ==0 || text.toLowerCase.indexOf(name.toLowerCase) ==0 || name.toLowerCase.indexOf(text.toLowerCase) == 0) 
      case _ => return false; 
    }
  }

  def getUserNode(user: Option[Vertex] = None): UserNode = {
    user match {
      case Some(u: UserNode) => return u;
      case _ => throw new Exception("No user found")
    }
  }

  

  def isPossessed(text: String): Boolean = {

    if(ownRegex.findAllIn(text).hasNext) return true;

    val tagged = lemmatiser.posTag(text) 
    for(taggedText <- tagged){
      val tag = taggedText._2;
      val component = taggedText._1;
      if(tag.toUpperCase=="POS") {
        return true;
      }
      else if((verbRegex.findAllIn(tag).hasNext && component == "has")||(verbRegex.findAllIn(tag).hasNext && component == "have")) {
        return true
      }

    }
    return false;
  }


  def getOwnerOwned(text: String) : (String, String) = {
    var owner = "";
    var owned = "";

    if(ownRegex.findAllIn(text).hasNext) {
      val splitText = ownRegex.split(text);
      if(splitText.length==2) {
        owner = splitText(0).trim
        owned = splitText(1).trim
        return (owner, owned)
      }
    }
    val tagged = lemmatiser.posTag(text) 
    
    var posFound = false;
    for(taggedText <- tagged){

      val tag = taggedText._2;
      val component = taggedText._1;

      if(tag.toUpperCase=="POS") {
        posFound = true;
      }
      else {
        if(posFound) {
          owned += component.trim;
        }
        else {
          owner += component.trim;
        }
      }
    }
    if(posFound) {return (owner, owned)}
    var hasFound = false;
    for(taggedText <- tagged){

      val tag = taggedText._2;
      val component = taggedText._1;

      if((verbRegex.findAllIn(tag).hasNext && component == "has")||(verbRegex.findAllIn(tag).hasNext && component == "have")) {
        hasFound = true;
      }
      else {
        if(hasFound) {
          owned += component.trim;
        }
        else {
          owner += component.trim;
        }
      }
    }
    return (owner, owned)
  }

  val instanceOwnedByRelType = store.createEdgeType(id = ID.reltype_id("instance_of~owned_by"));

  def textToNodes(text:String, node: Vertex = store.createTextNode(namespace="", text="GBNoneGB"), user:Option[Vertex]=None): List[(Vertex, Option[(List[Vertex], Vertex)])] = {
    
    var results: List[(Vertex, Option[(List[Vertex], Vertex)])] = Nil
    
    if(isUser(text, user) && !isPossessed(text)) {
      user match {
        case Some(u:UserNode) => 
          //println("isUser");
          results = (u, None) :: results;
        case _ => 
      }
    }

    else if(!isPossessed(text)) {
      //println("Not possessed")
      val nodes = textToNode(text.trim, user = user);
      for (node <- nodes) {
        results = (node, None) :: results;
      }
    }
    else {
      //println("Possessed")
      val ownerOwned = getOwnerOwned(text)
      //println("Owner: " + ownerOwned._1)
      if(isUser(text = ownerOwned._1, user = user)) {
        //println("User possessed")
        
        val ownerNode = getUserNode(user)
        val userName = ownerNode.username
        val ownedNodes = textToNode(ownerOwned._2); 
        for(ownedNode <- ownedNodes){
          ownedNode match {
            case o: TextNode => val ownedText = o.text;
              val accessoryVertices = (List(ownedNode, ownerNode), instanceOwnedByRelType);
              val newNode = getNextAvailableUserOwnedNode(ownedText, userName);
              results = (newNode, Some(accessoryVertices)) :: results;
            case _ =>
          }
        }
          
          
      }
      else {
        val ownerNodes = textToNode(ownerOwned._1)
        val ownedNodes = textToNode(ownerOwned._2)
        for(ownerNode <- ownerNodes) {
        
          for (ownedNode <- ownedNodes) {
         
            ownedNode match {
              case o: TextNode => val ownedText = o.text;
                val accessoryVertices = (List(ownedNode, ownerNode), instanceOwnedByRelType);
                val newNode = getNextAvailableNode(ownedText, 2);
                results = (newNode, Some(accessoryVertices)) :: results;
                //println("Owner: " + ownerNode.id + " Owned: " + ownedNode.id + " Node: " + newNode.id)
              case _ =>
 
            }
          }
        }
      }
    }
    //println("Length results: " + results.length)
    return results.reverse
    
  }

  def getNextAvailableNode(text: String, startCounter: Int = 1): Vertex = {
    var i = startCounter;
    while(nodeExists(ID.text_id(text, i))) {
      i +=1
    }
    return store.createTextNode(namespace = i.toString, text=text);
  }

  def getFirstFoundNode(text: String, startCounter: Int = 1): Vertex = {
    var i = startCounter;
    while(!nodeExists(ID.text_id(text, i))) {
      i +=1
    }
    return store.createTextNode(namespace = i.toString, text=text);
  }


  def getNextAvailableUserOwnedNode(text: String, username: String, startCounter: Int = 1): Vertex = {
    var i = startCounter;
    while(nodeExists(ID.personalOwned_id(username, text, i))) {
      i +=1;
    }
    val ns = "user/" + username + "/p/" + i.toString; 
    return store.createTextNode(namespace = ns, text = text);
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
    poslabel = poslabel.slice(0, poslabel.length-2).trim
    lemma = lemma.slice(0, lemma.length-2).trim
     
    val lemmaNode = store.createTextNode(namespace="1", text=lemma)
    val lemmaRelType = store.createEdgeType(id = ID.reltype_id(poslabel), label = poslabel)
    return (relType, (lemmaNode, lemmaRelType));
    
  }

def strictChunk(sentence: String, root: Vertex): (List[String], String, List[(String, String)]) = {
  
  val nodeTexts = nodeRegex.findAllIn(sentence);
  if(!nodeTexts.hasNext) {
    return (Nil, "", Nil);
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
  return (nodes, edge, Nil)

}


def checkTags(lemmatisedSentence1: (String, String, String), lemmatisedSentence2: (String, String, String), quoteTaggedSentence1: (String, String), quoteTaggedSentence2: (String, String)): ((String, String, String), (String, String)) = {
  def currentSame = quoteTaggedSentence1._1.trim == lemmatisedSentence1._1.trim
  def nextSame = quoteTaggedSentence2._1.indexOf(lemmatisedSentence2._1)==0 || lemmatisedSentence2._1.indexOf(quoteTaggedSentence2._1)==0
  def quoteAhead = lemmatisedSentence1._1.trim + lemmatisedSentence2._1.trim == quoteTaggedSentence1._1
  def lemAhead = lemmatisedSentence2._1.trim == quoteTaggedSentence1._1 + quoteTaggedSentence2._1;
  def nextQuoteSuperstring = quoteTaggedSentence2._1.trim.indexOf(lemmatisedSentence2._1.trim)==0 && quoteTaggedSentence2._1.trim.length > lemmatisedSentence2._1.trim.length
  def nextLemSuperstring = lemmatisedSentence2._1.trim.indexOf(quoteTaggedSentence2._1.trim)==0 && quoteTaggedSentence2._1.trim.length < lemmatisedSentence2._1.trim.length
  def lemLarger = quoteTaggedSentence1._1.trim.length < lemmatisedSentence1._1.trim.length 
  def quoteLarger = quoteTaggedSentence1._1.trim.length > lemmatisedSentence1._1.trim.length
  def nextQuoteURL = quoteTaggedSentence2._2=="URL" && quoteLarger;

  if(lemmatisedSentence2._1.trim==quoteTaggedSentence2._1.trim) return (lemmatisedSentence2, quoteTaggedSentence2);
  else if(quoteAhead) return (lemmatisedSentence2, quoteTaggedSentence1);
  else if(nextQuoteURL && quoteLarger && currentSame) return (lemmatisedSentence2, quoteTaggedSentence1)
  else if(quoteLarger && nextQuoteSuperstring) return (lemmatisedSentence2, quoteTaggedSentence1)
  else if(lemLarger && nextLemSuperstring) return (lemmatisedSentence1, quoteTaggedSentence2)
  else return (lemmatisedSentence2, quoteTaggedSentence2);

}

def posChunkGeneral(sentence: String, root: Vertex): (List[String], String, List[(String, String)])={
  val sanSentence = TextFormatting.deQuoteAndTrim(sentence)
  
  var taggedSentence = lemmatiser.annotate(sanSentence);
  var quoteTaggedSentence = InputSyntax.quoteAndDisambigTag(InputSyntax.quoteURL(sentence));
  
  var inEdge = false;
  var inQuote = false;
  var quoteCounter = 0;

  var nodeTexts: List[String] = List()
  var disambigs: List[(String, String)] = List() //First tuple stores the text, the second stores the disambiguation.
  var edgeText = ""
  var nodeText = ""
  var currentSplitQuote =""


  while(taggedSentence.length > 1 || quoteTaggedSentence.length > 1) {
      
    val current = taggedSentence.head 
    val lookahead = taggedSentence.tail.head 
    val currentQuote = quoteTaggedSentence.head
    val nextQuote = quoteTaggedSentence.tail.head 
    //println(current + " " + lookahead + " " + currentQuote + " " + nextQuote)
   

    (current, lookahead, currentQuote, nextQuote) match{
      case ((word1, tag1, lem1), (word2, tag2, lem2), (qw1, qt1), (qw2, qt2)) => 
        
        if(qt1=="InQuote") {

          nodeText += qw1 + " ";
          if(qt2 == "NonQuote") {
            nodeTexts = TextFormatting.deQuoteAndTrim(nodeText) :: nodeTexts;
            nodeText = ""
          }
          
        }
        else if(qt1=="URL") {
          
          nodeTexts = TextFormatting.deQuoteAndTrim(qw1) :: nodeTexts;
          val urlProcessed = InputSyntax.resolveURL(qw1, taggedSentence, quoteTaggedSentence);
          taggedSentence = urlProcessed;
        }

        else if(relRegex.findAllIn(tag1).toArray.length == 1) {
          edgeText += word1.trim + " "

          if(relRegex.findAllIn(tag2).toArray.length == 0) {
            edgeText = edgeText.trim + "~"
              
          }

          
        }
        else if (relRegex.findAllIn(tag1).toArray.length == 0) {
          if(hashRegex.findAllIn(word1).toArray.length==1) {
            val hashProcessed = InputSyntax.hashedWords(nodeText.head.toString, disambigs, taggedSentence, quoteTaggedSentence);
            disambigs = hashProcessed._1;
          }
          else {
            nodeText += word1.trim + " "  
          }
          if(relRegex.findAllIn(tag2).toArray.length == 1) {

            nodeTexts = TextFormatting.deQuoteAndTrim(nodeText) :: nodeTexts;
            nodeText = ""
          }
          
        }
        if(leftParenthPosTag.findAllIn(tag1).toArray.length == 1 && qt1!="URL") {
            val parenthProcessed = InputSyntax.disambig(nodeText.head.toString, disambigs, taggedSentence, quoteTaggedSentence);
            disambigs = parenthProcessed._1;
            taggedSentence = parenthProcessed._2;
            quoteTaggedSentence = parenthProcessed._3;

          //}
        }
        if (quoteTaggedSentence.length == 2) {

          nodeText += qw2.trim;
          nodeTexts = TextFormatting.deQuoteAndTrim(nodeText) :: nodeTexts;
          nodeTexts = nodeTexts.reverse;
          edgeText = edgeText.substring(0, edgeText.length-1);
          //println(edgeText)
    


    return (nodeTexts, edgeText, disambigs);
          

        }    
        
      }
      val newPair = checkTags(current, lookahead, currentQuote, nextQuote)
      if(newPair._1==lookahead) {
        taggedSentence = taggedSentence.tail;  
      }
      else {
        taggedSentence = taggedSentence
      }
      if(newPair._2==nextQuote) {
        quoteTaggedSentence = quoteTaggedSentence.tail
      }
      else {
        quoteTaggedSentence = quoteTaggedSentence;  
      }
      
    }

    
    nodeTexts = nodeTexts.reverse;
    edgeText = edgeText.substring(0, edgeText.length-1);
    //println(edgeText)
    


    return (nodeTexts, edgeText, disambigs);
  }

def findOrConvertToVertices(possibleParses: List[(List[String], String)], root: Vertex, user:Option[Vertex], maxPossiblePerParse: Int = 10): List[(List[Vertex], Edge)]={
    
    var userID = ""
    user match {
        case u:UserNode => userID = u.username;
        case _ => 

    }
	var possibleGraphs:List[(List[Vertex], Edge)] = List()
	val sortedParses = removeDeterminers(sortRootParsesPriority(possibleParses, root), root)

  //println("Sorted parses: " + sortedParses.length)

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
        //println("Limit: " + limit)
				for(i <- 0 to limit-1) {
				  val result = try {results(i) } catch { case e => ""}
				  val resultNode = getOrCreate(result, user, nodeText, root)
				  //println("Node: " + resultNode.id)

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
			  //println("Edge: " + edge)
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
                  case (n: List[(Vertex, Option[(List[Vertex], Vertex)])], r: Vertex) =>
                  for(node <- n) {
                    
                    node match {
                      case (nd: TextNode, None) => println("Node: " + nd.id);
                      case (nd: TextNode, Some(aux: (List[Vertex], Vertex))) => 
                      println("Node with aux: " + nd.id)
                        aux match {
                          case (a:List[TextNode], ed:EdgeType) => 
                            for(aNode <- a) {
                              println("auxNode: " + aNode.id)
                            }
                            println("auxEdge: " + ed.id)
                          case _ => println("mismatch")
                        }
                      case _ => println("No match")
                    }
                  }
                  println("Rel: " + r.id)
                }

              }
            case r: ResponseType => println(r)

          }
 
      }

	}
}
