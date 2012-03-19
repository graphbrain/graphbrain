package com.graphbrain.webapp

import com.graphbrain.hgdb.VertexStore
import com.graphbrain.hgdb.Vertex
import com.graphbrain.hgdb.Edge
import com.graphbrain.hgdb.TextNode
import com.graphbrain.hgdb.ImageNode
import scala.collection.mutable.{Map => MMap}
import scala.collection.mutable.{Set => MSet}
import com.codahale.jerkson.Json._


class GraphInterface (val rootId: String, val store: VertexStore) {
  val neighbors = store.neighbors(rootId)
  val edgeIds = store.neighborEdges(neighbors)
  //val edges = edgeIds.map(e => store.getEdge(e))
  val snodes = supernodes
  val links = visualLinks
  val nodesJSON = nodes2json
  val snodesJSON = snodes2json
  val linksJSON = links2json

  /** Generates relationship Map
    * 
    * Generates a Map where all nodeIds that participate in the same
    * kind of relationship are grouped.
    */
  // NOTE: We only consider the first two participants for now
  private def relmap = {
    val rm = MMap[(String, Int, String), Set[String]]()
    for (edge <- edgeIds) {
      val participants = Edge.participantIds(edge).slice(0, 2)
      for (i <- 0 until participants.size) {
        val key = (Edge.edgeType(edge), i, participants(i))
        val others = participants.filter(p => (p != participants(i)) && (p != rootId)).toSet
        if (rm contains key)
          rm(key) ++= others
        else
          rm(key) = others
      }
    }
    rm
  }

  /** Key with highest numer of nodes
    * 
    * Returns the key from the relationsip map with the highest number
    * of associated nodes, or null if relationship map is empty.
    */
  private def maxrel(relmap: MMap[(String, Int, String), Set[String]]) =
    if (relmap.size == 0)
      null
    else
      (relmap max Ordering[Int].on[(_, Set[String])](_._2.size))._1

  /** Deletes key and related relationships from relationship map
    * 
    * Deletes key and it's value and removes node in key 
    * and nodes corresponding to that key from all other node lists.
    */
  private def delrel(rm: MMap[(String, Int, String), Set[String]], key: (String, Int, String)) {
    val nodes = rm(key)
    rm -= key
    for (k <- rm) {
      rm(k._1) = k._2.filter(id => (id != key._3) && !nodes.exists(_ == id))
      if (rm(k._1).size == 0) rm -= k._1
    }
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
    var key = maxrel(rm)
    while (key != null) {
      val nodes = rm(key)
      superkey = "sn" + superId
      snodes += Map[String, Any](("id" -> superkey), ("key" -> key), ("nodes" -> nodes))
      delrel(rm, key)
      key = maxrel(rm)
      superId += 1
    }

    // create unitary supernodes for remaining nodes
    for (n <- neighbors) {
      val nodeId = n._1
      if (!snodes.exists(sn => sn("nodes").asInstanceOf[Set[String]].exists(_ == nodeId))) {
        superkey = "sn" + superId
        snodes += Map[String, Any](("id" -> superkey), ("key" -> ("", -1, "")), ("nodes" -> Set(nodeId)))
        superId += 1
      }
    }

    snodes
  }

  /** Determines if an Edge is redundant in realtion to supernode links */
  private def redundantEdge(snodes: MSet[Map[String, Any]], edge: String): Boolean = {
    for (sn <- snodes) {
      val key: (String, Int, String) = sn("key").asInstanceOf[(String, Int, String)]
      val otherParticipants = Edge.participantIds(edge).toSet - key._3
      if ((Edge.edgeType(edge) == key._1) &&
        (Edge.participantIds(edge)(key._2) == key._3) && 
        otherParticipants.subsetOf(sn("nodes").asInstanceOf[Set[String]]))
        return true
    }
    false
  }

  /** Generates list of links to be displayed */
  private def visualLinks = {
    val nodeLinks = (for (e <- edgeIds if (!redundantEdge(snodes, e))) yield {
      val pids = Edge.participantIds(e)
      (Edge.edgeType(e), pids(0), pids(1), true, true)
    }).toSet

    val snodeLinks = (for (sn <- snodes) yield {
      val superkey = sn("id")
      val key: (String, Int, String) = sn("key").asInstanceOf[(String, Int, String)]
      if (key._2 == 0)
        (key._1, key._3, superkey, true, false)
      else
        (key._1, superkey, key._3, false, true)
    }).toSet.filter(_._1 != "")

    nodeLinks ++ snodeLinks
  }

  /** Generates a JSON friendly map from node */
  private def node2map(nodeId: String, parentId: String) = {
    val node = store.get(nodeId)
    node match {
      case tn: TextNode => Map(("type" -> "text"), ("text" -> tn.text), ("parent" -> parentId))
      case in: ImageNode => Map(("type" -> "image"), ("text" -> in.url), ("parent" -> parentId))
      case _ => Map(("type" -> "text"), ("text" -> node.id), ("parent" -> parentId))
    }
  }

  /** Generates JSON string from nodes */
  private def nodes2json = {
    val json = (for (n <- neighbors) yield
      (n._1 -> node2map(n._1, n._2))).toMap
    generate(json)
  }

  /** Generates JSON string from supernodes */
  private def snodes2json = {
    val json = for (snode <- snodes) yield
      Map(("id" -> snode("id").toString), ("nodes" -> snode("nodes").asInstanceOf[Set[String]]))
    generate(json)
  }

  /** Generates JSON string from links */
  private def links2json = {
    var lid = 0
    val json = for (l <- links) yield {
      lid += 1
      if (l._4 && l._5)
        Map(("id" -> lid), ("directed" -> 1), ("relation" -> l._1), ("orig" -> l._2.toString), ("targ" -> l._3.toString))
      else if (l._4)
        Map(("id" -> lid), ("directed" -> 1), ("relation" -> l._1), ("orig" -> l._2.toString), ("starg" -> l._3.toString))
      else if (l._5)
        Map(("id" -> lid), ("directed" -> 1), ("relation" -> l._1), ("sorig" -> l._2.toString), ("targ" -> l._3.toString))
    }
    generate(json)
  }
}