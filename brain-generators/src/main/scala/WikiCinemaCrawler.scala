import java.net.URL
import scala.io.Source
import scala.util.matching.Regex
import scala.xml._
import java.io.BufferedReader;
import java.io.InputStreamReader
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.HashMap


object WikiCinemaCrawler {

  val sourceURL = "http://en.wikipedia.org/wiki/List_of_films#Alphabetical_indexes"
  val sourceName = "WikiCinemaList"
  
  val film_list_stub = "List_of_films:_"
  val list_pages = List[String]("numbers", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J-K", "L", "M", "N-O", "P", "Q-R", "S", "T", "U-W", "X-Z")
  val fields = List[String]("producer", "writer", "director", "music", "starring", "image", "poster")
  val fieldRelationMap=Map("producer"->"produced", "director"->"directed", "writer"->"wrote", "music"->"music", "starring"->"starred in", "image"->"image", "poster"->"poster")
  val personFields=List[String]("image", "birth_place")

  def process_page(wptitle:String, output:OutputDBWriter):Unit={
   
    val wpage = Wikipedia.getPage(wptitle)
    println(wpage.length)
    val infoBoxItems=Wikipedia.getInfobox(wpage, WikiCinemaCrawler.fields)
    val iterKeys=infoBoxItems.keys
    

    for(key <- iterKeys)
    {
      println(key)
      key match{
            case "producer" => processField("producer", infoBoxItems.getOrElse("producer", Array[String]()), Formatting.normalizeWikiTitle(wptitle), output)
            case "writer" => processField("writer", infoBoxItems.getOrElse("writer", Array[String]()), Formatting.normalizeWikiTitle(wptitle), output)
            case "director" => processField("director", infoBoxItems.getOrElse("director", Array[String]()), Formatting.normalizeWikiTitle(wptitle), output)
            case "music" => processField("music", infoBoxItems.getOrElse("music", Array[String]()), Formatting.normalizeWikiTitle(wptitle), output)
            case "starring" => processField("starring", infoBoxItems.getOrElse("starring", Array[String]()), Formatting.normalizeWikiTitle(wptitle), output)
            case "image" => processField("image", infoBoxItems.getOrElse("image", Array[String]()), Formatting.normalizeWikiTitle(wptitle), output)
            case "poster" => processField("poster", infoBoxItems.getOrElse("poster", Array[String]()), Formatting.normalizeWikiTitle(wptitle), output)
            case _ => 
          }
    }
        
  
  }

  def processField(field:String, values:Array[String], filmname:String, output:OutputDBWriter):Unit=
  {
    println(field)
    for(value<-values)
    {
      value match
      {
        case a:String => output.writeOutDBInfo(a, WikiCinemaCrawler.fieldRelationMap(field), filmname, "")
        println(a+","+WikiCinemaCrawler.fieldRelationMap(field)+","+ filmname);
        case _ =>
      }
    }
  }

  def processPerson():Unit=
  {
    
  }

  

  def main(args : Array[String]) : Unit = {
    var count = 0
    var inserted = 0

    val output = new OutputDBWriter("gb", sourceName)
    
  
    for(l <- WikiCinemaCrawler.list_pages){
        val title = film_list_stub+l;
        val page = Wikipedia.getPage(title)

        val films = Wikipedia.getLinks(page)
        
        for(f <- films){
          
          
          try{
        
              process_page(Formatting.wikiLink(f), output)
              count+=1;
              inserted+=1      
          }
          catch {
            case _ => println("Exception: " + f); count+=1
          }
        }
      
    }
    println("Total: "+ count.toString + ", Inserted: " + inserted.toString)
  }

}