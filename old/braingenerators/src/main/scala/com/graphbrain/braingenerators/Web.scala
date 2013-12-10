package com.graphbrain.braingenerators

import java.net.URL
import java.io.BufferedReader
import java.io.InputStreamReader

object Web {

  def getPage(pageURL:String): String={
  	val page=new URL(pageURL)
    	val reader=new BufferedReader(new InputStreamReader(page.openStream()))
    	var input=""
    	while(true)
    	{
      		val line=reader.readLine()
      		line match{
        		case a:String => input+=line+"\n"
        		case _ => return input
      		}

    	}
    	input
  }	
}
