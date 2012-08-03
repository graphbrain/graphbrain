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
	

	def writeOutDBInfo(node1: String, relin: String, node2: String, resource: String):Unit=
	{
		

		try{
			val rel=ID.relation_id(relin);
			val globalRelType = ID.reltype_id(rel, 1)
			val wikiName1 = ID.wikipedia_id(node1)
			val wikiName2 = ID.wikipedia_id(node2)
			
			
			val ng1 = insertAndGetWikiDisambigNode(node1)
			val ng2 = insertAndGetWikiDisambigNode(node2)
			val relType = EdgeType(id = globalRelType, label = rel);
			val w1 = TextNode(id = wikiName1, text = URLDecoder.decode(node1, "UTF-8"))
			val w2 = TextNode(id = wikiName2, text = URLDecoder.decode(node2, "UTF-8"))

			
			println(store.getOrInsert2(relType, store.idFromUsername(username)).id)
			println(store.getOrInsert2(ng1, store.idFromUsername(username)).id)
			println(store.getOrInsert2(ng2, store.idFromUsername(username)).id)
        	

			store.addrel(wikiRel, Array[String](ng1.id, w1.id))
        	store.addrel(wikiRel, Array[String](ng2.id, w2.id))

			//Relationship at global level
			store.addrel2(relType.id, Array[String](ng1.id, ng2.id), store.idFromUsername(username))


			
			
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
      return wikiTitle.split("""\(""")(0)
	}

	def insertAndGetWikiDisambigNode(wikiTitle: String): Vertex = {
		//Check if exists in database
		val titleSP = removeWikiDisambig(wikiTitle);
		
		var i = 1;

		while(store.exists(ID.text_id(titleSP, i.toString))) {
			val existingNode = store.get(ID.text_id(titleSP, i.toString));
			existingNode match {
				case e: TextNode => if(e.text == URLDecoder.decode(wikiTitle, "UTF-8")) {return existingNode}
				case _ =>
			}
			i += 1
		}
		val newNode = TextNode(id = ID.text_id(titleSP, i.toString), text=URLDecoder.decode(wikiTitle, "UTF-8"))
		println(store.put(newNode));
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
	    	store.addrel2("source", Array[String](sourceNode.id, urlNode.id), store.idFromUsername(username))
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
  			store.addrel2("en_wikipage", Array[String](urlNode.id, node.id), store.idFromUsername(username)); 
  			store.addrel2("source", Array[String](sourceNode.id, urlNode.id), store.idFromUsername(username))
  			
  		}
  		catch {
  			case e => e.printStackTrace()

  		}
  		

  	}

  	def writeNounProjectImageNode(imagename:String, url:String, image:String="", contributorName:String="", contributorURL:String="")
  	{
  		try{
  			//Tries to find an existing Wiki node.
  			val WikiID=ID.wikipedia_id(imagename)
			val wikinode = TextNode(id=WikiID, text=imagename);
			

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
					val imageLocal=TextNode(id=ID.nounproject_id(imagename), text=a)
					store.getOrInsert2(imageLocal, store.idFromUsername(username))
					store.addrel("image_page", Array[String](urlNode.id,imageLocal.id))
					store.addrel("source", Array[String](sourceNode.id, imageLocal.id))
					val attribution = imagename + NounProjectCrawler.attributionText + contributorName;
					val contributorNode = TextNode(id=ID.nounproject_id(contributorName), text=contributorName);
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

						val newNode=TextNode(id=ID.text_id(imagename, "noun"), text=imagename)
						store.getOrInsert2(newNode, store.idFromUsername(username));
						store.addrel("image_of", Array[String](imageLocal.id, newNode.id))
				
					}

			}

  		}
  	}

  	def addWikiPageToDB(pageTitle:String):Unit=
  	{
    	val pageURL = Wikipedia.wikipediaBaseURL+pageTitle.replace(" ", "_")
    	val id=ID.wikipedia_id(pageTitle)
    	val pageNode = TextNode(id, pageTitle);
    	writeURLNode(pageNode, pageURL)

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