import scala.util.Random;
import scala.io._

object InferenceTester 
{
	def generateRandomGraph(relations: List[String], objects: List[String], numRelated: Int): List[(String, String, String)]=
	{
		val r = new Random()
		var graph:List[(String, String, String)]=List()

		for(i<-0 until numRelated)
		{
			val related=(objects(r.nextInt(objects.length)), relations(r.nextInt(relations.length)), objects(r.nextInt(objects.length)))
			graph=related::graph

					

		}
		return graph;
		
	}

	def testRandomGraph(numRelationships: Int) 
    {
    	val objects=List("David Lynch", "graphbrain", "london", "film", "book", "city", "Blue Velvet", "person", "dog", "director", "mammal")
    	val relationships=List("likes", "made", "is a", "likes", "hates", "dreams of")
      	val graph=generateRandomGraph(relationships, objects, numRelationships)
      	println(graph)
      	println(LocalInference.getNodeIndexedRelations(graph))
      	println(LocalInference.getRelationIndexedNodes(graph))
      	println(LocalInference.getAllNodes(graph))
      	println(LocalInference.getAllRelations(graph))
      	


    }

    def generateGraphFromFile(fileName: String):List[(String, String, String)]=
    {
    	var graph:List[(String, String, String)]=List()
    	
    	val file=Source.fromFile(fileName).getLines()
    	for(line <- file)
    	{
    		
    		
    		val r = line.split(", ")
    		val related=(r(0), r(1), r(2))
    		graph=related::graph
    	}

    	return graph
    	
    	
    }
	def main(args: Array[String])
    {
    	print(generateGraphFromFile(args(0)))
    	//testRandomGraph(args(0).toInt)
    }
}