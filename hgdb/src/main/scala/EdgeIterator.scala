package com.graphbrain.hgdb


class EdgeIterator(val edgeSetId: String, val store: VertexStore) extends Iterator[String] {
  var extraPos = 0
  var iterSet: Iterator[String] = null

  def next: String = {
    if (iterSet != null) {
      if (iterSet.hasNext) {
        return iterSet.next
      }

      extraPos += 1
    }

    if (extraPos == 0) {
      val edgeSet = store.getEdgeSet(edgeSetId)
      iterSet = edgeSet.edges.iterator
      iterSet.next
    }
    else {
      val edgeSet = store.getEdgeSet(ID.extraId(edgeSetId, extraPos))
      iterSet = edgeSet.edges.iterator
      iterSet.next
    }
  }

  def hasNext: Boolean = {
    var extra = extraPos

    if (iterSet != null) {
      if (iterSet.hasNext) {
        return true
      }

      extra += 1
    }

    if (extra == 0) {
      if (store.exists(edgeSetId)) {
        val edgeSet = store.getEdgeSet(edgeSetId)
        edgeSet.edges.size > 0
      }
      else {
        false
      }
    }
    else {
      if (store.exists(ID.extraId(edgeSetId, extra))) {
        val edgeSet = store.getEdgeSet(ID.extraId(edgeSetId, extra))
        edgeSet.edges.size > 0
      }
      else {
        false
      }
    }
  }
}
