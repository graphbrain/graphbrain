//import com.mongodb.casbah.Imports._
import scala.util.Random;
import scala.collection.mutable;

object LocalInference 
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

	/**
	*Finds the relations in which the root node participates in a list of orig-rel-targ and returns a list of relation-role tuples (in the binary case only orig and target but later may have other roles in n-ary relations).
	*/
	def getAllRelationsForNode(graph: List[(String, String, String)], root: String): List[(String, String)]=
	{

		//first tuple element is the relation, second tuple element is the role.
		var relRole:List[(String, String)]=List()
		for(related <- graph)
		{

			val relation=related._2
			val orig=related._1
			val targ=related._3
			if(orig==root)
			{
				
				relRole=(relation.toString(), "orig")::relRole
			}
			else if(orig==targ)
			{
				relRole=(relation.toString(), "targ")::relRole
			}
		}
		return relRole

	}

		/**
	*Finds the nodes participating in the relation and the role they play.
	*/
	def getAllNodesForRelation(graph: List[(String, String, String)], relation: String): List[(String, String)]=
	{

		//first tuple element is the node, second tuple element is the role.
		var nodeRole:List[(String, String)]=List()
		for(related <- graph)
		{

			val rel=related._2
			if(rel==relation)
			{
				nodeRole=(related._1.toString(), "orig")::nodeRole
				nodeRole=(related._3.toString(), "targ")::nodeRole

			}
		}
		return nodeRole

	}



	def numSharedRelations(relations1: List[(String, String)], relations2: List[(String, String)]):Int=
	{
		var count = 0;
		for(rel1 <- relations1)
		{
			for(rel2 <- relations2)
			{
				if(rel1==rel2)
				{
					count+=1;
				}
			}
		}
		return count;
	}

	/**
	Returns the set of (distinct) nodes.
	*/
	def getAllNodes(graph: List[(String, String, String)]):List[String]=
	{
		var nodes:List[String]=List()
		for(entry <- graph)
		{
			nodes=entry._1::nodes
			nodes=entry._3::nodes
		}
		return nodes.distinct
	}

		/**
	Returns the set of (distinct) relations.
	*/
	def getAllRelations(graph: List[(String, String, String)]):List[String]=
	{
		var relations:List[String]=List()
		for(entry <- graph)
		{
			val edges=entry._2::relations

		}
		return relations.distinct
	}

	/**
	Returns the set of all nodes with the relations in which they particpate and their role in these relations (orig or target in the binary case).
	*/
	def getNodeIndexedRelations(graph: List[(String, String, String)]): mutable.HashMap[String, List[(String, String)]]=
	{
		var relationRoles=new mutable.HashMap[String, List[(String, String)]]
		val nodes = getAllNodes(graph)
		 
      	println(nodes)
      	for(node <- nodes)
      	{
      		val current=getAllRelationsForNode(graph, node);
      		relationRoles.put(node, current)
      	
      	}
      	return relationRoles		
	}

		/**
	Returns the set of all relations with their participating nodes (and the roles of these nodes in the relation).
	*/
	def getRelationIndexedNodes(graph: List[(String, String, String)]): mutable.HashMap[String, List[(String, String)]]=
	{
		var relationRoles=new mutable.HashMap[String, List[(String, String)]]
		val relations = getAllRelations(graph)
		 
      	for(relation <- relations)
      	{
      		//START HERE
      		val current=getAllNodesForRelation(graph, relation);
      		relationRoles.put(relation, current)
      	
      	}
      	return relationRoles		
	}


    def testRandomGraph(numRelationships: Int) 
    {
    	val objects=List("David Lynch", "graphbrain", "london", "film", "book", "city", "Blue Velvet", "person", "dog", "director", "mammal")
    	val relationships=List("likes", "made", "is a", "likes", "hates", "dreams of")
      	val graph=generateRandomGraph(relationships, objects, numRelationships)
      	println(graph)
      	println(getNodeIndexedRelations(graph))
      	println(getRelationIndexedNodes(graph))
      	println(getAllNodes(graph))
      	println(getAllRelations(graph))
      	


    }
    def main(args: Array[String])
    {
    	testRandomGraph(args(0).toInt)
    }
}
