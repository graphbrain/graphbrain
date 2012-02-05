import scala.util.Random;
import scala.io._

object RuleTester 
{

    def generateSentencesFromRuleFiles(fileName: String, sentence: String):List[String]=
    {
      var new_sentences:List[String]=Nil
    	    	
    	val file=Source.fromFile(fileName).getLines()
    
    	for(line <- file)
    	{
    		
        val rule=line.split(", ")
        
        if(rule.length==3)
        {
          val cond=rule(0)
          val oldStr=rule(1)
          val newStr=rule(2)
          
          if(RuleMatcher.checkMatch(cond, sentence))
          {
            new_sentences=RuleMatcher.replace(sentence, oldStr, newStr)::new_sentences
          }
          else
          {
            //No match
          }
        }
        
    	}

    	return new_sentences	
    }
	 
    def main(args: Array[String])
    {
    	print(generateSentencesFromRuleFiles(args(0), args(1)))

    }
}