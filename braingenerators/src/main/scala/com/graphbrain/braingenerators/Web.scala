package com.graphbrain.braingenerators

import scala.util.parsing.json._
import java.net.URL
import scala.io.Source
import scala.util.matching.Regex
import scala.xml._
import java.io.BufferedReader;
import java.io.InputStreamReader
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.HashMap

object Web {

  def getPage(pageURL:String):String={
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
}
