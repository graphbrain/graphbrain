package com.graphbrain.braingenerators

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.net.URLDecoder;
import scala.collection.immutable.HashMap
import com.graphbrain.hgdb.VertexStore
import com.graphbrain.hgdb.BurstCaching
import com.graphbrain.hgdb.TimeStamping
import com.graphbrain.hgdb.UserManagement
import com.graphbrain.hgdb.UserOps
import com.graphbrain.hgdb.OpLogging
import com.graphbrain.hgdb.TextNode
import com.graphbrain.hgdb.Edge
import com.graphbrain.hgdb.SourceNode
import com.graphbrain.hgdb.URLNode
import com.graphbrain.hgdb.Vertex
import com.graphbrain.hgdb.EdgeType


class OutputDBWriter(storeName:String, source:String, username:String, name:String, role:String) {
	

	val store = new VertexStore(storeName) with BurstCaching with TimeStamping with UserManagement with UserOps
	val wikiURL = "http://en.wikipedia.org/wiki/"
	val wikiPageET = EdgeType(ID.reltype_id("wikipage"), label = "wikipage")
	val lineRegex = """#.*?""".r
	val wikiRel = "sys/wikipedia"
	val asInRel = ID.reltype_id("as in", 1)
	

	def writeOutDBInfo(node1: String, relin: String, node2: String, resource: String):Unit=
	{
		

		try{

			val globalRelType = ID.reltype_id(separateWords(relin.trim), 1)
			
			
			val ng1 = insertAndGetWikiDisambigNode(node1, username)
			val ng2 = insertAndGetWikiDisambigNode(node2, username)
			val relType = EdgeType(id = globalRelType, label = separateWords(relin.trim));
			
			
			println(store.getOrInsert2(relType, store.idFromUsername(username)).id + ", " + relType.label);
			println(store.getOrInsert2(ng1, store.idFromUsername(username)).id);
			println(store.getOrInsert2(ng2, store.idFromUsername(username)).id);
        	

			
			//Relationship at global level
			store.addrel2(relType.id, Array[String](ng1.id, ng2.id), store.idFromUsername(username), true)


			
			
		}
		catch {
			case e => e.printStackTrace()
		}
		
		

	}

	

	def nodeExists(node:Vertex):Boolean=
	{
		try{
			store.get(node.id);
			return true			
		}
		catch{
			case e => return false

		}	
	}

	def removeWikiDisambig(wikiTitle: String): String = {
      return wikiTitle.split("""\(""")(0).reverse.dropWhile(_ == '_').reverse.trim
	}

	def insertAndGetWikiDisambigNode(wikiTitle: String, username: String): Vertex = {
		val decodedTitle = URLDecoder.decode(wikiTitle, "UTF-8")
		val titleSP = removeWikiDisambig(decodedTitle);
		val disAmb = """\((.*?)\)""".r.findAllIn(decodedTitle)
		var i = 1;

		while(store.exists(ID.text_id(titleSP, i.toString))) {
			val existingNode = store.get(ID.text_id(titleSP, i.toString));
			existingNode match {
				case e: TextNode => 

				  if(disAmb.hasNext) {
				  	val da = disAmb.next.replace("(", "").replace(")", "").trim
				  	val daID = ID.text_id(da, "1")
				  	val daRelID = asInRel + " " + e.id + " " + daID;
				  	if(e.text == titleSP && store.exists(daRelID)) {
				  		return existingNode
				  	}
				  }
				  else {
				  	if(e.text == titleSP) {
				  		return existingNode;
				  	}

				  }
				case _ =>
			}
			i += 1
		}
		val newNode = TextNode(namespace = i.toString, text=titleSP)
		println(store.getOrInsert2(newNode, store.idFromUsername(username)).id)
		val wikiNode = TextNode(namespace = "wikipedia", text = URLDecoder.decode(wikiTitle, "UTF-8"))
		store.put(wikiNode)
		println(store.get(wikiNode.id).id)
		store.addrel(wikiRel, Array[String](newNode.id, wikiNode.id))
		
		if(disAmb.hasNext) {
			val da = disAmb.next.replace("(", "").replace(")", "").trim;
			val daNode = TextNode(namespace = "1", text=da)
			println(store.getOrInsert2(daNode, store.idFromUsername(username)).id + ", da: " +  daNode.text)
			store.addrel2(asInRel, Array[String](newNode.id, daNode.id), store.idFromUsername(username), true)

		}

		return newNode;
		
	}


	def writeGeneratorSource(sourceID:String, sourceURL:String)
  	{
  		try{
  			val sourceNode=SourceNode(id=ID.source_id(sourceID))
	    	val urlNode=URLNode(ID.url_id(sourceURL), sourceURL)
	    	println(username)
	    	store.getOrInsert2(sourceNode, store.idFromUsername(username))
	    	store.getOrInsert2(urlNode, store.idFromUsername(username))
	    	store.addrel2("source", Array[String](sourceNode.id, urlNode.id), store.idFromUsername(username), true)
	    	store.getOrInsert2(wikiPageET, store.idFromUsername(username))
	    }
	    catch{
	    	case e => e.printStackTrace()
	    }
  	}

