package com.graphbrain.braingenerators


import com.graphbrain.hgdb.VertexStore
import com.graphbrain.hgdb.SimpleCaching
import com.graphbrain.hgdb.TextNode
import com.graphbrain.hgdb.ImageNode
import com.graphbrain.hgdb.Edge


object DataDBTest {

	def checkDBGraphFromInfobox():Unit=
	{
		val testStoreName = "testgbdb"
		DBPediaGraphFromInfobox.processFile("brain-generators/data-files/mappingbased_properties_en.nq", new OutputDBWriter(testStoreName), 100)
		val testStore = new VertexStore(testStoreName);
		val sourceNode = new SourceNode(DBPediaGraphFromInfobox.sourceName, DBPediaGraphFromInfobox.sourceURL, output)
		println(testStore.neighbours(sourceNode.id))
		
	}

	def main(args : Array[String]) : Unit = {

		DataDBTest.checkDBGraphfromInfobox();
	}




}