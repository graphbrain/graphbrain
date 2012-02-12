import java.net.URL
import scala.io.Source
import scala.util.matching.Regex
import scala.collection.mutable;
import scala.collection.immutable;
import scala.collection.mutable


object RuleMatcher {

  //Text rules are the base level primitives and simply replace a segment of text (which can be the label for a node or link, or simply a sentence). These are stored in graph form, with the source as the expression (string or regex) string to be replaced, the target is the replacement expression, and the relation "replaces".
  //TextMatch and POS rules replace a segment of text and can apply to both nodes and relations. 
  //In TextMatch rules, the source is a string or regex expression that must be matched and the target is another rule.
  //In POS rules, the source is a POS expression or string that must be matched and the target is another rule. 


  //Structural rules change the structure of nodes and links. For example, "expand" relations define conversions from text into node-link-nodes (expression identifying the relation as the source, "expands to" as the source, which occurrence of the expression to segment by as the target) while "reverse" rules reverse the node-link-node relation by replacing it with a new relation in the opposite direction (oldRelation as the source, newRelation as the target).
  //TODO: Change expand and iterConcat

  //Composite rules are defined using the standard logical operators as relations. Two rules linked by an OR relationship generate are both subtypes of this rule, whereas two rules linked by an AND relationship give rise to a subtype. If A and B are linked by the subtype relationship such that A is_subtype_of B, then we know that if rule node A is activated, then B must also be activated. 
  


  //Returns true if str_to_match matches the regex_condition.
  def checkMatch(regex_condition:String, str_to_match:String) : Boolean = {
    val regex = new Regex(regex_condition)
    
    str_to_match match{
      case regex(a) => return true;
      case _ => return false;
    }
    return false;
    
  }


  //Returns the parts of speech of the text (indexed by the tagged fragments).
  def getPOS(stringInput:String):immutable.HashMap[String, String]={
    val text_pos_map=immutable.HashMap(stringInput -> stringInput)
    
    return text_pos_map;
  }

  //Returns the parts of speech as a string (which can then be matched to the expression stored in the rule).
  def getPOSString(stringInput:String):String={
    val posMap=getPOS(stringInput)
    val pos=posMap.values
    return pos.mkString(" ");
  }

  def process_pos_rule(ruleSource:String, textRule:(String,String), content:String): String= {
    val posString=getPOSString(content)
    
    if(checkMatch(ruleSource, posString))
    {
      return process_text_rule(textRule, content)
    }
    return content;
  }

//Returns the output of applying the rule (content string is immutable)
  def process_text_rule(textRule:(String, String), content:String): String = {
    textRule match {
      case (replaceSubstring, replacement) => return replace(content, replaceSubstring, replacement)
    }
    return content;
  }

  //Replaces the all substring matches of replaceSegment in inString with outSegment
  //e.g. replace("Hello hello", "hello", "goodbye") returns "Hello goodbye"
  def replace(inString:String, replaceSegment:String, outSegment:String) : String = {
    val regex=new Regex(replaceSegment)
    return regex.replaceAllIn(inString, outSegment)

  }

  //Reverses the source-relation-target and replaces the relation with the reverse relation.
  def reverse(sourceNode:String, targetNode:String, oldRelation:String, newRelation:String):(String, String, String) = {
      return (targetNode, newRelation, sourceNode)
  }
  

  //Expands a string (which could be a node or edge) making the substring before the relation the
  //source node and the substring after the relation the target using only the leftmost match
  //so that if another instance of the relaton string appears later, it is merged in the target node.
  def expand(nodeOrEdge:String, relationText:String):(String, String, String)={
      
      val splits=nodeOrEdge.split(relationText)
      return (splits(0), relationText, iterConcat(splits, relationText, 1))

  }

  //Concatenates the set of strings strArray with toInsert as the delimiter starting from the element indexed by startIndex.
  def iterConcat(strArray:Array[String], toInsert:String, startIndex:Int):String={
    var returnString=strArray(startIndex)
    for(i <- startIndex+1 until strArray.length)
    {      
      returnString=returnString.concat(toInsert+strArray(i));
    }
    return returnString
  }



  def main(args : Array[String]) : Unit = 
  {
  	 		
  }
}
