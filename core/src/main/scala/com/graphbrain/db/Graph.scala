package com.graphbrain.db

import java.util.Date
import com.typesafe.scalalogging.slf4j.Logging

class Graph() extends Logging {
  val back = new LevelDbBackend()

  def get(id: String): Vertex = back.get(id, VertexType.getType(id))

  def put(vertex: Vertex): Vertex = {
    val v = if (vertex.ts < 0)
      vertex.setTs(new Date().getTime)
    else
      vertex

    back.put(v)

    v match  {
      case e: Edge => onPutEdge(e)
      case _ =>
    }

    v
  }

  def update(vertex: Vertex): Vertex = put(vertex)

  def exists(id: String): Boolean = get(id) != null

  def exists(v: Vertex): Boolean = exists(v.id)

  def remove(vertex: Vertex) = {
    back.remove(vertex)

    vertex match {
      case e: Edge => onRemoveEdge(e)
      case _ =>
    }
  }

  def edges(pattern: Edge) = back.edges(pattern)

  def edges(center: Vertex) = back.edges(center)

  def edges(centerId: String): Set[Edge] = edges(get(centerId))

  def edges(pattern: Array[String]) = back.edges(Edge.fromParticipants(pattern))

  def nodesFromEdgeSet(edgeSet: Set[Edge]) = {
    var nset = Set[String]()

    for (e <- edgeSet)
      for (pid <- e.ids)
        nset = nset + pid

    nset
  }

  def neighbors(centerId: String) = {
    logger.debug(s"neighbors: $centerId")

    val nedges = edges(centerId)
    val nodes = nodesFromEdgeSet(nedges)
    nodes + centerId
  }

  protected def incDegree(vertex: Vertex): Vertex = update(vertex.setDegree(vertex.degree + 1))

  protected def incDegree(id: String): Vertex = incDegree(get(id))

  protected def onPutEdge(edge: Edge) =
    for (id <- edge.ids) {
      if (!exists(id)) {
        put(Vertex.createFromId(id))
      }
      incDegree(id)
    }

  protected def decDegree(vertex: Vertex): Vertex = update(vertex.setDegree(vertex.degree - 1))

  protected def decDegree(id: String): Vertex = {
    val v = get(id)
    if (v != null) decDegree(v) else null
  }

  protected def onRemoveEdge(edge: Edge) =
    for (id <- edge.ids)
      decDegree(id)
}