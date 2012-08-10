package com.graphbrain.hgdb


class SearchInterface(store: VertexStoreInterface) {

  def query(text: String) = {
    val id = ID.sanitize(text)
    var maxId = 0

    while (store.exists("" + maxId + "/" + id)) {
      maxId += 1
    }

    for (i <- 1 to maxId) yield "" + i + "/" + id
  }
}