package com.graphbrain.brain-generators

import java.io.BufferedReader;
import java.io.InputStreamReader
import java.io.FileInputStream



class InputFileReader(fileName:String, sep:String=",") {
	
	val reader:BufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
	var lineNum=0;

	def readLine():String=
	{
		val line = reader.readLine();
		lineNum+=1;
		line match{
			case a:String => return a;
			case _ => return "";
		}
		
	}

	def getLineNum():Int=
	{
		return lineNum;
	}

	def initAtLine(line:Int):Unit=
	{
		for(i<-0 to line-1){
			readLine()

		}
		
	}

	/*def readLine(startLine:Int):String=
	{
		for(i<-0 to startLine-1){
			reader.readLine()
			lineNum+=1
		}
		val line=reader.readLine()
		lineNum+=1;
		line match{
			case a:String => return a;
			case _ => return "";
		}
	}*/

	def readItems():Array[String]=
	{
		val line = reader.readLine();
		line match{
			case a:String => return line.split(sep)			
			case _ => return new Array[String](0)
		}

	}

	
	def close():Unit=
	{	
		reader.close()
		
	}

        
}

