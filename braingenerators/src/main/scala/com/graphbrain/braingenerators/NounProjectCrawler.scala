package com.graphbrain.braingenerators;

import java.io._
import scala.collection.mutable
import java.io.BufferedReader;
import java.io.FileInputStream
import java.net.URL
import scala.io.Source
import scala.util.matching.Regex
import scala.xml._
import java.io.BufferedReader;
import java.io.InputStreamReader
import scala.collection.mutable.ListBuffer


object NounProjectCrawler {

  val nounProjectBaseURL = "http://thenounproject.com/"
  val nounProjectURL = "http://thenounproject.com/noun/"
  val svgRegex="""[^\s^\']+(\.(?i)(svg))""".r
  val nounRegex="""(<a\sclass=\"group\"\shref=\"\/noun\/.+?\/)""".r
  val sourceName = "thenounproject"
  val sourceURL = "http://thenounproject.com/"
  val attributionText=" symbol by "


  //There seem to 8 noun list pages:
  val nounListPages=8;

  def getNounList(pageNum:Int):List[String]={
    val url=NounProjectCrawler.nounProjectBaseURL + pageNum.toString;
    val page=readPage(url);
    val nounList=NounProjectCrawler.nounRegex.findAllIn(page).toList
    return nounList.map(extractNoun)
  }

  def extractNoun(fullURL:String):String={
    return fullURL.replace("<a class=\"group\" href=\"/noun/", "").replace("/", "");
  }


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

  def getAllImages(output:OutputDBWriter):Unit={
    var inserted=0;
    output.writeUser()
    inserted += 1;
    //output.writeGeneratorSource(NounProjectCrawler.sourceName, NounProjectCrawler.sourceURL);
    inserted += 1;

    for(i <- 1 to NounProjectCrawler.nounListPages)
    {
      val nouns=NounProjectCrawler.getNounList(i)
      for(noun <- nouns)
      {
        val image=NounImageQuery.getImage(noun); 
        val contributorData=NounImageQuery.getContributor(noun)
        val imageURL = NounImageQuery.getImageURL(noun)


        output.writeNounProjectImageNode(noun, imageURL, image, contributorData._1, contributorData._2)
        println("Inserted "+inserted.toString+": " +noun + ", " + imageURL + ", " + image + "," + contributorData._1 + "," + contributorData._2);
        inserted+=1;
      }
    }
  }

  def main(args : Array[String]) : Unit = {

    args match
    {
      
      case a:Array[String] if(a.length==2) => getAllImages(new OutputDBWriter(args(1), args(2), "nounproject", "nounproject", "crawler"))
      case _ =>  getAllImages(new OutputDBWriter("gb", NounProjectCrawler.sourceName, "nounproject", "nounproject", "crawler"))
  
    }

    
  }
}
