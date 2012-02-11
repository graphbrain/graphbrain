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
      case _ => return false;
    }
  }

  def checkMatch(posExpression:POS, str_to_match:String):Boolean={
    val pos = new Regex(posExpression.exp)

    str_to_match match {
      case pos(a) => return true;
      case _ => return false;
    }
  }

  def checkMatch(graph2Expression:GRAPH2, graph2_to_match:(String, String, String)):Boolean={
    graph2_to_match match{
      case (graph2Expression.source, graph2Expression.relation, graph2Expression.target) => return true;
      case _ => return false
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
    return ("","","")
  }

  def transform(inExp:REGEX, outExp:GRAPH2, input:String):(String, String, String)={
    return ("","","")
  }

  def transform(inExp:GRAPH2, outExp:GRAPH2, input:GRAPH2):(String, String, String)={
    return ("","","")
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
