import java.net.URL
import scala.io.Source
import scala.util.matching.Regex
import scala.xml._
import java.io.BufferedReader;
import java.io.InputStreamReader
import scala.collection.mutable.ListBuffer


object NounImageQuery {

  val nounProjectURL = "http://thenounproject.com/noun/"
  val svgRegex="""[^\s^\']+(\.(?i)(svg))""".r

  def getImageResource(noun:String):String={
    val imageURL = NounImageQuery.nounProjectURL + noun + "/";
    val result=NounImageQuery.readPage(imageURL)
    val svgImage=svgRegex.findFirstIn(result);
    return(svgImage.getOrElse("Not found"))
    
  }

  def getSVGImage(imageURL:String):String={
    return NounImageQuery.readPage(imageURL);
  }


  def getPageReader(pageURL:String):BufferedReader=
  {
    val page=new URL(pageURL);
    return new BufferedReader(new InputStreamReader(page.openStream()));
    
  }

  def readPage(pageURL:String):String=
  {
    val reader=getPageReader(pageURL)  
    var input="";
      while(true)
      {
          val line=reader.readLine()
          line match{
            case a:String => input+=line+"\n"; 
            case _ => return input;
          }

      }
      return input;
  }

  def main(args : Array[String]) : Unit = {
    args(0) match
    {
      case a:String => 
      val imageResource=NounImageQuery.getImageResource(a); 
      println(imageResource); 
      println(NounImageQuery.getSVGImage(imageResource))

    }
    
  }
}
