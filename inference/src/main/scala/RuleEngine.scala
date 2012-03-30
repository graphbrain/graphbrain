package com.graphbrain.inference

import java.net.URL
import scala.io.Source
import scala.util.matching.Regex
import scala.collection.mutable;
import scala.collection.immutable;




object RuleEngine {

  /*def applyRules(rules:List[RULE], input:Any, output:mutable.ListBuffer[Any]):mutable.ListBuffer[Any]=
  {
    for(rule <- rules)
    {
      applyRule(rule, input) match{
        case Nil => //don't add
        case a:Any =>  output.appendAll(applyRules(rules, a, output))
      }
    }
    return output;
      
  }*/

  def applyRule(rule:RULE, input:Any):Any=
  {
    
    if(checkMatch(rule.condition, input)) 
    {
      
      return transform(rule.input, rule.output, input)
    }
    return Nil;
  }

  /*Checks match for rule given input. If the rule is intended for the wrong data type, the method returns false.*/
  def checkMatch(expression:Any, input_to_match:Any):Boolean={
    expression match{
      case a:REGEX => input_to_match match{
        case b:String => return checkMatch(a, b)
      }
      case a:POS => input_to_match match{
        case b:String => return checkMatch(a, b)
      }
      case a:GRAPH2 => input_to_match match{
        case (b:String, c:String, d:String) => return checkMatch(a, (b, c, d))
      }
      case a:COMPOSITE =>  return checkMatch(a, input_to_match)
      case a:GRAPH2PAIR =>  input_to_match match{
        case ((c:String,d:String,e:String), (f:String,g:String,h:String)) => return checkMatch(a, ((c,d,e),(f,g,h)))
      }
      
      case _ => return false
    } 
  }

  def transform(inExp:Any, outExp:Any, input:Any):Any={
    (inExp, outExp, input) match{
      case (i:REGEX, o:String, in:String) => return transform(i, o, in)
      case (i:GRAPH2, o:String, in:String) => return transform(i, o, in)
      case (i:POS, o:String, in:String) => return transform(i, o, in)
      case (i:POS, o:GRAPH2, in:String) => return transform(i, o, in)
      case (i:REGEX, o:GRAPH2, in:String) => return transform(i, o, in)
      case (i:GRAPH2, o:GRAPH2, (in1:String, in2:String, in3:String)) => return transform(i, o, (in1, in2, in3))
      case ((i1:GRAPH2,i2:GRAPH2), o:GRAPH2, ((in1:String, in2:String, in3:String), (in4:String, in5:String, in6:String))) => return transform((i1, i2), o, ((in1, in2, in3), (in4, in5, in6)))
      case (i:GRAPH2PAIR, o:GRAPH2, ((in1:String, in2:String, in3:String), (in4:String, in5:String, in6:String))) => return transform(i, o, ((in1, in2, in3), (in4, in5, in6)))
      case _ => return false
    }
  }

  /**
  Returns true if str_to_match matches the regex_condition defined in regexExpression.
  */
  def checkMatch(regexExpression:REGEX, str_to_match:String):Boolean={
    
    val regex = new Regex(regexExpression.exp)

    str_to_match match {
      case regex(a) => return true;
      case b if b==regexExpression.exp => true
      case _ => return false;
    }
  }

  /**
  Returns true if the POS tags in str_to_match match the POS expression defined in posExpression, e.g. if str_to_match is "I am a person", it would match with the POS expressions "(PRP VBP DT NN)", ".*(NN).*" and ".*(PRP VBP).*".
  */
  def checkMatch(posExpression:POS, str_to_match:String):Boolean={
    val tags = POSTagger.getTokenSequence(str_to_match)
    val regexPOS = new Regex(posExpression.exp)
    tags match {
      case regexPOS(a) => return true;
      case t if t==posExpression.exp => true;
      case _ => return false;
    }
  }

