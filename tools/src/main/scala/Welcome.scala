package com.graphbrain.tools

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
    addTextNode("welcome/rotating", "Rotating")
    addTextNode("welcome/zooming", "Zooming")
    addTextNode("welcome/navigating", "Navigating")
    addTextNode("welcome/searching", "Searching")
    addTextNode("welcome/telmo_menezes", "Telmo Menezes")
    addTextNode("welcome/chih-chun_chen", "Chih-Chun Chen")
    addTextNode("welcome/user_generated_content", "User Generated Content")
    addTextNode("welcome/natural_language_interface", "Natural Language Interface")
    addTextNode("welcome/private_areas", "Private Areas")
    addBinaryRel("welcome/graphbrain", "is", "welcome/knowledge_base")
    addBinaryRel("welcome/graphbrain", "is", "welcome/search_engine")
    addBinaryRel("welcome/graphbrain", "is", "welcome/artificial_brain")
    addBinaryRel("welcome/graphbrain", "allows", "welcome/rotating")
    addBinaryRel("welcome/graphbrain", "allows", "welcome/zooming")
    addBinaryRel("welcome/graphbrain", "allows", "welcome/navigating")
    addBinaryRel("welcome/graphbrain", "allows", "welcome/searching")
    addBinaryRel("welcome/graphbrain", "founder", "welcome/telmo_menezes")
    addBinaryRel("welcome/graphbrain", "founder", "welcome/chih-chun_chen")
    addBinaryRel("welcome/graphbrain", "will_have", "welcome/user_generated_content")
    addBinaryRel("welcome/graphbrain", "will_have", "welcome/natural_language_interface")
    addBinaryRel("welcome/graphbrain", "will_have", "welcome/private_areas")
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