package com.graphbrain.webapp

import com.graphbrain.hgdb.VertexStore
import com.graphbrain.hgdb.Edge
import scala.collection.mutable.{Map => MMap}
import scala.collection.mutable.{Set => MSet}


object GraphInterface {

  /** Generates relationship Map
    * 
    * Generates a Map where all nodeIds that participate in the same
    * kind of relationship are grouped.
    */
  // NOTE: We only consider the first two participants for now
  private def relmap(edges: Set[Edge], rootId: String) = {
    val rm = MMap[(String, Int, String), Set[String]]()
    for (edge <- edges) {
      val participants = edge.participantIds.slice(0, 2)
      for (i <- 0 until participants.size) {
        val key = (edge.etype, i, participants(i))
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
    * Deletes key and it's value and removes node in key from all other node lists.
    */
  private def delrel(relmap: MMap[(String, Int, String), Set[String]], key: (String, Int, String)) {
    val nodes = relmap(key)
    relmap -= key
    for (k <- relmap) {
      relmap(k._1) = k._2.filter(_ != key._3)
      if (relmap(k._1).size == 0) relmap -= k._1
    }
  }

  /** Generates supernodes Map */
  def supernodes(edges: Set[Edge], rootId: String) = {
    var snodes = MSet[Map[String, Any]]()
    val rd = relmap(edges, rootId)
    var superId = 0

    // create root supernode
    var superkey = "sn" + superId
    snodes += Map[String, Any](("id" -> superkey), ("key" -> ("", -1, "")), ("nodes" -> Set(rootId)))

    superId += 1

    // create supernodes
    var key = maxrel(rd)
    while (key != null) {
      val nodes = rd(key)
      superkey = "sn" + superId
      snodes += Map[String, Any](("id" -> superkey), ("key" -> key), ("nodes" -> nodes))
      delrel(rd, key)
      key = maxrel(rd)
      superId += 1
    }
    snodes
  }

  /** Determines if an Edge is redundant in realtion to supernode links */
  private def redundantEdge(snodes: MSet[Map[String, Any]], edge: Edge): Boolean = {
    for (sn <- snodes) {
      val key: (String, Int, String) = sn("key").asInstanceOf[(String, Int, String)]
      val otherParticipants = edge.participantIds.toSet - key._3
      if ((edge.etype == key._1) &&
        (edge.participantIds(key._2) == key._3) && 
        otherParticipants.subsetOf(sn("nodes").asInstanceOf[Set[String]]))
        return true
    }
    false
  }

  /** Generates list o links to be displayed */
  def visualLinks(snodes: MSet[Map[String, Any]], edges: Set[Edge]) = {
    val nodeLinks = (for (e <- edges if (!redundantEdge(snodes, e))) yield {
      val pids = e.participantIds
      (e.etype, pids(0), pids(1))
    }).toSet

    val snodeLinks = (for (sn <- snodes) yield {
      val superkey = sn("id")
      val key: (String, Int, String) = sn("key").asInstanceOf[(String, Int, String)]
      if (key._2 == 0)
        (key._1, key._3, superkey)
      else
        (key._1, superkey, key._3)
    }).toSet.filter(_._1 != "")
      
    nodeLinks ++ snodeLinks
  }
}