  	def writeUser() 
  	{
  		try {
  			store.createUser(username = username, name = name, email = "", password = "", role = role)
  		}
  		catch {
  			case e => e.printStackTrace()
  		}
  	}

  	def writeURLNode(node:Vertex, url:String)
  	{
  		try{
  			val sourceNode=store.getSourceNode(ID.source_id(source))
  			val urlNode = URLNode(ID.url_id(url), url)	
  			store.getOrInsert2(node, store.idFromUsername(username))
  			store.getOrInsert2(urlNode, store.idFromUsername(username));
  			store.getOrInsert2(sourceNode, store.idFromUsername(username))
  			store.addrel2("en_wikipage", Array[String](urlNode.id, node.id), store.idFromUsername(username), true); 
  			store.addrel2("source", Array[String](sourceNode.id, urlNode.id), store.idFromUsername(username), true)
  			
  		}
  		catch {
  			case e => e.printStackTrace()

  		}
  		

  	}

  	def writeNounProjectImageNode(imagename:String, url:String, image:String="", contributorName:String="", contributorURL:String="")
  	{
  		try{
  			//Tries to find an existing Wiki node.
			  val wikinode = TextNode(namespace="wikipedia", text=imagename);
			

  			val sourceNode=store.getSourceNode(ID.source_id(source));
  			val urlNode=URLNode(id=ID.url_id(url), url=url)
			
			store.getOrInsert2(sourceNode, store.idFromUsername(username));
			store.getOrInsert2(urlNode, store.idFromUsername(username));
			store.addrel("source", Array[String](sourceNode.id, urlNode.id))

				
			if(image=="")
			{
				return;
			}
			image match{
				case a:String => 
					val imageLocal=TextNode(namespace=ID.nounproject_ns(), text=a)
					store.getOrInsert2(imageLocal, store.idFromUsername(username))
					store.addrel("image_page", Array[String](urlNode.id,imageLocal.id))
					store.addrel("source", Array[String](sourceNode.id, imageLocal.id))
					val attribution = imagename + NounProjectCrawler.attributionText + contributorName;
					val contributorNode = TextNode(namespace=ID.nounproject_ns(), text=contributorName);
					val contURLNode = URLNode(id=ID.url_id(contributorURL), url=contributorURL);

					store.getOrInsert2(contributorNode, store.idFromUsername(username))
					store.getOrInsert2(contURLNode, store.idFromUsername(username))
					store.addrel("attribute_as", Array[String](imageLocal.id, contributorNode.id))
					store.addrel("contributor_page", Array[String](contURLNode.id, contributorNode.id, imageLocal.id))


					store.getOrInsert2(imageLocal, store.idFromUsername(username));

					if(nodeExists(wikinode)) 
					{
						store.addrel("image_of", Array[String](imageLocal.id, wikinode.id))

					
					}
					else{

						val newNode=TextNode(namespace="noun", text=imagename)
						store.getOrInsert2(newNode, store.idFromUsername(username));
						store.addrel("image_of", Array[String](imageLocal.id, newNode.id))
				
					}

			}

  		}
  	}

  	def addWikiPageToDB(pageTitle:String):Unit=
  	{
    	val pageURL = Wikipedia.wikipediaBaseURL+pageTitle.trim.replace(" ", "_")
    	val pageNode = TextNode("wikipedia", pageTitle);
    	writeURLNode(pageNode, pageURL)
  	}

  	def separateWords(stringToSep: String) : String = {
  		val capRegex = """[A-Z][a-z]*""".r;
  		val nonCapRegex = """[a-z]+""".r;
  		var separated = "";
  		var nonCapsSep = nonCapRegex.findAllIn(stringToSep);
  		var capsSeparated = capRegex.findAllIn(stringToSep);
  		
  		if(nonCapsSep.length > 0) {
  			nonCapsSep = nonCapRegex.findAllIn(stringToSep);
  			separated += " " + nonCapsSep.next.toLowerCase;
  		}


  		if(capsSeparated.length > 0) {
  		  capsSeparated = capRegex.findAllIn(stringToSep);
  		  while(capsSeparated.hasNext) {
             
            separated += " " + capsSeparated.next.toLowerCase;

  		  }
  		} 
  		return separated.trim;
  	}

	def getRelID(rel:String, node1ID:String, node2ID:String):String=
	{
		val pageTokens=List[String](rel)++Array[String](node1ID, node2ID)
		return pageTokens.reduceLeft(_+ " " +_)
	}

	def finish() = {
		store.finish();
	}
}

object OutputDBWriter {
	def main(args : Array[String]) : Unit = {
		val test = new OutputDBWriter(storeName = "gb", source = DBPediaGraphFromCategories.sourceName, username = "dbpedia", name = "dbpedia", role = "crawler")
		val sister1 = "Sister_(film)"
		val sister2 = "Sister_(band)"
		test.writeOutDBInfo(sister1, "is a", "film", "http://en.wikipedia.org/wiki/Sister_(film)")
		test.writeOutDBInfo(sister2, "likes", "music", "http://en.wikipedia.org/wiki/Sister_(band)")
    }
  
}
