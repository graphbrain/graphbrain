package com.graphbrain.gbdb


class SearchInterface(store: VertexStore) {

  def query(text: String) = {
    val id = ID.sanitize(text)
    var maxId = 0

    while (store.exists("" + (maxId + 1) + "/" + id))
      maxId += 1

    for (i <- 1 to maxId) yield "" + i + "/" + id
  }
}
