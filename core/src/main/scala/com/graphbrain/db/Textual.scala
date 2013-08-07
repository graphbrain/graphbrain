package com.graphbrain.db


abstract class Textual extends Vertex {
  val summary: String

  def updateSummary(): Textual = this

  //override def description: String = toString + " " + generateSummary
}

object Textual {
  def generateSummary(id: String, graph: Graph): String = {
    val edges = graph.neighborEdges(id)

    var bestEdge: Edge = null
    var maxScore = -1
    for (e <- edges) {
      val score = scoreForSummary(id, e)
      if (score > maxScore) {
        bestEdge = e
        maxScore = score
      }
    }

    if (bestEdge == null)
      ""
    else
      "(" + graph.get(bestEdge.participantIds(1)) + ")"
  }

  // Score edge in terms of its ability to generate a summary for this TextNode
  private def scoreForSummary(id: String, edge: Edge): Int = {
    // Only consider edges where this TextNode is the first participant
    if (edge.participantIds(0) != id)
      return -1

    edge.edgeType match {
      case "rtype/1/as_in" => 100
      case "rtype/1/is_a" => 10
      case _ => -1
    }
  }
}