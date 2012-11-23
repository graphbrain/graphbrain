package com.graphbrain.webapp

import com.graphbrain.hgdb.UserOps
import com.graphbrain.hgdb.Vertex
import com.graphbrain.hgdb.Edge
import com.graphbrain.hgdb.TextNode
import com.graphbrain.hgdb.URLNode
import com.graphbrain.hgdb.UserNode
import com.graphbrain.hgdb.ID
import scala.collection.mutable.{Map => MMap}
import scala.collection.mutable.{Set => MSet}
import scala.math


class GraphInterface (val rootId: String, val store: UserOps, val user: UserNode) {
  val userId = if (user != null) user.id else ""
  val edgeIds = store.neighborEdges2(rootId, userId)
  val neighbors = store.nodesFromEdgeSet(edgeIds)
  val snodes = supernodes
  val nodesJSON = nodes2json
  val snodesJSON = snodes2json

  Server.store.clear()

  /** Generates relationship Map
    * 
    * Generates a Map where all nodeIds that participate in the same
    * kind of relationship are grouped.
    */
  // NOTE: We only consider the first two participants for now
  private def relmap = {
    val rm = MMap[(String, Int, String), Set[String]]()
    for (edge <- edgeIds) {
      val participants = edge.participantIds.slice(0, 2)
      for (i <- 0 until participants.size) {
        val key = (edge.edgeType, i, participants(i))
        val others = participants.filter(p => (p != participants(i)) && (p != rootId)).toSet
        if (rm contains key)
          rm(key) ++= others
        else
          rm(key) = others
      }
    }
    rm
  }

  /** Generates supernodes Map */
  private def supernodes = {
    var snodes = MSet[Map[String, Any]]()
    val rm = relmap
    var superId = 0

    // create root supernode
    var superkey = "sn" + superId
    snodes += Map[String, Any](("id" -> superkey), ("key" -> ("", -1, "")), ("nodes" -> Set(rootId)))

    superId += 1

    // create supernodes
    for ((key, nodes) <- rm) {
      if (nodes.size > 0) {
        superkey = "sn" + superId
        snodes += Map[String, Any](("id" -> superkey), ("key" -> key), ("nodes" -> nodes))
        superId += 1
      }
    }

    snodes
  }
  

  /** Generates a JSON friendly map from node */
  private def node2map(nodeId: String, parentId: String) = {
    val node = try {
      store.get(nodeId)
    }
    catch {
      case _ => null
    }
    node match {
      case tn: TextNode => Map(("type" -> "text"), ("text" -> tn.text), ("parent" -> parentId))
      case un: URLNode => {
        val title = if (un.title == "") un.url else un.title
        Map(("type" -> "url"), ("text" -> title), ("url" -> un.url), ("icon" -> un.icon), ("parent" -> parentId))
      }
      case un: UserNode => Map(("type" -> "user"), ("text" -> un.name), ("parent" -> parentId))
      case null => ""
      case _ => Map(("type" -> "text"), ("text" -> node.id), ("parent" -> parentId))
    }
  }

  /** Generates JSON string from nodes */
  private def nodes2json = {
    val json = (Map((rootId -> node2map(rootId, ""))) ++
        (for (n <- neighbors if (n != rootId)) yield
          (n -> node2map(n, rootId)))).toMap
    JSONGen.json(json)
  }

  /** Generates JSON string from supernodes */
  private def snodes2json = {
    val json = for (snode <- snodes) yield {
      val key = snode("key") match { 
	      case x: (String, Integer, String) => x
	      case _ => ("", -1, "")
      }
      val etype = key._1
      val label = linkLabel(etype)
      Map(("id" -> snode("id").toString), ("etype" -> etype), ("label" -> label), ("color" -> linkColor(label)), ("rpos" -> key._2), ("nodes" -> snode("nodes").asInstanceOf[Set[String]]))
    }
    JSONGen.json(json)
  }

  private def linkColor(label: String) = {
    val index = math.abs(label.hashCode) % Colors.colors.length
    Colors.colors(index)
  }

  private def linkLabel(edgeType: String): String = {
    if (edgeType == "")
      return ""
    val lastPart = ID.parts(edgeType).last
    lastPart
    lastPart.replace("_", " ")
  }
}