  /**
  Returns true if the (String, String, String) in graph_to_match representing (source, relation, target) matches the GRAPH2 expression i.e. the relation in graph_to_match is the same as that in graph2Expression. 
  */
  def checkMatch(graph2Expression:GRAPH2, graph_to_match:(String, String, String)):Boolean={
    graph_to_match match{
      case (a, graph2Expression.relation, b) => return true;
      case _ => return false
    }
  }

  

  

  /**
  Checks match for a composite expression for graph expression. If the rule is intended for another data type, the method returns false.
  */
  def checkMatch(expression:COMPOSITE, input_to_match:Any):Boolean={

    (expression.exp1, expression.operator, expression.exp2) match{
     //Recursively check each relation
      case (a:RuleExpression, "AND", b:RuleExpression) => input_to_match match{
        case c:String => return checkMatch(a, c)&&checkMatch(b, c)
        case (d:String, e:String, f:String) => return checkMatch(a, (d,e,f))&&checkMatch(b, (d,e,f))
        case ((d:String, e:String, f:String), (g:String, h:String, i:String)) => checkMatch(expression, ((d,e,f),(g,h,i)))
        case _ => return false;
      }      
      case (a:RuleExpression, "OR", b:RuleExpression) => input_to_match match{
        case c:String => return checkMatch(a, c)|checkMatch(b, c)
        case (d:String, e:String, f:String) => return checkMatch(a, (d,e,f))|checkMatch(b, (d,e,f))
        case ((d:String, e:String, f:String), (g:String, h:String, i:String)) => checkMatch(expression, ((d,e,f),(g,h,i)))
        case _ => return false;
      } 
      case _=> return false 
      
    }
  }

  /**
  Checks for an exact match such that expression1 matches with the first input tuple and expression2 matches with the second input tuple.
  */
  def checkMatch(expressions:GRAPH2PAIR, twoGraphInput:((String, String, String), (String, String, String))):Boolean={

    //Check if relations match
    if(checkMatch(expressions.exp1, twoGraphInput._1)&&checkMatch(expressions.exp2, twoGraphInput._2))
    {
      

      twoGraphInput match {
        case ((a, b, c) , (d, e, f)) if ((a==d)==(expressions.exp1.source==expressions.exp2.source))&&((c==f)==(expressions.exp1.target==expressions.exp2.target))&&((a==c)==(expressions.exp1.source==expressions.exp1.target))&&((d==f)==(expressions.exp2.source==expressions.exp2.target))&&((a==f)==(expressions.exp1.source==expressions.exp2.target))&&((c==d)==(expressions.exp1.target==expressions.exp2.source)) 
        => return true
          case _ => return false;
        }              
    }
    else
    {
      return false
    }
  }

  /**
  Checks match for two inputs to see whether the composite relation holds. Both the components of the composite relation need to be satisfied exactly once by one of the inputs (input_to_match1 and input_to_match2).
  */
  def checkMatch(expression:COMPOSITE, twoInputs_to_match:(Any, Any)):Boolean={

    (expression.exp1, expression.operator, expression.exp2) match{
     //Recursively check each relation
      case (a:GRAPH2, "AND", b:GRAPH2) => (twoInputs_to_match._1, twoInputs_to_match._2) match{
        case ((f:String, g:String, h:String), (i:String, j:String, k:String)) => return (checkMatch(GRAPH2PAIR(a, b), ((f,g,h), (i,j,k)))|checkMatch(GRAPH2PAIR(a, b), ((i,j,k), (f,g,h)))); 
        case _ => return false;
      }
      case (a:RuleExpression, "AND", b:RuleExpression) => (twoInputs_to_match._1, twoInputs_to_match._2) match{
        case (c:String, d:String) => return (checkMatch(a, c)&&checkMatch(b, d))|(checkMatch(a, d)&&checkMatch(b, c))
        case _ => return false;
      }
      case _=> return false      
      
    }
  }

  

