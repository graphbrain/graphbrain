package com.graphbrain.braingenerators

object InputFileTester{
	def main(args:Array[String]) : Unit = {
      val fileName="/Users/chihchun_chen/Documents/graphbrain/mappingbased_properties_en.nq";
      val reader:InputFileReader=new InputFileReader(fileName);
      reader.initAtLine(1000000)
      println(reader.readLine())
      println(reader.getLineNum())
  	}
}