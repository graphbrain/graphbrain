package com.graphbrain.webapp

import com.graphbrain.hgdb.VertexStore
import com.graphbrain.hgdb.Vertex
import com.graphbrain.hgdb.Edge
import com.graphbrain.hgdb.TextNode
import com.graphbrain.hgdb.ImageNode
import com.graphbrain.hgdb.UserNode
import com.graphbrain.hgdb.Brain
import scala.collection.mutable.{Map => MMap}
import scala.collection.mutable.{Set => MSet}
import com.codahale.jerkson.Json._
import scala.math


class PoorLong (val l:Long) {
    /** a "mathematical" modulo that always maps to 0...n-1 (for positive n) */
    def mod(n:Long) = {
        val m = l % n
        if (m<0) m+n else m
    }
}

class PoorDouble (val d:Double) {
    /**the "decimals" of this double */
    def fraction:Double = d - d.floor

    /** a "pseudo-mod", 7.5 mod 5 would map to 2.5, -0.5 mod 5 would map to 4.5 */
    def mod(n:Long) = new PoorLong(d.floor.longValue).mod(n).toDouble + fraction
}

object Conversions {
    implicit def longToPoorLong(l:Long) = new PoorLong(l)
    implicit def doubleToPoorDouble(d:Double) = new PoorDouble(d)
} 

import Conversions._ //put this import before your hsv converter code


class GraphInterface (val rootId: String, val store: VertexStore, val user: UserNode) {
  val neighbors = store.neighbors(rootId)
  val edgeIds = store.rootNeighborEdges(rootId, neighbors)
  val snodes = supernodes
  val links = visualLinks
  val nodesJSON = nodes2json
  val snodesJSON = snodes2json
  val linksJSON = links2json
  val brainsJSON = if (user == null) "" else brains2json

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
      case un: UserNode => Map(("type" -> "text"), ("text" -> un.name), ("parent" -> parentId))
      case br: Brain => Map(("type" -> "text"), ("text" -> br.name), ("parent" -> parentId))
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

  /** Converts an hsv triplet in the range ([0,360],[0,1],[0,1])
   *  to an rgb triplet in the range ([0,1],[0,1],[0,1])
   */
  private def hsvToRgb(h:Double, s:Double, v:Double): (Double, Double, Double) = {
    val c = s * v
    val h1 = h / 60.0
    val x  = c * (1.0 - ((h1 mod 2) - 1.0).abs) 
    val (r,g,b) = if (h1 < 1.0) (c, x, 0.0)
                else if (h1 < 2.0) (x, c, 0.0)
                else if (h1 < 3.0) (0.0, c, x)
                else if (h1 < 4.0) (0.0, x, c)
                else if (h1 < 5.0) (x, 0.0, c)
                else (c, 0.0, x) 
    val m = v - c
    (r + m, g + m, b + m)
  }

  private def linkColor(label: String) = {
    val code = math.abs(label.hashCode) % 99
    var hue: Double = code.toDouble / 99.0
    val hue1 = 0
    val hue2 = 360
    val deltaHue = hue2 - hue1
    hue *= deltaHue
    hue += hue1
    // println("label: " + label + "; hue: " + hue)
    val rgb = hsvToRgb(hue, 0.75, 1.0)
    val r: Int = (rgb._1 * 255).toInt
    val g: Int = (rgb._2 * 255).toInt
    val b: Int = (rgb._3 * 255).toInt
    "rgb(" + r + "," + g + "," + b + ")"
  }

  /** Generates JSON string from links */
  private def links2json = {
    var lid = 0
    val json = for (l <- links) yield {
      lid += 1
      val color = linkColor(l._1)
      if (l._4 && l._5)
        Map(("id" -> lid), ("directed" -> 1), ("relation" -> l._1), ("orig" -> l._2.toString), ("targ" -> l._3.toString), ("color" -> color))
      else if (l._4)
        Map(("id" -> lid), ("directed" -> 1), ("relation" -> l._1), ("orig" -> l._2.toString), ("starg" -> l._3.toString), ("color" -> color))
      else if (l._5)
        Map(("id" -> lid), ("directed" -> 1), ("relation" -> l._1), ("sorig" -> l._2.toString), ("targ" -> l._3.toString), ("color" -> color))
    }
    generate(json)
  }

  private def brains2json = {
    val brains = for (brainId <- user.brains if Server.store.exists(brainId)) yield Server.store.getBrain(brainId)
    val brainMaps = for (brain <- brains)
      yield Map(("id" -> brain.id), ("name" -> brain.name), ("access" -> brain.access))
    generate(brainMaps)
  }
}