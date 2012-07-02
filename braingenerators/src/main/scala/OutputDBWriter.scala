package com.graphbrain.braingenerators

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.net.URLDecoder;
import scala.collection.immutable.HashMap
import com.graphbrain.hgdb.VertexStore
import com.graphbrain.hgdb.BurstCaching
import com.graphbrain.hgdb.OpLogging
import com.graphbrain.hgdb.TextNode
import com.graphbrain.hgdb.ImageNode
import com.graphbrain.hgdb.Edge
import com.graphbrain.hgdb.SourceNode
import com.graphbrain.hgdb.URLNode
import com.graphbrain.hgdb.SVGNode
import com.graphbrain.hgdb.Vertex
import com.graphbrain.hgdb.EdgeType
import com.graphbrain.searchengine.Indexing

class OutputDBWriter(storeName:String, source:String) {
	

	val store = new VertexStore(storeName) with Indexing with BurstCaching
	val wikiURL = "http://en.wikipedia.org/wiki/"

	def writeOutDBInfo(node1: String, relin: String, node2: String, resource: String):Unit=
	{
		

		try{
			val rel=ID.relation_id(relin);
			val sourceNode=store.getSourceNode(ID.source_id(source))
			val global1 = ID.text_id(node1, "1")
			val global2 = ID.text_id(node2, "1")
			val globalRelType = ID.text_id(rel, "1")
			val userNode1 = ID.user_id(global1, "dbpedia")
			val userNode2 = ID.user_id(global2, "dbpedia")
			val userRelType = ID.user_id(globalRelType, "dbpedia")

			val N1Wiki=ID.wikipedia_id(node1)
			val N2Wiki=ID.wikipedia_id(node2)
			
			val nw1 = TextNode(id=N1Wiki, text=URLDecoder.decode(node1, "UTF-8"));
			val nw2 = TextNode(id=N2Wiki, text=URLDecoder.decode(node2, "UTF-8"));

			val ng1 = TextNode(id = global1, text=URLDecoder.decode(node1, "UTF-8"))
			val ng2 = TextNode(id = global2, text=URLDecoder.decode(node2, "UTF-8"))
			val rg = TextNode(id = globalRelType, text=URLDecoder.decode(relin, "UTF-8"))
			
			val ug1 = TextNode(id = userNode1, text=URLDecoder.decode(node1, "UTF-8"))
			val ug2 = TextNode(id = userNode2, text=URLDecoder.decode(node2, "UTF-8"))
			val ru = TextNode(id = userRelType, text=URLDecoder.decode(relin, "UTF-8"))


			val relType = EdgeType(id = ID.reltype_id(rel), label = rel);
			getOrInsert(relType)
			getOrInsert(nw1)
			getOrInsert(nw2)
			getOrInsert(ng1)
			getOrInsert(ng2)
			getOrInsert(rg)
			getOrInsert(ug1)
			getOrInsert(ug2)
			getOrInsert(ru)
			
		
			
			//val n1RNode = URLNode(ID.url_id(wikiURL+N1Wiki), wikiURL+N1Wiki)
			//val n2RNode = URLNode(ID.url_id(wikiURL+N2Wiki), wikiURL+N2Wiki)
			
			//getOrInsert(n1RNode)
			//getOrInsert(n2RNode)			
			//store.addrel("en_wikipage", Array[String](n1RNode.id, n1.id))
			//store.addrel("en_wikipage", Array[String](n2RNode.id, n2.id))
			store.addrel("source", Array[String](sourceNode.id, nw1.id))
			store.addrel("source", Array[String](sourceNode.id, nw2.id))
		
						
			//The id for the relationship between two nodes
			//val relID = getRelID(rel, ng1.id, ng2.id)
		
			//Relationship at global level
			store.addrel(rel, Array[String](ng1.id, ng2.id))
			
			/*
			if(resource!="")
			{
				val resourceNode = URLNode(ID.url_id(resource), resource)	
				getOrInsert(resourceNode)
				//Only the explicit reference in the DBPedia record is included - the Wikipedia pages to the nodes are not.
				store.addrel("source", Array[String](sourceNode.id, resourceNode.id))
				store.addrel("en_wikipage_line", Array[String](resourceNode.id, relID))
			}*/	
		}
		catch {
			case e => e.printStackTrace()
		}
		
		

	}

	def getOrInsert(node:Vertex):Vertex =
	{
		try{
			return store.get(node.id)
		}
		catch{
			case e => store.put(node)
			return store.get(node.id)
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

	def writeGeneratorSource(sourceID:String, sourceURL:String, output:OutputDBWriter)
  	{
  		try{
  			val sourceNode=SourceNode(id=ID.source_id(sourceID))
	    	val urlNode=URLNode(ID.url_id(sourceURL), sourceURL)
	    
	    	getOrInsert(sourceNode)
	    	getOrInsert(urlNode)
	    	store.addrel("source", Array[String](sourceNode.id, urlNode.id))
	    }
	    catch{
	    	case e => e.printStackTrace()
	    }
  	}

  	def writeURLNode(node:Vertex, url:String)
  	{
  		try{
  			val sourceNode=store.getSourceNode(ID.source_id(source))
  			val urlNode = URLNode(ID.url_id(url), url)	
  			getOrInsert(node)
  			getOrInsert(urlNode);
  			getOrInsert(sourceNode)
  			store.addrel("en_wikipage", Array[String](urlNode.id, node.id)); 
  			store.addrel("source", Array[String](sourceNode.id, urlNode.id))
  			
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
			
			getOrInsert(sourceNode);
			getOrInsert(urlNode);
			store.addrel("source", Array[String](sourceNode.id, urlNode.id))			
				
			if(image=="")
			{
				return;
			}
			image match{
				case a:String => val imageLocal=SVGNode(id=ID.nounproject_id(imagename), svg=a)
					getOrInsert(imageLocal)
					store.addrel("image_page", Array[String](urlNode.id,imageLocal.id))
					store.addrel("source", Array[String](sourceNode.id, imageLocal.id))
					val attribution = imagename + NounProjectCrawler.attributionText + contributorName;
					val contributorNode = TextNode(id=ID.nounproject_id(contributorName), text=contributorName);
					val contURLNode = URLNode(id=ID.url_id(contributorURL), url=contributorURL);

					getOrInsert(contributorNode)
					getOrInsert(contURLNode)
					store.addrel("attribute_as", Array[String](imageLocal.id, contributorNode.id))
					store.addrel("contributor_page", Array[String](contURLNode.id, contributorNode.id, imageLocal.id))


					getOrInsert(imageLocal);

					if(nodeExists(wikinode)) 
					{
						store.addrel("image_of", Array[String](imageLocal.id, wikinode.id))

					
					}
					else{
					//If no wikipedia, create new non-wikipedia node:
						val newNode=TextNode(id=ID.text_id(imagename, "noun"), text=imagename)
						getOrInsert(newNode);
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