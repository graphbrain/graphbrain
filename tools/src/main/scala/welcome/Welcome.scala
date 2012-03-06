package com.graphbrain.tools.welcome

import com.graphbrain.hgdb.VertexStore
import com.graphbrain.hgdb.TextNode
import com.graphbrain.hgdb.EdgeType
import com.graphbrain.hgdb.SourceNode


class Welcome(val store: VertexStore) {
  var nodes = 0
  var edges = 0

  def addTextNode(id: String, text: String) = {
    val node = TextNode(id, text)
    store.update(node)
    addBinaryRel("source/welcome", "source", id)
    nodes += 1
  }

  def addBinaryRel(node1: String, rel:String, node2: String) = {
    store.addrel(rel, Array(node1, node2))
    edges += 1
  }

  def createGraph() = {
    store.update(SourceNode("source/welcome"))

    // root node
    addTextNode("welcome/graphbrain", "GraphBrain")

    // GraphBrain is ...
    addTextNode("welcome/knowledge_base", "Knowledge Base")
    addTextNode("welcome/search_engine", "Search Engine")
    addTextNode("welcome/artificial_brain", "Artificial Brain")
    addBinaryRel("welcome/graphbrain", "is", "welcome/knowledge_base")
    addBinaryRel("welcome/graphbrain", "is", "welcome/search_engine")
    addBinaryRel("welcome/graphbrain", "is", "welcome/artificial_brain")
  }
}

object Welcome {
  def apply(args: Array[String]) = {
    val store: VertexStore = new VertexStore("gb")
    val welcome = new Welcome(store)
    welcome.createGraph()
    println("Nodes created: " + welcome.nodes)
    println("Edges create: " + welcome.edges)
  }
}