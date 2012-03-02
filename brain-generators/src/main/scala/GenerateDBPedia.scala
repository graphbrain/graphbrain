import com.graphbrain.hgdb.VertexStore
import com.graphbrain.hgdb.SimpleCaching
import com.graphbrain.hgdb.TextNode
import com.graphbrain.hgdb.ImageNode
import com.graphbrain.hgdb.Edge
import com.graphbrain.hgdb.SourceNode
import com.graphbrain.hgdb.URLNode





object GenerateDBPedia {

	
	def main(args : Array[String]) : Unit = {
		val testStoreName = "testgbdb"
		DBPediaGraphFromInfobox.processFile("mappingbased_properties_en.nq", new OutputDBWriter("testgbdb", DBPediaGraphFromInfobox.sourceName), 0-1)
		DBPediaGraphFromCategories.processFile("instance_types_en.nq", new OutputDBWriter("testgbdb", DBPediaGraphFromCategories.sourceName), 0-1)
		
			
	}


}