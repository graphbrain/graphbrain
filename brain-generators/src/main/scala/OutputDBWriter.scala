import java.io.BufferedWriter;
import java.io.FileWriter;
import scala.collection.immutable.HashMap
import com.graphbrain.hgdb.VertexStore
import com.graphbrain.hgdb.SimpleCaching
import com.graphbrain.hgdb.TextNode
import com.graphbrain.hgdb.ImageNode
import com.graphbrain.hgdb.Edge
import com.graphbrain.hgdb.SourceNode
import com.graphbrain.hgdb.URLNode

class OutputDBWriter(storeName:String, source:String) {
	

	val store = new VertexStore(storeName)
	val wikiURL = "http://en.wikipedia.org/wiki/"

	def writeOutDBInfo(node1: String, rel: String, node2: String, resource: String):Unit=
	{
		val sourceNode=store.get(ID.source_id(source))
		val N1Wiki=ID.wikipedia_id(node1)
		val N2Wiki=ID.wikipedia_id(node2)
		val n1 = TextNode(id=N1Wiki, text=node1);
		val n2 = TextNode(id=N2Wiki, text=node2);
		
		val n1RNode = URLNode(ID.url_id(wikiURL+N1Wiki), wikiURL+N1Wiki)
		val n2RNode = URLNode(ID.url_id(wikiURL+N2Wiki), wikiURL+N2Wiki)
		store.addrel("en_wikipage", Array[String](n1RNode.id, n1.id))
		
		store.addrel("en_wikipage", Array[String](n2RNode.id, n2.id))
		
		store.addrel("source", Array[String](sourceNode.id, n1.id))
		
		store.addrel("source", Array[String](sourceNode.id, n2.id))
		
		
						
		//The id for the relationship between two nodes
		val relID = getRelID(rel, n1.id, n2.id)
		store.addrel(rel, Array[String](ID.wikipedia_id(node1), ID.wikipedia_id(node2)))
		
		if(resource!="")
		{
			val resourceNode = URLNode(ID.url_id(resource), resource)	
			//Only the explicit reference in the DBPedia record is included - the Wikipedia pages to the nodes are not.
			store.addrel("source", Array[String](sourceNode.id, resourceNode.id))
			store.addrel("en_wikipage_line", Array[String](resourceNode.id, relID))
		}
		

	}

	def writeGeneratorSource(sourceID:String, sourceURL:String, output:OutputDBWriter)
  	{
  		val sourceNode=SourceNode(id=ID.source_id(sourceID))
	    val urlNode=URLNode(ID.url_id(sourceURL), sourceURL)
	    store.addrel("source", Array[String](sourceNode.id, urlNode.id))
  	}


	def getRelID(rel:String, node1ID:String, node2ID:String):String=
	{
		val pageTokens=List[String](rel)++Array[String](node1ID, node2ID)
		return pageTokens.reduceLeft(_+ " " +_)
	}


}