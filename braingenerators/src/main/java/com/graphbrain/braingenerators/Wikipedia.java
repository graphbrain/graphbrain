package com.graphbrain.braingenerators;


public class Wikipedia {
    public static String wikipediaBaseURL = "http://en.wikipedia.org/w/index.php?title=";
    public static String enWikiPath = "en.wikipedia.org/wiki/";
    public static String raw = "&action=raw";
    public static String ListRegex = "List(s?)\\sof\\s";
    public static String WikiLinkRegex = "\\[\\[([^\\]]*)\\]\\]";
    public static String CategoryRegex = "category\\:.*";
    public static String IrregularTitleRegex = ".*\\:.*";
    public static String pipe = "\\|";
    

    /*
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
        		case a:String => input+=line+"\n"; 
        		case _ => return input;
      		}

    	}
    	return input;
    }

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

  def text_and_or_link(s:String):(String, String)={
  	val regex ="""\[\[([^\]]*)\]\]""".r
  	val matches = regex.findAllIn(s)
  	var text = ""
  	var link = ""
  	if(matches.hasNext)
  	{
  		val parts = matches.next.split("|");
  		val link = parts(0).trim;
  		if(parts.length>1)
  		{
  			text = parts(0).trim
  		}
  		else{
  			text = link
  		}
  	}
  	else
  		text = s.trim
  	return(text, link)
  }

  def br_list(s:String):Array[String]=
  {

    return s.split("<br>")
  }
    

  def getInfobox(content:String, fields:List[String]):HashMap[String, Array[String]]=
  {
    var infoItems:HashMap[String, Array[String]] = new HashMap
    val lines = content.split('\n')
    for(line <- lines)
    {

      if(line.contains('='))
      {
        
        val splitItems = line.split('=')
        val fieldname=pipe.replaceAllIn(splitItems(0), "").trim
        if(fields.contains(fieldname))
        {
          val values = splitItems(1)
          infoItems.update(fieldname, values.split("<br>"))  
        }
        

      } 
      
    }
    return infoItems
    
  }

  def page2sections(content:String):HashMap[String, String]=
  {
    var sections:HashMap[String, String] = new HashMap()
    var cur_section = ""
    var cur_text = ""

    val lines = content.split("\n")
    for(l <- lines)
    {
    	if(l.length>4&&l.substring(0,2)=="==")
    	{
    		if(cur_text.length>0)
    		{
    			sections.update(cur_section, cur_text)
    		}
    		val section = l.replaceAll("=", "").toLowerCase
    		cur_section = section;
    		cur_text="";

    	}
    	else{
    		cur_text += l + "\n";
    	}
    	if(cur_text.length>0)
    	{
    		sections.update(cur_section, cur_text)
    	}
    }
    return sections
 }

 def page2sectionsLists(content:String):HashMap[String, List[String]]=
  {
    var sections:HashMap[String, List[String]] = new HashMap()
    var cur_section = ""
    var cur_text = ""

    val lines = content.split("\n")
    for(l <- lines)
    {
    	if(l.length>4&&l.substring(0,2)=="==")
    	{
    		if(cur_text.length>0)
    		{
    			sections.update(cur_section, cur_text::sections.getOrElse(cur_section, Nil))
    		}
    		val section = l.replaceAll("=", "").toLowerCase
    		cur_section = section;
    		cur_text="";

    	}
    	else{
    		cur_text += l + "\n";
    	}
    	if(cur_text.length>0)
    	{
    		sections.update(cur_section, cur_text::sections.getOrElse(cur_section, Nil))
    	}
    }
    return sections
 }
 */
}