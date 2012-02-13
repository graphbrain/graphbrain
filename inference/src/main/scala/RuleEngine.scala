import java.net.URL
import scala.io.Source
import scala.util.matching.Regex
import scala.collection.mutable;
import scala.collection.immutable;
import scala.collection.mutable


object RuleEngine {

  


    //Returns true if str_to_match matches the regex_condition.
  def checkMatch(regexExpression:REGEX, str_to_match:String):Boolean={
    
    val regex = new Regex(regexExpression.exp)

    str_to_match match {
      case regex(a) => return true;
      case b if b==regexExpression.exp => true
      case _ => return false;
    }
  }

  def checkMatch(posExpression:POS, str_to_match:String):Boolean={
    val tags = POSTagger.getTokenSequence(str_to_match)
    val regexPOS = new Regex(posExpression.exp)
    tags match {
      case regexPOS(a) => return true;
      case t if t==posExpression.exp => true;
      case _ => return false;
    }
  }

  def checkMatch(graph2Expression:GRAPH2, graph2_to_match:(String, String, String)):Boolean={
    graph2_to_match match{
      case (graph2Expression.source, graph2Expression.relation, graph2Expression.target) => return true;
      case _ => return false
    }
  }

  def checkMatch(expression:RuleExpression, graph2_to_match:(String, String, String)):Boolean={
    expression match{
      case a:GRAPH2 => return checkMatch(expression, graph2_to_match)
      case _ => return false
    }
  }

  def checkMatch(expression:RuleExpression, string_to_match:String):Boolean={
    expression match{
      case a:REGEX => return checkMatch(expression, string_to_match)
      case a:POS => return checkMatch(expression, string_to_match)
      case _ => return false
    }
  }

  def checkMatch(expression:COMPOSITE, input_to_match:String):Boolean={

    expression match{
     //Recursively check each relation
      case COMPOSITE(a, "AND", b) => return checkMatch(a, input_to_match)&&checkMatch(b, input_to_match)
      case COMPOSITE(a, "OR", b) => return checkMatch(a, input_to_match)|checkMatch(b, input_to_match)
      case _ => return false;
    }
  }

  def checkMatch(expression:COMPOSITE, input_to_match:(String, String, String)):Boolean={

    expression match{
     //Recursively check each relation
      case COMPOSITE(a, "AND", b) => return checkMatch(a, input_to_match)&&checkMatch(b, input_to_match)
      case COMPOSITE(a, "OR", b) => return checkMatch(a, input_to_match)|checkMatch(b, input_to_match)
      case _ => return false;
    }
  }


  def transform(inExp:POS, outExp:REGEX, input:String):String={
    return ""
  }

  def transform(inExp:REGEX, outExp:REGEX, input:String):String={
    val regex=new Regex(inExp.exp)
    return regex.replaceAllIn(input, outExp.exp)

  }

  def transform(inExp:GRAPH2, outExp:REGEX, input:GRAPH2):String={
    return ""
  }

  def transform(inExp:POS, outExp:GRAPH2, input:String):(String, String, String)={
    val taggedTokens=POSTagger.tagText(input)
    var pre=""
    var post=""
    var rel=""
    var found=false;
    for(tt <- taggedTokens)
    {
      tt match{
        case (word, tag) if tag==inExp.exp => rel=outExp.relation; found=true; 
        case (word, tag) if found => post=post+word;
        case (word, tag) if found==false => pre=pre+word
      }
    }
    if(outExp.source < outExp.target)
    {

      return (pre, rel, post)  
    }
    else
    {
      return (post, rel, pre)
    }
    
  }

  def transform(inExp:REGEX, outExp:GRAPH2, input:String):(String, String, String)={
    return ("","","")
  }

  def transform(inExp:COMPOSITE, outexp:GRAPH2, input1:GRAPH2, input2:GRAPH2):(String, String, String)={
    return ("", "", "")
  }


