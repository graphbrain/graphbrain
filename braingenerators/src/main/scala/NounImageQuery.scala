package com.graphbrain.braingenerators

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
  val contributorRegex="""(<span class="attribution">\s*.*\s*</span>)""".r
  val contRegex="""(<a href=)(.+?)(>)(.+?)(</a>)""".r
  val contURLRegex="""(<a href=.+?>)""".r
  val contTagRegex="""(<a href=.+?>)(.+?)(</a>)"""
  

  def getContributor(noun:String):(String, String)={
    try{
          val imageURL=NounImageQuery.nounProjectURL + noun + "/";
          val result=NounImageQuery.readPage(imageURL)
          val contributorData=contRegex.findAllIn(contributorRegex.findAllIn(result).next).next;
          val contributorURL=contURLRegex.findAllIn(contributorData).next.replace("<a href=", "").replace("\"", "").replace(">", "");
          val contributorName=contributorData.replaceAll("<a href=.+?>", "").replace("</a>", "")
          return (contributorName, contributorURL)
      }
      catch
      {
        case e => e.printStackTrace(); return ("", "")
      }

    
    
  }

  def getImageURL(noun:String):String={
    val imageURL = NounImageQuery.nounProjectURL + noun + "/";
    val result=NounImageQuery.readPage(imageURL)
    val svgImage=svgRegex.findFirstIn(result);
    val found = svgImage.getOrElse("Not found")
    if(found=="Not found")
    {
      return "Not found"
    }
    else{
      return found
    }

  }

  def getImage(noun:String):String={
    val imageURL = NounImageQuery.getImageURL(noun)
    if(imageURL=="Not found")
    {
      return "Not found"
    }
    else
    {
      return(getSVGImagePage(imageURL))  
    }
    
    
  }

  def getSVGImagePage(imageURL:String):String={
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
    //val imageResource=NounImageQuery.getImageResource("tree")
      //println(NounImageQuery.imageResource); 
      //println(NounImageQuery.getSVGImagePage(imageResource));
      //println(NounImageQuery.getContributor("tree"));
    args(0) match
    {
      case a:String => val imageResource=NounImageQuery.getImageURL(a); 
      println(imageResource); 
      println(NounImageQuery.getSVGImagePage(imageResource));
      println(NounImageQuery.getContributor(a));
      case _ => 

    }
    
  }
}
