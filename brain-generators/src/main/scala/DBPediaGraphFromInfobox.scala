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
  val sourceName = "dbpedia/mappingproperties"
  val dataFile = "brain-generators/data-files/mappingbased_properties_en.nq"
  val sourceURL = "http://downloads.dbpedia.org/3.7/en/mappingbased_properties_en.nq.bz2"

  /*
  Gets a qtuple and returns a 4-tuple with (node, relation, node, source)
  If the qtuple is not in the correct format, the tuple ("", "", "", "", "") is returned.
  */
  def processQTuple(qTuple:String):(String, String, String, String)=
  {
    var things = thingRegex.findAllIn(qTuple).toArray
    val predicate = predicateRegex.findAllIn(qTuple).toArray
    val wikiSource = wikiRegex.findAllIn(qTuple).toArray
    if(things.length==2&&predicate.length==1)
    {
      val subj=Formatting.normalizeWikiTitle(things(0).replace("<http://dbpedia.org/resource/", "").replace(">", ""))
      val obj = Formatting.normalizeWikiTitle(things(1).replace("<http://dbpedia.org/resource/", "").replace(">", ""))
      if(Formatting.isList(subj)||Formatting.isList(obj)){return ("","","","")}
      val pred = predicate(0).replace("<http://dbpedia.org/ontology/", "").replace(">", "")
      if(wikiSource.length==1)
      {
        return (subj, pred, obj, wikiSource(0));
      }
      else{
        return (subj,pred,obj,"")
      }
    }
    else
    {
      return ("", "", "", "")
    }
  }


  def processFile(filename:String, output:OutputDBWriter, limit:Int, readerLine:Int=1):Unit=
  {
    
     
    val reader = new InputFileReader(filename);
    if(readerLine>1)
    {
      reader.initAtLine(readerLine)
    }
    var counter=0
    var inserted=0;
    output.writeGeneratorSource(DBPediaGraphFromInfobox.sourceName, DBPediaGraphFromInfobox.sourceURL, output)
    inserted+=1;

    while(counter<limit||limit<0)
    {
      val line = reader.readLine()
      println("Line: " + counter.toString + " Inserted: " + inserted.toString +  line + " File line: " + reader.getLineNum());
      
      line match{
        case ""=> return;
        case a:String => processQTuple(a) match {
          case ("", "", "", "") => //Don't output  
          case (b:String, c:String, d:String, e:String) => output.writeOutDBInfo(b, c, d, e); inserted+=1
        }
      } 
      counter+=1     
      
    }
    println("Total lines: "+ counter.toString); 
    println("Inserted: "+ inserted.toString); 
    return
  }


     

  def main(args : Array[String]) : Unit = {
    args match
    {
      
      case a:Array[String] if(a.length==3) => processFile(args(0), new OutputDBWriter(args(1), args(2)), 0-1)
      case _ =>  processFile(DBPediaGraphFromInfobox.dataFile, new OutputDBWriter("gb", DBPediaGraphFromInfobox.sourceName), 100)
  
    }
    
  }
}