  def transform(inExp:REGEX, outExp:String, input:String):String={
    val regex=new Regex(inExp.exp)
    return regex.replaceAllIn(input, outExp)

  }


  /**
  Replaces the word associated with the inExp POS with the string in outExp.
  */

  def transform(inExp:POS, outExp:String, input:String):String={
    val taggedTokens=POSTagger.tagText(input)
    
    var outString=""
    for(tt <- taggedTokens)
    {
      tt match{
        case (word, tag) if tag==inExp.exp => outString += outExp;
        case (word, tag) => outString += word;
        
      }
    }
    return outString
  }

  /**
  * Makes the word associated with the inExp POS the relation and the substrings either end the source and target (which becomes the source and which the target depends on the GRAPH2 expression - e.g. ('A', relation 'B') makes the preceding string the source, while ('B' relation 'A' makes the preceding string the target))
  */
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
    val regex=new Regex(inExp.exp)
    val strings = regex.split(input)
    var pre=""
    var rel=""
    var post=""
    if(strings.length>=2)
    {
      pre=strings(0)  
      val r=regex.findFirstIn(input)
      r match {
        case Some(a)=>rel=a.trim
        case None => rel
      }
      for(i<-1 to strings.length-1){post+=strings(i)}
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

  def transform(inExp:GRAPH2PAIR, outExp:GRAPH2, inputPair:((String, String, String), (String, String, String))):(String, String, String)={
    
    //Brute force checking for role matches
    if(checkMatch(inExp, inputPair))
    {
        return mergeGraphs(inExp, outExp, inputPair)

    }
    return ("", "", "")
  }

  //Applies graph transformations depending on the patterns in the rule input and output expressions.
  def transform(inExp:GRAPH2, outExp:GRAPH2, input:(String, String, String)):(String, String, String)={

    if(GRAPH_NO_REVERSE(inExp, outExp)&&GRAPH_RELATION_REPLACE(inExp, outExp))
    { 
      //Simply replace relation name (used for synonyms or type inferred relations)
      return (input._1, outExp.relation, input._3)
    }
    else if(GRAPH_REVERSE(inExp, outExp)&&GRAPH_RELATION_KEEP(inExp, outExp))
    { 
      //Reverse without changing the name of the relation (used to symmetrise relations)
      return (input._3, input._2, input._1)
    }
    else if(GRAPH_REVERSE(inExp, outExp)&&GRAPH_RELATION_REPLACE(inExp, outExp))
    { 
      //Reverse and replace relation name with new name given by outExp.relation:
      return (input._3, outExp.relation, input._1)  
    }
    else 
    {
      return (input._1, input._2, input._3)
    }
  }
  private def mergeGraphs(inExp:GRAPH2PAIR, outExp:GRAPH2, inputPair:((String, String, String), (String, String, String))):(String, String, String)=
  {
    inputPair match{
        //Brute force checjing of role matches to return the correct output. Very ugly code but works.
        case ((a,b,c),(d,e,f)) if (outExp.source==inExp.exp1.source)&&(outExp.target==inExp.exp1.source) => return (a, outExp.relation, a)
        case ((a,b,c),(d,e,f)) if (outExp.source==inExp.exp1.source)&&(outExp.target==inExp.exp1.target) => return (a, outExp.relation, c)
        case ((a,b,c),(d,e,f)) if (outExp.source==inExp.exp1.source)&&(outExp.target==inExp.exp2.source) => return (a, outExp.relation, d)
        case ((a,b,c),(d,e,f)) if (outExp.source==inExp.exp1.source)&&(outExp.target==inExp.exp2.target) => return (a, outExp.relation, f)
        case ((a,b,c),(d,e,f)) if (outExp.source==inExp.exp1.target)&&(outExp.target==inExp.exp1.source) => return (c, outExp.relation, a)
        case ((a,b,c),(d,e,f)) if (outExp.source==inExp.exp1.target)&&(outExp.target==inExp.exp1.target) => return (c, outExp.relation, c)
        case ((a,b,c),(d,e,f)) if (outExp.source==inExp.exp1.target)&&(outExp.target==inExp.exp2.source) => return (c, outExp.relation, d)
        case ((a,b,c),(d,e,f)) if (outExp.source==inExp.exp1.target)&&(outExp.target==inExp.exp2.target) => return (c, outExp.relation, f)
        case ((a,b,c),(d,e,f)) if (outExp.source==inExp.exp2.source)&&(outExp.target==inExp.exp1.source) => return (d, outExp.relation, a)
        case ((a,b,c),(d,e,f)) if (outExp.source==inExp.exp2.source)&&(outExp.target==inExp.exp1.target) => return (d, outExp.relation, c)
        case ((a,b,c),(d,e,f)) if (outExp.source==inExp.exp2.source)&&(outExp.target==inExp.exp2.source) => return (d, outExp.relation, d)
        case ((a,b,c),(d,e,f)) if (outExp.source==inExp.exp2.source)&&(outExp.target==inExp.exp2.target) => return (d, outExp.relation, f)
        case ((a,b,c),(d,e,f)) if (outExp.source==inExp.exp2.target)&&(outExp.target==inExp.exp1.source) => return (f, outExp.relation, a)
        case ((a,b,c),(d,e,f)) if (outExp.source==inExp.exp2.target)&&(outExp.target==inExp.exp1.target) => return (f, outExp.relation, c)
        case ((a,b,c),(d,e,f)) if (outExp.source==inExp.exp2.target)&&(outExp.target==inExp.exp2.source) => return (f, outExp.relation, d)
        case ((a,b,c),(d,e,f)) if (outExp.source==inExp.exp2.target)&&(outExp.target==inExp.exp2.target) => return (f, outExp.relation, f)
      }
  }

  def GRAPH_REVERSE(inExp:GRAPH2, outExp:GRAPH2):Boolean={return inExp.source==outExp.target&&inExp.target==outExp.source}

  def GRAPH_NO_REVERSE(inExp:GRAPH2, outExp:GRAPH2):Boolean = {return (!(GRAPH_REVERSE(inExp, outExp)))}

  def GRAPH_RELATION_REPLACE(inExp:GRAPH2, outExp:GRAPH2):Boolean={return (inExp.relation!=outExp.relation)}

  def GRAPH_RELATION_KEEP(inExp:GRAPH2, outExp:GRAPH2):Boolean={return (!(GRAPH_RELATION_REPLACE(inExp, outExp)))}

  


  def graph2String(source:String, relation:String, target:String):String={
    return source + " " + relation + " " + target;
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

  def main(args : Array[String]) : Unit = 
  {
      val inGraphExp=GRAPH2("A", "is colleagues with", "B")
      val outGraphExp=GRAPH2("B", "is colleagues with", "A")

      val testInGraph=("John", "is colleagues with", "Mary")
      val expectedOutGraph=("Mary", "is colleagues with", "John")

      val condExp1=GRAPH2("A", "is a", "B")
      val condExp2=GRAPH2("B", "is a", "C")
      val condExp3=GRAPH2("C", "is a", "D")
      val outExp=GRAPH2("A", "is a", "C")
      val compositeExp=COMPOSITE(condExp1, "AND", condExp2)
      val graphPair=(condExp1, condExp2)
      val testGraph1=("John", "is a", "surgeon")
      val testGraph2=("surgeon", "is a", "human")
      val testGraph3=("John", "is a", "human")

  
      println(RuleEngine.transform(inGraphExp, outGraphExp, testInGraph))
      println(RuleEngine.checkMatch(compositeExp, (testGraph1, testGraph2)))
      println(RuleEngine.transform(graphPair, outExp, (testGraph1, testGraph2)))
  }

  
}
