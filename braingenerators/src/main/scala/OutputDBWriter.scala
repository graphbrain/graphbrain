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
	

	def writeOutDBInfo(node1: String, relin: String, node2: String, resource: String):Unit=
	{
		

		try{
			val rel=ID.relation_id(relin);
			
			val global1 = ID.text_id(node1, "1")
			val global2 = ID.text_id(node2, "1")
			val globalRelType = ID.reltype_id(rel, 1)
			
			
			//val N1Wiki=ID.wikipedia_id(node1)
			//val N2Wiki=ID.wikipedia_id(node2)
			
			//val nw1 = TextNode(id=N1Wiki, text=URLDecoder.decode(node1, "UTF-8"));
			//val nw2 = TextNode(id=N2Wiki, text=URLDecoder.decode(node2, "UTF-8"));

			val ng1 = TextNode(id = global1, text=URLDecoder.decode(node1, "UTF-8"))
			val ng2 = TextNode(id = global2, text=URLDecoder.decode(node2, "UTF-8"))
			val relType = EdgeType(id = globalRelType, label = rel);
			

			
			println(store.getOrInsert2(relType, username).id)
			println(store.getOrInsert2(ng1, username).id)
			println(store.getOrInsert2(ng2, username).id)


			//Relationship at global level
			store.addrel2(relType.id, Array[String](ng1.id, ng2.id), username)

			//Wikipedia page
			val wikiPageURL = lineRegex.split(resource)(0)
			
			if(wikiPageURL.startsWith(wikiURL)) {
				val wikipediaPage = URLNode(ID.url_id(wikiPageURL), url = wikiPageURL, title = node1)
			
				println(store.getOrInsert2(wikipediaPage, username).id)
				store.addrel2(wikiPageET.id, Array[String](wikipediaPage.id, ng1.id), username)
			}

			
			
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

	def writeGeneratorSource(sourceID:String, sourceURL:String)
  	{
  		try{
  			val sourceNode=SourceNode(id=ID.source_id(sourceID))
	    	val urlNode=URLNode(ID.url_id(sourceURL), sourceURL)
	    	println(username)
	    	store.getOrInsert2(sourceNode, username)
	    	store.getOrInsert2(urlNode, username)
	    	store.addrel2("source", Array[String](sourceNode.id, urlNode.id), username)
	    	store.getOrInsert2(wikiPageET, username)
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
  			store.getOrInsert2(node, username)
  			store.getOrInsert2(urlNode, username);
  			store.getOrInsert2(sourceNode, username)
  			store.addrel2("en_wikipage", Array[String](urlNode.id, node.id), username); 
  			store.addrel2("source", Array[String](sourceNode.id, urlNode.id), username)
  			
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
			
			store.getOrInsert2(sourceNode, username);
			store.getOrInsert2(urlNode, username);
			store.addrel("source", Array[String](sourceNode.id, urlNode.id))

				
			if(image=="")
			{
				return;
			}
			image match{
				case a:String => 
					val imageLocal=TextNode(id=ID.nounproject_id(imagename), text=a)
					store.getOrInsert2(imageLocal, username)
					store.addrel("image_page", Array[String](urlNode.id,imageLocal.id))
					store.addrel("source", Array[String](sourceNode.id, imageLocal.id))
					val attribution = imagename + NounProjectCrawler.attributionText + contributorName;
					val contributorNode = TextNode(id=ID.nounproject_id(contributorName), text=contributorName);
					val contURLNode = URLNode(id=ID.url_id(contributorURL), url=contributorURL);

					store.getOrInsert2(contributorNode, username)
					store.getOrInsert2(contURLNode, username)
					store.addrel("attribute_as", Array[String](imageLocal.id, contributorNode.id))
					store.addrel("contributor_page", Array[String](contURLNode.id, contributorNode.id, imageLocal.id))


					store.getOrInsert2(imageLocal, username);

					if(nodeExists(wikinode)) 
					{
						store.addrel("image_of", Array[String](imageLocal.id, wikinode.id))

					
					}
					else{

						val newNode=TextNode(id=ID.text_id(imagename, "noun"), text=imagename)
						store.getOrInsert2(newNode, username);
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