  def GRAPH_REVERSE(inExp:GRAPH2, outExp:GRAPH2):Boolean={return inExp.source==outExp.target&&inExp.target==outExp.source}

  def GRAPH_NO_REVERSE(inExp:GRAPH2, outExp:GRAPH2):Boolean = {return (!(GRAPH_REVERSE(inExp, outExp)))}

  def GRAPH_RELATION_REPLACE(inExp:GRAPH2, outExp:GRAPH2):Boolean={return (inExp.relation!=outExp.relation)}

  def GRAPH_RELATION_KEEP(inExp:GRAPH2, outExp:GRAPH2):Boolean={return (!(GRAPH_RELATION_REPLACE(inExp, outExp)))}

  //Applies graph transformations depending on the patterns in the rule input and output expressions.
  def transform(inExp:GRAPH2, outExp:GRAPH2, input:GRAPH2):(String, String, String)={

    if(GRAPH_NO_REVERSE(inExp, outExp)&&GRAPH_RELATION_REPLACE(inExp, outExp))
    { 
      //Simply replace relation name (used for synonyms or type inferred relations)
      return (input.source, outExp.relation, input.target)
    }
    else if(GRAPH_REVERSE(inExp, outExp)&&GRAPH_RELATION_KEEP(inExp, outExp))
    { 
      //Reverse without changing the name of the relation (used to symmetrise relations)
      return (input.target, input.relation, input.source)
    }
    else if(GRAPH_REVERSE(inExp, outExp)&&GRAPH_RELATION_REPLACE(inExp, outExp))
    { 
      //Reverse and replace relation name with new name given by outExp.relation:
      return (input.target, outExp.relation, input.source)  
    }
    else 
    {
      return (input.source, input.relation, input.target)
    }
  }


  
  //Returns the parts of speech of the text (indexed by the tagged fragments).
  private def getPOS(stringInput:String):immutable.HashMap[String, String]={
    val text_pos_map=immutable.HashMap(stringInput -> stringInput)
    
    return text_pos_map;
  }

  //Returns the parts of speech as a string (which can then be matched to the expression stored in the rule).
  private def getPOSString(stringInput:String):String={
    val posMap=getPOS(stringInput)
    val pos=posMap.values
    return pos.mkString(" ");
  }


//Returns the output of applying the rule (content string is immutable)
  private def process_text_rule(textRule:(String, String), content:String): String = {
    textRule match {
      case (replaceSubstring, replacement) => return replaceText(content, replaceSubstring, replacement)
    }
    return content;
  }

  //Replaces the all substring matches of replaceSegment in inString with outSegment
  //e.g. replace("Hello hello", "hello", "goodbye") returns "Hello goodbye"
  private def replaceText(inString:String, replaceSegment:String, outSegment:String) : String = {
    val regex=new Regex(replaceSegment)
    return regex.replaceAllIn(inString, outSegment)

  }

  //Reverses the source-relation-target and replaces the relation with the reverse relation.
  def reverseGraph(sourceNode:String, targetNode:String, oldRelation:String, newRelation:String):(String, String, String) = {
      return (targetNode, newRelation, sourceNode)
  }
  

  //Expands a string (which could be a node or edge) making the substring before the relation the
  //source node and the substring after the relation the target using only the leftmost match
  //so that if another instance of the relaton string appears later, it is merged in the target node.
  def expand2Graph(nodeOrEdge:String, relationText:String):(String, String, String)={
      
      val splits=nodeOrEdge.split(relationText)
      return (splits(0), relationText, iterConcatString(splits, relationText, 1))

  }

  //Concatenates the set of strings strArray with toInsert as the delimiter starting from the element indexed by startIndex.
  def iterConcatString(strArray:Array[String], toInsert:String, startIndex:Int):String={
    var returnString=strArray(startIndex)
    for(i <- startIndex+1 until strArray.length)
    {      
      returnString=returnString.concat(toInsert+strArray(i));
    }
    return returnString
  }


  
}
