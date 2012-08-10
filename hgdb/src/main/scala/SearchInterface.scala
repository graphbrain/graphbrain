package com.graphbrain.hgdb


class SearchInterface(store: VertexStoreInterface) {

  def query(text: String) = {
    val id = ID.sanitize(text)
    var maxId = 0

    println("===> sanitized: " + id)

    while (store.exists("" + (maxId + 1) + "/" + id)) {
      maxId += 1
      println("" + maxId + "/" + id)
    }

    for (i <- 1 to maxId) yield "" + i + "/" + id
  }
}