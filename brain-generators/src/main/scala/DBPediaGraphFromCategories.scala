package com.graphbrain.brain-generators

import java.net.URL
import scala.io.Source
import scala.util.matching.Regex
import scala.xml._
import java.io.BufferedReader;
import java.io.InputStreamReader
import scala.collection.mutable.ListBuffer


object DBPediaGraphFromCategories {

  
  val thingRegex="""(<http:\/\/dbpedia.org\/resource\/.+?>)""".r
  val predicateRegex = """(<http:\/\/dbpedia.org\/ontology\/.+?>)""".r
  val owlString="""<http:.*owl#Thing>""".r
  val wikiRegex = """(<http:\/\/en.wikipedia.org\/wiki\/.+?>)""".r
  val sourceName = "dbpedia/instancetypes"
  val sourceURL = "http://downloads.dbpedia.org/3.7/en/instance_types_en.nq.bz2"
  val dataFile = "brain-generators/data-files/instance_types_en.nq"

  /*
  Gets a qtuple and returns a 4-tuple with (node, relation, node, source)
  If the qtuple is not in the correct format, the tuple ("", "", "", "", "") is returned.
  */
  def processQTuple(qTuple:String):(String, String, String, String)=
  {
    val thing = thingRegex.findAllIn(qTuple).toArray

    val category = getCategory(qTuple)
    val wikiSource = wikiRegex.findAllIn(qTuple).toArray

    if(thing.length==1&&category.length>=1)
    {
      val subj=Formatting.normalizeWikiTitle(thing(0).replace("<http://dbpedia.org/resource/", "").replace(">", ""))
      if(Formatting.isList(subj)){return ("","","","")}
      if(wikiSource.length==1)
      {
        return (subj, "is a", category, wikiSource(0));
      }
      else{
        return (subj,"is a",category,"")
      }
    }
    return ("", "", "", "")
    
  }

  private def getCategory(qTuple:String):String=
  {
    if(predicateRegex.findAllIn(qTuple).hasNext)
    {
      val category=predicateRegex.findAllIn(qTuple).toArray
      return Formatting.separateWords(category(0).replace("<http://dbpedia.org/ontology/", "").replace(">", ""))
    }
    if(owlString.findAllIn(qTuple).hasNext)
    {
      return "owl#Thing"
    }
    return ""
  }

  def processFile(filename:String, output:OutputDBWriter, limit:Int, readerLine:Int=0):Unit=
  
  {
    
    val reader = new InputFileReader(filename);
    if(readerLine>1)
    {
      reader.initAtLine(readerLine)
    }
    var counter=reader.getLineNum();
    var inserted=0;
    output.writeGeneratorSource(DBPediaGraphFromCategories.sourceName, DBPediaGraphFromCategories.sourceURL, output)
    inserted+=1

    var items = new ListBuffer[(String, String, String, String)]

    while(counter<limit||limit<0)
    {
      val line = reader.readLine()
      println("Processed Line: " + counter.toString + " Inserted: " + inserted.toString + line + " File line: " + reader.getLineNum());
      
      line match{
        case ""=> return 
        case a:String => processQTuple(a) match {
          case ("", "", "", "") => //Don't output  
          case (b:String, c:String, d:String, e:String) => d match {
          	case "owl#Thing" => inserted+=addTypes(items.reverse.toList, output); items.clear();
          	case _ => items.append(processQTuple(a)); 
          }
         
        }
      } 
      counter+=1     
    }
    println("Start line: "+readerLine.toString)
    println("End line: "+ counter.toString); 
    println("Inserted: "+ inserted.toString); 
    return

  }

  def addTypes(items:List[(String, String, String, String)], output:OutputDBWriter):Int=
  {
    var inserted=0;
    items match{
      case x::Nil => x match{
        case (a:String, b:String, c:String, d:String) => output.writeOutDBInfo(Formatting.normalizeWikiTitle(a), "is_a", Formatting.normalizeWikiTitle(c), d); inserted+=1; println(Formatting.normalizeWikiTitle(a) + "," + "is_a" + "," + Formatting.normalizeWikiTitle(c) + "," + d ); return inserted
      }
      case x::y::xs => (x,y) match {
        case ((a:String, b:String, c:String, d:String),(f:String, g:String, h:String, i:String)) => output.writeOutDBInfo(Formatting.normalizeWikiTitle(h), "is_a", Formatting.normalizeWikiTitle(c), ""); inserted+=1; println(Formatting.normalizeWikiTitle(h) + "," + "is_a" + "," + Formatting.normalizeWikiTitle(c) + "," + "" ); addTypes(y::xs, output);  
      }
      case Nil => return inserted
    }

  }
     

  def main(args : Array[String]) : Unit = {
    args match
    {
      
      case a:Array[String] if(a.length==3) => processFile(args(0), new OutputDBWriter(args(1), args(2)), 0-1, args(3).toInt)
      case _ =>  processFile(DBPediaGraphFromCategories.dataFile, new OutputDBWriter("gb", DBPediaGraphFromCategories.sourceName), 100, 95)
      
    }
    
    
  }
}