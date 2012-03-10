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

  def process_page(wptitle:String):Unit={
   
    val wpage = Wikipedia.getPage(wptitle)
    println(wpage.length)
    val infoBoxItems=Wikipedia.getInfobox(wpage, WikiCinemaCrawler.fields)
    val iterKeys=infoBoxItems.keys
    

    for(key <- iterKeys)
    {
      println(key)
      key match{
            case "producer" => processField("producer", infoBoxItems.getOrElse("producer", Array()), Formatting.normalizeWikiTitle(wptitle))
            case "writer" => processField("writer", infoBoxItems.getOrElse("writer", Array()), Formatting.normalizeWikiTitle(wptitle))
            case "director" => processField("director", infoBoxItems.getOrElse("director", Array()), Formatting.normalizeWikiTitle(wptitle))
            case "music" => processField("music", infoBoxItems.getOrElse("music", Array()), Formatting.normalizeWikiTitle(wptitle))
            case "starring" => processField("starring", infoBoxItems.getOrElse("starring", Array()), Formatting.normalizeWikiTitle(wptitle))
            case "image" => processField("image", infoBoxItems.getOrElse("image", Array()), Formatting.normalizeWikiTitle(wptitle))
            case "poster" => processField("poster", infoBoxItems.getOrElse("poster", Array()), Formatting.normalizeWikiTitle(wptitle))
            case _ => 
          }
    }
        
  
  }

  def processField(field:String, values:Array[String], filmname:String):Unit=
  {
    println(field)
    for(value<-values)
    {
      value match
      {
        case a:String => println(a+","+WikiCinemaCrawler.fieldRelationMap(field)+","+ filmname);
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

    //val output = new OutputDBWriter("gb", sourceName)
    
  
    for(l <- WikiCinemaCrawler.list_pages){
        val title = film_list_stub+l;
        val page = Wikipedia.getPage(title)

        val films = Wikipedia.getLinks(page)
        
        for(f <- films){
          while(count<1){
          
          try{
        
              process_page(Formatting.wikiLink(f))
              count+=1;
              inserted+=1      
          }
          catch {
            case _ => println("Exception: " + f); count+=1
          }
        }
      }
    }
    println("Total: "+ count.toString + ", Inserted: " + inserted.toString)
  }

}