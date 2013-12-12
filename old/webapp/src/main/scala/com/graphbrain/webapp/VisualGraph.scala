package com.graphbrain.webapp

import scala.math
import com.graphbrain.db._
import com.graphbrain.utils.JSONGen
import scala.collection.JavaConversions._
import scala.collection.mutable.Set

object VisualGraph {
  val MAX_SNODES = 15


  def generate(rootId: String, store: Graph, user: UserNode, edgeType: String = "", relPos: Integer = -1) = {
    val userId = if (user != null) user.id else ""
    
    // get neighboring edges
    val hyperEdges = store.edges(rootId, userId).filter(_.isPositive)

    // map hyperedges to visual edges
    val visualEdges = hyperEdges.map(hyper2edge(_, rootId)).filter(_ != null)

    // group nodes by edge type
    val edgeNodeMap = generateEdgeNodeMap(visualEdges, rootId)

    // truncate edge node map
    val truncatedEdgeNodeMap = truncateEdgeNodeMap(edgeNodeMap, MAX_SNODES)

    // full relations list
    val allRelations = edgeNodeMap.keys.map(x => Map("rel" -> x._1, "pos" -> x._2,
      "label" -> linkLabel(x._1), "snode" -> snodeId(x._1, x._2)))

    // create map with all information for supernodes
    val snodeMap = generateSnodeMap(truncatedEdgeNodeMap, store, rootId, user)

    // contexts
    val contexts = Nil

    // create reply structure with all the information needed for rendering
    val reply = Map("user" -> userId, "root" -> node2map(rootId, "", store, rootId, user),
      "snodes" -> snodeMap, "allrelations" -> allRelations, "context" -> "", "contexts" -> contexts)

    //WebServer.graph.clear()

    // generate json reply
    JSONGen.json(reply)
  }

  private def hyper2edge(edge: Edge, rootId: String): SimpleEdge = {
    if (edge.getParticipantIds.length > 2) {
      if (edge.getEdgeType == "r/1/instance_of~owned_by") {
        if (edge.getParticipantIds()(0) == rootId) {
          new SimpleEdge("r/1/has", edge.getParticipantIds()(2), rootId, edge)
        }
        else if (edge.getParticipantIds()(2) == rootId) {
          new SimpleEdge("r/1/has", rootId, edge.getParticipantIds()(0), edge)
        }
        else {
          null
        }
      }
      if (edge.getEdgeType == "r/1/has~of_type") {
        val edgeType = "has " + ID.lastPart(edge.getParticipantIds()(2))
        new SimpleEdge(edgeType, edge.getParticipantIds()(0), edge.getParticipantIds()(1), edge)
      }
      else {
        val parts = edge.getEdgeType.split("~").toList
        val edgeType = parts.head + " " + ID.lastPart(edge.getParticipantIds()(1)) + " " + parts.tail.reduceLeft(_ + " " + _)
        //val edgeType = edge.edgeType.replaceAll("~", " .. ") + " .. "
        new SimpleEdge(edgeType, edge.getParticipantIds()(0), edge.getParticipantIds()(2), edge)
      }
    }
    else {
      new SimpleEdge(edge)
    }
  }

  private def generateEdgeNodeMap(edges: Set[SimpleEdge], rootId: String) = {
    edges.map(
      e => List(e.getId1 -> 0, e.getId2 -> 1)
        .map(x => (e.getEdgeType, x._2, x._1, e.getParent.toString))
    ).flatten
      .filter(x => x._3 != rootId)
      .groupBy(x => (x._1, x._2))
      .mapValues(x => x.map(y => (y._3, y._4)))
  }

  private def truncateEdgeNodeMap(edgeNodeMap: Map[(String, Int), Set[(String, String)]], maxSize: Int) = {
    edgeNodeMap.zipWithIndex.filter(x=> x._2 < maxSize).map(x => x._1)
  }

  private def node2map(nodeId: String, nodeEdge: String, store: Graph, rootId: String, user: UserNode) = {
    val vtype = VertexType.getType(nodeId)
    val node = if ((nodeId != rootId) && (vtype == VertexType.Entity)) {
      new EntityNode(nodeId)
    }
    else{
      try {
        store.get(nodeId)
      }
      catch {
        case _: Throwable => null
      }
    }

    node match {
      case tn: EntityNode => Map("id" -> tn.id, "type" -> "text", "text" -> tn.text, "edge" -> nodeEdge)
      case un: URLNode => {
        val title = if (un.getTitle == "") un.getUrl else un.getTitle
        Map("id" -> un.id, "type" -> "url", "text" -> title, "url" -> un.getUrl, "icon" -> un.getIcon, "edge" -> nodeEdge)
      }
      case un: UserNode => Map("id" -> un.id, "type" -> "user", "text" -> un.getName, "edge" -> nodeEdge)
      case null => ""
      case _ => Map("id" -> node.id, "type" -> "text", "text" -> node.id, "edge" -> nodeEdge)
    }
  }

  private def snodeId(edgeType: String, pos: Integer) = edgeType.replaceAll("/", "_").replaceAll(" ", "_").replaceAll("\\.", "_") + "_" + pos

  private def generateSnode(pair: ((String, Int), Set[(String, String)]), store: Graph, rootId: String, user: UserNode) = {
    val id = snodeId(pair._1._1, pair._1._2)
    val label = linkLabel(pair._1._1)
    val color = linkColor(label)
    val nodes = pair._2.map(x => node2map(x._1, x._2, store, rootId, user))

    val data = Map("nodes" -> nodes, "etype" -> pair._1._1, "rpos" -> pair._1._2, "label" -> label, "color" -> color)

    id -> data
  }

  private def generateSnodeMap(edgeNodeMap: Map[(String, Int), Set[(String, String)]], store: Graph, rootId: String, user: UserNode) = {
    edgeNodeMap.map(x => generateSnode(x, store, rootId, user))
  }

  private def linkColor(label: String) = {
    val index = math.abs(label.hashCode) % Colors.colors.length
    Colors.colors(index)
  }

  private def fixLabel(label: String) = if (EdgeLabelTable.table.contains(label)) EdgeLabelTable.table(label) else label

  private def linkLabel(edgeType: String): String = {
    if (edgeType == "")
      return ""
    val lastPart = ID.parts(edgeType).last
    fixLabel(lastPart.replace("_", " "))
  }

  /*
  def main(args: Array[String]) {
    val store = new VertexStore with SimpleCaching with UserOps with UserManagement
      
    println(VisualGraph.generate("1/eraserhead", store, null))

    sys.exit(0)
  }*/
}