package com.graphbrain.db

case class SimpleEdge(edgeType: String,
                id1: String,
                id2: String,
                parent: Edge) {

  def this(edge: Edge) =
    this(edge.ids(0), edge.ids(1), edge.ids(2), edge)
}