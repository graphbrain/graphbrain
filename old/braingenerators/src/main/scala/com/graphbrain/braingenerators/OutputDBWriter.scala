package com.graphbrain.braingenerators

import java.net.URLDecoder
import com.graphbrain.db.{ID => HGDBID, _}
import scala.collection.JavaConversions._


class OutputDBWriter(storeName:String, source:String, username:String, name:String, role:String) {

	val store = new Graph(storeName)
	val wikiURL = "http://en.wikipedia.org/wiki/"
	val wikiPageET = store.put(new EdgeType(ID.reltype_id("sys/wikipage"), "wikipage"))
	val wikiPageTitle = store.put(new EdgeType(ID.reltype_id("sys/wikipedia"), "wikipedia"))
	val lineRegex = """#.*?""".r
	val wikiRel = "sys/wikipedia"
	val asInRel = ID.reltype_id("as in", 1)
	

	def writeOutDBInfo(node1: String, relin: String, node2: String, resource: String):Unit = {
		try {
			val globalRelType = ID.reltype_id(separateWords(relin.trim), 1)
			
			val ng1 = insertAndGetWikiDisambigNode(node1, username)
			val ng2 = insertAndGetWikiDisambigNode(node2, username)
			val relType = new EdgeType(globalRelType, separateWords(relin.trim))
      store.put(relType)
			
			println("relType: " + store.getOrInsert(relType, HGDBID.userIdFromUsername(username)).id + ", " + relType.getLabel)
			println("ng1: " + store.getOrInsert(ng1, HGDBID.userIdFromUsername(username)).id)
			println("ng2: " + store.getOrInsert(ng2, HGDBID.userIdFromUsername(username)).id)
			
			// Relationship at global level
			store.connectVertices(Array[String](relType.id, ng1.id, ng2.id), HGDBID.userIdFromUsername(username))
		}
		catch {
			case e: Throwable => e.printStackTrace()
		}
	}

	def nodeExists(node:Vertex):Boolean = {
		try {
			store.get(node.id)
			true
		}
		catch {
			case e: Throwable => return false
		}	
	}

	def removeWikiDisambig(wikiTitle: String): String =
    wikiTitle.split("""\(""")(0).reverse.dropWhile(_ == '_').reverse.trim

	def insertAndGetWikiDisambigNode(wikiTitle: String, username: String): Vertex = {
		val decodedTitle = URLDecoder.decode(wikiTitle, "UTF-8")
		val titleSP = removeWikiDisambig(decodedTitle)
		
		var i = 1
		val wikiNode = store.put(EntityNode.fromNsAndText("wikipedia", URLDecoder.decode(wikiTitle, "UTF-8")))

		while(store.exists(ID.text_id(titleSP, i.toString))) {
			val existingNode = store.get(ID.text_id(titleSP, i.toString))
			val disAmb = """\(.*?\)""".r.findAllIn(decodedTitle)
			existingNode match {
				case e: EntityNode =>
				  if(disAmb.hasNext) {
				  	val da = disAmb.next().replace("(", "").replace(")", "").trim
				  	val daNode = store.put(EntityNode.fromNsAndText("1", da))
				  	val daID = daNode.id
				  	val daRel = Edge.fromParticipants(Array(asInRel, e.id, daID))
				  	if(e.text == titleSP.toLowerCase) {
				  		for(nEdge <- store.edges(existingNode.id)) {
				  			println(nEdge)
				  			if(nEdge == daRel) {
				  				println("Match:" + nEdge)
				  				return existingNode
				  			}
				  		}
				  	}
				  }
				  else {
				  	if(e.text == titleSP.toLowerCase) {
				  		return existingNode
				  	}
				  }
				case _ =>
			}
			i += 1
		}
		val newNode = store.put(EntityNode.fromNsAndText(i.toString, titleSP))
		println(store.getOrInsert(newNode, HGDBID.userIdFromUsername(username)).id)

		
		if(!store.exists(wikiNode.id)) {
		  store.put(wikiNode)	
		  println("Wiki_ID: " + store.get(wikiNode.id).id)
		}
		try {
		  store.connectVertices(Array[String](wikiPageTitle.id, newNode.id, wikiNode.id))
	  }
	  catch {
	    case e: Throwable => e.printStackTrace()
	  }
		
		val disAmbA = """\(.*?\)""".r.findAllIn(decodedTitle)
		
		if(disAmbA.hasNext) {
			val da = disAmbA.next().replace("(", "").replace(")", "").trim
			val daNode = EntityNode.fromNsAndText("1", da)
      store.put(daNode)
			println(store.getOrInsert(daNode, HGDBID.userIdFromUsername(username)).id + ", da: " +  daNode.text)
			store.connectVertices(Array[String](asInRel, newNode.id, daNode.id), HGDBID.userIdFromUsername(username))
		}

		newNode
	}

  /*
	def writeGeneratorSource(sourceID:String, sourceURL:String) = {
  		try{
  			val sourceNode = store.createSourceNode(id=ID.source_id(sourceID))
	    	val urlNode = store.createURLNode(ID.url_id(sourceURL), sourceURL)
	    	println(username)
	    	store.getOrInsert2(sourceNode, HGDBID.userIdFromUsername(username))
	    	store.getOrInsert2(urlNode, HGDBID.userIdFromUsername(username))
	    	store.addrel2("source", Array[String](sourceNode.id, urlNode.id), HGDBID.userIdFromUsername(username), true)
	    	store.getOrInsert2(wikiPageET, HGDBID.userIdFromUsername(username))
	    }
	    catch{
	    	case e => e.printStackTrace()
	    }
  	}*/

