package com.graphbrain.tools

import com.graphbrain.hgdb.VertexStore


object ShowVertex {
  def apply(args: Array[String]) = {
    if (args.size == 0) {
      println("Error: too few parameters for showvertex. You need to specify the vertex id.")
    }
    else {
      val store: VertexStore = new VertexStore("gb")
      println(store.get(args(0)))
    }
  }
}