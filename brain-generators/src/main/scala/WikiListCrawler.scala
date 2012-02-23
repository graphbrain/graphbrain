import java.net.URL
import scala.io.Source
import scala.util.matching.Regex
import scala.xml._
import java.io.BufferedReader;
import java.io.InputStreamReader
import scala.collection.mutable.ListBuffer


object WikiListCrawler {

  val wikipediaBaseURL = "http://en.wikipedia.org/w/index.php?title="
  val enWikiPath="en.wikipedia.org/wiki/"
  val raw="&action=raw"
  val listPage = "List_of_lists_of_lists"
  val featuredArticles = "Wikipedia:Featured_articles"
  val ListRegex = """List(s?)\sof\s""".r
  val WikiLinkRegex="""\[\[([^\]]*)\]\]""".r
  val CategoryRegex="""category\:.*""".r
  val IrregularTitleRegex=""".*\:.*""".r

  def getLinks(content:String):List[String]={
    val foundLinks = WikiLinkRegex.findAllIn(content)
    var links:List[String]=List()
    while(foundLinks.hasNext)
    {
      val link=foundLinks.next().replaceAll("""(\[\[)""", "").replaceAll("""\]\]""", "").replaceAll("""\|.*""", "").replaceAll("""#""", "")
      links=link::links
    }
    return links.reverse

  }

  def getPage(title:String, baseURL:String=wikipediaBaseURL, getRaw:Boolean=true):String={
    var pageURL = baseURL +title.replace(" ", "_")
    if(getRaw)
    {
      pageURL+=raw
    }
    val page=new URL(pageURL);
    val reader=new BufferedReader(new InputStreamReader(page.openStream()));
    var input="";
    while(true)
    {
      val line=reader.readLine()
      line match{
        case a:String => input+=line; 
        case _ => return input;
      }

    }
    return input;

  }

  /**
  * Returns a list of (item, category) tuples - can be interpreted as [item] [is a] [category].
  */
  def getFlatListTree(page_name:String, list:ListBuffer[(String, String)], maxNumEntries:Int=100):ListBuffer[(String, String)]=
  {
    val pageURL = wikipediaBaseURL + page_name.replace(" ", "_") + raw

    //val pageURL = enWikiPath + page_name.replace(" ", "_") + raw
    try
    {
      val page=new URL(pageURL)
      val reader=new BufferedReader(new InputStreamReader(page.openStream()))

      while(list.length<=maxNumEntries)
      {

        val links=getLinks(reader.readLine())
        for(link <- links)
        {
          link match
          {
            case a:String if(Formatting.isList(a)) => list.appendAll(getFlatListTree(a, list, maxNumEntries))
            case a:String if(Formatting.isCategory(a)) => //list.append((getListCategory(page_name), getListCategory(a)))
            case a:String if(Formatting.isIrregular(a)) => 
            case a:String if(Formatting.isList(a)==false) => list.append((Formatting.normalizeWikiTitle(getListCategory(a)), Formatting.normalizeWikiTitle(getListCategory(page_name))))
            case _ =>  
          }    
        }
      }
      return list;

    }
    catch 
    {
      case e => println("Exception with: "+ pageURL); return list;//e.printStackTrace; 
    }
  }

  def getListCategory(titleString:String):String={
    //Get the last string segment (in case "list of") is preceded by other junk.
    var cat=titleString.toLowerCase.split("list(s?)\\sof\\s").last
    cat = cat.toLowerCase.split("category:").last

    //TODO: Add de-pluralisation rule (so from "List of kings", we get "king" and from "List of cities", we get "city" - dropping s not good enough)
    //if(cat.last=='s')
    //{
      //return cat.dropRight(1)  
    //}
    //else {return cat}
    return cat
  }

  




  // main(args)
  //Enter URL, otherwise default is used:
  //"http://en.wikipedia.org/wiki/List_of_lists_of_lists"
  //To demo a shorted list: set the pagename to "Lists of cities" 
  //
  def main(args : Array[String]) : Unit = {
    args match
    {
      case a:Array[String] if(a.length==2) => print(getFlatListTree(args(0), new ListBuffer(), maxNumEntries=args(1).toInt))
      case _ => print(getFlatListTree(listPage, new ListBuffer(), maxNumEntries=100)) 
    }
  }
}
