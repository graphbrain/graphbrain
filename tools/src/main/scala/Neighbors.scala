package com.graphbrain.tools

import com.graphbrain.hgdb.VertexStore


object Neighbors {
  def apply(args: Array[String]) = {
    if (args.size == 0) {
      println("Error: too few parameters for neighbors. You need to specify the vertex id.")
    }
    else {
      val store: VertexStore = new VertexStore("gb")
      val neighbors = store.neighbors(args(0))
      for (n <- neighbors) println(n)
    }
  }
}