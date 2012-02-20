import java.net.URL
import scala.io.Source
import scala.util.matching.Regex
import scala.xml._
import java.io.BufferedReader;
import java.io.InputStreamReader
import scala.collection.mutable.ListBuffer


object DBPediaGraphFromInfobox {

  
  val thingRegex="""(<http:\/\/dbpedia.org\/resource\/.+?>)""".r
  val predicateRegex = """(<http:\/\/dbpedia.org\/ontology\/.+?>)""".r
  val wikiRegex = """(<http:\/\/en.wikipedia.org\/wiki\/.+?>)""".r


  /*
  Gets a qtuple and returns a 4-tuple with (node, relation, node, source)
  If the qtuple is not in the correct format, the tuple ("", "", "", "", "") is returned.
  */
  def processQTuple(qTuple:String):(String, String, String, String)=
  {
    var things = thingRegex.findAllIn(qTuple).toArray
    val predicate = predicateRegex.findAllIn(qTuple).toArray
    val wikiSource = wikiRegex.findAllIn(qTuple).toArray
    if(things.length==2&&predicate.length==1&&wikiSource.length==1)
    {
      val subj=WikiListCrawler.normalizeWikiTitle(things(0).replace("<http://dbpedia.org/resource/", "").replace(">", ""))
      val obj = WikiListCrawler.normalizeWikiTitle(things(1).replace("<http://dbpedia.org/resource/", "").replace(">", ""))
      val pred = separateWords(predicate(0).replace("<http://dbpedia.org/ontology/", "").replace(">", ""))
      
      return (subj, pred, obj, wikiSource(0));
    }
    else
    {
      return ("", "", "", "")
    }
  }

  def separateWords(stringToSep:String):String=
  {
     
     val wordRegex="""[a-z]+|[A-Z][a-z]+""".r
     val words=wordRegex.findAllIn(stringToSep)
     
     var returnString=""
     while(words.hasNext)
     {
       returnString+=" " + words.next.toLowerCase.trim;
       
     }
     return returnString;
  }


  def processFile(filename:String, output:Output=new OutputScreenPrinter()):Unit=
  {
    
    val reader = new InputFileReader(filename);
    var counter=1
    while(counter<100000)
    //while(true)
    {
      val line = reader.readLine()
      
      line match{
        case ""=> return 
        case a:String => processQTuple(a) match {
          case ("", "", "", "") => //Don't output  
          case (b:String, c:String, d:String, e:String) => output.writeOut(b+","+c+","+d+","+e)

        }
      } 
      counter+=1     
    }
    return

  }
  def processFileSTDIN(output:Output=new OutputScreenPrinter()):Unit=
  {
    
     for( line <- io.Source.stdin.getLines)
     {
        processQTuple(line) match {
          case ("", "", "", "") => //Don't output  
          case (a:String, b:String, c:String, d:String) => output.writeOut(a+","+b+","+c+","+d);

        }
     } 
     
    }

  def main(args : Array[String]) : Unit = {
    args match
    {
      
      case a:Array[String] if(a.length==2) => processFile(args(0), new OutputScreenPrinter())
      case _ =>  processFile("data-files/mappingbased_properties_en.nq")
      //case _ =>  processFileSTDIN()
    }
    
  }
}
