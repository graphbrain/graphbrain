package com.graphbrain.db


class SearchInterface(graph: Graph) {

  def query(text: String) = {
    val id = ID.sanitize(text)
    var maxId = 0

    while (graph.exists("" + (maxId + 1) + "/" + id))
      maxId += 1

    for (i <- 1 to maxId) yield "" + i + "/" + id
  }
}
