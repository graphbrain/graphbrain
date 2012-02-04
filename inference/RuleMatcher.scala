import java.net.URL
import scala.io.Source
import scala.util.matching.Regex
import scala.util.
import scala.xml._
import scala.collection.mutable;


object RuleMatcher {

  //Reverse rules have oldRelation as the source, newRelation as the target and type relation rule.
  //Text and POS rules replace a segment of text and can apply to both nodes and relations. 
  //In text rules, the source is a regex expression or string that is being searched for and the target is the replacement expression.
  //In POS rules, the source is a POS expression or string that must be matched and the target is a text rule or another POS rule. The 
  //POS expression can also be a regular expression with missing/optional values.
  



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
    val matches=regex.findAllIn(inString)
    return regex.replaceAllIn(inString, outSegment)

  }


  def reverse(sourceNode:String, targetNode:String, oldRelation:String, newRelation:String): (String, String, String) = {
      
      return (targetNode, newRelation, sourceNode)
    }


  }



  def main(args : Array[String]) : Unit = {

  	println(checkMatch(args(0), args(1)))
    println(replace(args(0), args(1), args(2)))

  		
  	
  }
}
