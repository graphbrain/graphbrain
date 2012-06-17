package com.graphbrain.hgdb


trait UIOps extends NodeManagement {

  def removeVertexAndEdgesUI(vertex: Vertex) = {
    vertex match {
      case u: UserNode => // don't delete usernodes
      case _ => removeVertexAndEdges(vertex)
    }
  }
}