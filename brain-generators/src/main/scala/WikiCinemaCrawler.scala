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
  
  
  def process_page(wptitle:String):Unit={
    val wpage = Wikipedia.getPage(wptitle)
    val sections = Wikipedia.page2sections(wpage)

    
    val lines = sections("").split("\n");
    for(ln <- lines)
    {
      val l = ln.trim;
      if(l.length>1&&l(0)=='|')
      {
        val prop = l.replace("|", "").split("=");
        if(prop.length>1)
        {
          val key = prop(0).trim
          val value = prop(1).trim

          key match{
            case "producer" => println("producer: "+ value)
            case "writer" => println("writer: "+ value)
            case "director" => println("director: "+ value)
            case "music" => println("musician: " + value)
            case "starring" => println("starring: " + value)
            case "image" => println("image: " + value)
            case "poster" => println("poster: " + value)
          }
        }
      }
    }

    val castlist = sections.getOrElse("cast", None);

    if(castlist!=None)
    {
      val lines = sections("cast").split("\n")
      for(ln <- lines)
      {
        val l=ln.trim
        if(l.length>1&&l(0)=='*')
        {
          val role = l.replace("*", "").split(" as ");
          val text_link = Wikipedia.text_and_or_link(role(0))
          println("Role: "+role+", Text: " + text_link._1 + "Link: "+ text_link._2)


        }

      }
    }
  }

  

  def main(args : Array[String]) : Unit = {
    var count = 0
    var inserted = 0
    

    for(l <- WikiCinemaCrawler.list_pages){
        val title = film_list_stub+l;
        val page = Wikipedia.getPage(title)

        val films = Wikipedia.getLinks(page)
        for(f <- films){
          try{
              val film_page = Wikipedia.getPage(Formatting.wikiLink(f))
              
              process_page(film_page)
              count+=1;
              inserted+=1      
          }
          catch {
            case _ => println(f); count+=1
          }
        }
      }
      println("Total: "+ count.toString + ", Inserted: " + inserted.toString)
  }

}