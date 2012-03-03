package com.graphbrain.tools.welcome

import com.graphbrain.hgdb.VertexStore


class Welcome(val store: VertexStore) {

}

object Welcome {
  def apply(args: Array[String]) {
  	val store: VertexStore = new VertexStore("gb")
  	val welcome = new Welcome(store)
  	//addTextNode("welcome/graphbrain", "GraphBrain")
  }
}