package com.graphbrain.webapp

import com.graphbrain.gbdb.UserOps
import com.graphbrain.gbdb.Vertex
import com.graphbrain.gbdb.Edge
import com.graphbrain.gbdb.TextNode
import com.graphbrain.gbdb.URLNode
import com.graphbrain.gbdb.UserNode
import com.graphbrain.gbdb.ContextNode
import com.graphbrain.gbdb.ID
import com.graphbrain.gbdb.IdFamily
import com.graphbrain.gbdb.JSONGen

import com.graphbrain.gbdb.VertexStore
import com.graphbrain.gbdb.UserManagement
import com.graphbrain.gbdb.SimpleCaching
import scala.math


object VisualGraph {
  val MAX_SNODES = 15


  def generate(rootId: String, store: UserOps, user: UserNode, edgeType: String = "", relPos: Integer = -1) = {
    val userId = if (user != null) user.id else ""
    
    // get neighboring edges
    val hyperEdges = store.neighborEdges2(rootId, userId, edgeType, relPos)
    
    // map hyperedges to visual edges
    val visualEdges = hyperEdges.map(hyper2edge(_, rootId)).filter(_ != null)

    // group nodes by edge type
    val edgeNodeMap = generateEdgeNodeMap(visualEdges, rootId)

    // truncate edge node map
    val truncatedEdgeNodeMap = truncateEdgeNodeMap(edgeNodeMap, MAX_SNODES)

    // full relations list
    val allRelations = edgeNodeMap.keys.map(x => Map(("rel" -> x._1), ("pos" -> x._2),
      ("label" -> linkLabel(x._1)), ("snode" -> snodeId(x._1, x._2))))

    // create map with all information for supernodes
    val snodeMap = generateSnodeMap(truncatedEdgeNodeMap, store, rootId, user)

    // contexts
    val contexts = if ((user != null) && (user.contexts != null)) {
      user.contexts.map(c => Map(("id" -> c.id), ("name" -> c.name), ("access" -> c.access)))
    }
    else {
      Nil
    }

    // create reply structure with all the information needed for rendering
    val reply = Map(("user" -> userId), ("root" -> node2map(rootId, "", store, rootId, user)), 
      ("snodes" -> snodeMap), ("allrelations" -> allRelations), ("context" -> ID.contextId(rootId)), ("contexts" -> contexts))

    Server.store.clear()

    // generate json reply
    JSONGen.json(reply)
  }

  private def hyper2edge(edge: Edge, rootId: String) = {
    if (edge.participantIds.length > 2) {
      if (edge.edgeType == "rtype/1/instance_of~owned_by") {
        if (edge.participantIds(0) == rootId) {
          Edge("rtype/1/has", List(edge.participantIds(2), rootId), edge)
        }
        else if (edge.participantIds(2) == rootId) {
          Edge("rtype/1/has", List(rootId, edge.participantIds(0)), edge)
        }
        else {
          null
        }
      }
      if (edge.edgeType == "rtype/1/has~of_type") {
        val edgeType = "has " + ID.lastPart(edge.participantIds(2))
        Edge(edgeType, List(edge.participantIds(0), edge.participantIds(1)), edge)
      }
      else {
        val parts = edge.edgeType.split("~").toList
        val edgeType = parts.head + " " + ID.lastPart(edge.participantIds(1)) + " " + parts.tail.reduceLeft(_ + " " + _)
        //val edgeType = edge.edgeType.replaceAll("~", " .. ") + " .. "
        Edge(edgeType, List(edge.participantIds(0), edge.participantIds(2)), edge)
      }
    }
    else {
      edge
    }
  }

  private def generateEdgeNodeMap(edges: Set[Edge], rootId: String) = {
    edges.map(
      e => e.participantIds
        .zip(0 until e.participantIds.length)
        .map(x => (e.edgeType, x._2, x._1, e.getOriginalEdge.toString))
    ).flatten
      .filter(x => x._3 != rootId)
      .groupBy(x => (x._1, x._2))
      .mapValues(x => x.map(y => (y._3, y._4)))
  }

  private def truncateEdgeNodeMap(edgeNodeMap: Map[(String, Int), Set[(String, String)]], maxSize: Int) = {
    edgeNodeMap.zipWithIndex.filter(x=> x._2 < maxSize).map(x => x._1)
  }

  private def node2map(nodeId: String, nodeEdge: String, store: UserOps, rootId: String, user: UserNode) = {
    val family = IdFamily.family(nodeId)
    val node = if ((nodeId != rootId) && ((family == IdFamily.Global) || (family == IdFamily.UserSpace))) {
      TextNode.fromId(nodeId, store)
    }
    else{
      try {
        store.get(nodeId)
      }
      catch {
        case _ => null
      }
    }

    node match {
      case tn: TextNode => Map(("id" -> tn.id), ("type" -> "text"), ("text" -> tn.text), ("edge" -> nodeEdge))
      case un: URLNode => {
        val title = if (un.title == "") un.url else un.title
        Map(("id" -> un.id), ("type" -> "url"), ("text" -> title), ("url" -> un.url), ("icon" -> un.icon), ("edge" -> nodeEdge))
      }
      case un: UserNode => Map(("id" -> un.id), ("type" -> "user"), ("text" -> un.name), ("edge" -> nodeEdge))
      case cn: ContextNode => Map(("id" -> cn.id), ("type" -> "context"), ("text" -> user.name), ("text2" -> ID.humanReadable(cn.id)), ("edge" -> nodeEdge))
      case null => ""
      case _ => Map(("id" -> node.id), ("type" -> "text"), ("text" -> node.id), ("edge" -> nodeEdge))
    }
  }

  private def snodeId(edgeType: String, pos: Integer) = edgeType.replaceAll("/", "_").replaceAll(" ", "_").replaceAll("\\.", "_") + "_" + pos

  private def generateSnode(pair: ((String, Int), Set[(String, String)]), store: UserOps, rootId: String, user: UserNode) = {
    val id = snodeId(pair._1._1, pair._1._2)
    val label = linkLabel(pair._1._1)
    val color = linkColor(label)
    val nodes = pair._2.map(x => node2map(x._1, x._2, store, rootId, user))

    val data = Map(("nodes" -> nodes), ("etype" -> pair._1._1), ("rpos" -> pair._1._2), ("label" -> label), ("color" -> color))

    id -> data
  }

  private def generateSnodeMap(edgeNodeMap: Map[(String, Int), Set[(String, String)]], store: UserOps, rootId: String, user: UserNode) = {
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