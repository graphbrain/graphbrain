package com.graphbrain.brain-generators

import java.net.URL
import scala.io.Source
import scala.util.matching.Regex
import scala.xml._
import java.io.BufferedReader;
import java.io.InputStreamReader
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.HashMap

object Wikipedia {
	val wikipediaBaseURL = "http://en.wikipedia.org/w/index.php?title="
  	val enWikiPath="en.wikipedia.org/wiki/"
  	val raw="&action=raw"
  	val ListRegex = """List(s?)\sof\s""".r
	 val WikiLinkRegex="""\[\[([^\]]*)\]\]""".r
  	val CategoryRegex="""category\:.*""".r
  	val IrregularTitleRegex=""".*\:.*""".r
    val pipe="""\|""".r
    

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
}