  def writeUser() = {
    try {
  		store.createUser(username, name, "", "", role)
  	}
  	catch {
  		case e: Throwable => e.printStackTrace()
  	}
  }

  def writeURLNode(node:Vertex, url:String) = {
  	try {
  		//val sourceNode = store.getSourceNode(ID.source_id(source))
  		val urlNode = store.put(new URLNode(ID.url_id(url), url))
  		store.getOrInsert(node, HGDBID.userIdFromUsername(username))
  		store.getOrInsert(urlNode, HGDBID.userIdFromUsername(username))
  		//store.getOrInsert(sourceNode, HGDBID.userIdFromUsername(username))
  		store.connectVertices(Array[String]("en_wikipage", urlNode.id, node.id), HGDBID.userIdFromUsername(username))
  		//store.addrel2("source", Array[String](sourceNode.id, urlNode.id), HGDBID.userIdFromUsername(username))
  	}
  	catch {
  		case e: Throwable => e.printStackTrace()
  	}
  }

  def writeNounProjectImageNode(imagename:String, url:String, image:String="", contributorName:String="", contributorURL:String=""): Unit = {
  	try {
  		//Tries to find an existing Wiki node.
			val wikinode = store.put(EntityNode.fromNsAndText("wikipedia", imagename))

  		//val sourceNode=store.getSourceNode(ID.source_id(source))
  		val urlNode = store.put(new URLNode(url))
			
			//store.getOrInsert(sourceNode, HGDBID.userIdFromUsername(username))
			store.getOrInsert(urlNode, HGDBID.userIdFromUsername(username))
			//store.connectVertices(Array[String]("source", sourceNode.id, urlNode.id))

				
			if(image == "") {
				return
			}
			image match{
				case a:String => 
					val imageLocal = store.put(EntityNode.fromNsAndText(ID.nounproject_ns(), a))
					store.getOrInsert(imageLocal, HGDBID.userIdFromUsername(username))
					store.connectVertices(Array[String]("image_page", urlNode.id,imageLocal.id))
					//store.connectVertices(Array[String]("source", sourceNode.id, imageLocal.id))
					//val attribution = imagename + NounProjectCrawler.attributionText + contributorName
					val contributorNode = store.put(EntityNode.fromNsAndText(ID.nounproject_ns(), contributorName))
					val contURLNode = store.put(new URLNode(contributorURL))

					store.getOrInsert(contributorNode, HGDBID.userIdFromUsername(username))
					store.getOrInsert(contURLNode, HGDBID.userIdFromUsername(username))
					store.connectVertices(Array[String]("attribute_as", imageLocal.id, contributorNode.id))
					store.connectVertices(Array[String]("contributor_page", contURLNode.id, contributorNode.id, imageLocal.id))

					store.getOrInsert(imageLocal, HGDBID.userIdFromUsername(username))

					if(nodeExists(wikinode))  {
						store.connectVertices(Array[String]("image_of", imageLocal.id, wikinode.id))
					}
					else {
						val newNode = store.put(EntityNode.fromNsAndText("noun", imagename))
						store.getOrInsert(newNode, HGDBID.userIdFromUsername(username))
						store.connectVertices(Array[String]("image_of", imageLocal.id, newNode.id))
					}
			  }
  		}
  	}

  	def addWikiPageToDB(pageTitle:String):Unit=
  	{
    	val pageURL = Wikipedia.wikipediaBaseURL+pageTitle.trim.replace(" ", "_")
    	val pageNode = store.put(EntityNode.fromNsAndText("wikipedia", pageTitle))
    	writeURLNode(pageNode, pageURL)
  	}

  	def separateWords(stringToSep: String) : String = {
  		val capRegex = """[A-Z][a-z]*""".r
  		val nonCapRegex = """[a-z]+""".r
  		var separated = ""
  		var nonCapsSep = nonCapRegex.findAllIn(stringToSep)
  		var capsSeparated = capRegex.findAllIn(stringToSep)
  		
  		if(nonCapsSep.length > 0) {
  			nonCapsSep = nonCapRegex.findAllIn(stringToSep)
  			separated += " " + nonCapsSep.next().toLowerCase
  		}

  		if(capsSeparated.length > 0) {
  		  capsSeparated = capRegex.findAllIn(stringToSep)
  		  while(capsSeparated.hasNext) {
          separated += " " + capsSeparated.next().toLowerCase
  		  }
  		} 
  		separated.trim
  	}

	def getRelID(rel:String, node1ID:String, node2ID:String):String=
	{
		val pageTokens=List[String](rel)++Array[String](node1ID, node2ID)
		pageTokens.reduceLeft(_+ " " +_)
	}

	def finish() = {}
}

object OutputDBWriter {
	def main(args : Array[String]) : Unit = {
		val test = new OutputDBWriter(storeName = "gb", source = DBPediaGraphFromCategories.sourceName, username = "dbpedia", name = "dbpedia", role = "crawler")
		val sister1 = "Sizzzy_(film)"
		val sister2 = "Sizzzy_(band)"
		val sister3 = "Sizzzy_(tree)"
		test.writeOutDBInfo(sister1, "is a", "film", "http://en.wikipedia.org/wiki/Sister_(film)")
		test.writeOutDBInfo(sister1, "is a", "film", "http://en.wikipedia.org/wiki/Sister_(film)")
		test.writeOutDBInfo(sister2, "likes", "music", "http://en.wikipedia.org/wiki/Sister_(band)")
		test.writeOutDBInfo(sister3, "likes", "music", "http://en.wikipedia.org/wiki/Sister_(tree)")
		println(test.store.get("1/sizzzy").id)
		println(test.store.get("2/sizzzy").id)
		println(test.store.get("3/sizzzy").id)
		//println(test.store.get("4/sizzzy").id)
    }
  
}
