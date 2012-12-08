package com.graphbrain.hgdb


object HyperEdges2Edges {
  def apply(hyperEdges: Set[Edge], rootId: String) = hyperEdges.map(hyper2edge(_, rootId)).filter(_ != null)

  def hyper2edge(edge: Edge, rootId: String) = {
    if (edge.participantIds.length > 2) {
      if (edge.edgeType == "rtype/1/instance_of~owned_by") {
        if (edge.participantIds(0) == rootId) {
          Edge("rtype/1/has", List(edge.participantIds(2), rootId))
        }
        else if (edge.participantIds(2) == rootId) {
          Edge("rtype/1/has", List(rootId, edge.participantIds(0)))
        }
        else {
          null
        }
      }
      else {
        null
      }
    }
    else {
      edge
    }
  }
}