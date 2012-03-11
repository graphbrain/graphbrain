package com.graphbrain.hgdb

abstract trait VertexStoreInterface {
  def get(id: String): Vertex
  def put(vertex: Vertex): Vertex
  def update(vertex: Vertex): Vertex
  def remove(vertex: Vertex): Vertex

  def getEdge(id: String): Edge = {
  	get(id) match {
  		case x: Edge => x
  		case v: Vertex => throw WrongVertexType("on vertex: " + id + " (expected 'edg', found : '" + v.vtype + "')")
  	}
  }

  def getExtraEdges(id: String): ExtraEdges = {
    get(id) match {
      case x: ExtraEdges => x
      case v: Vertex => throw WrongVertexType("on vertex: " + id + " (expected 'ext', found : '" + v.vtype + "')")
    }
  }

  def getEdgeType(id: String): EdgeType = {
  	get(id) match {
  		case x: EdgeType => x
  		case v: Vertex => throw WrongVertexType("on vertex: " + id + " (expected 'edgt', found : '" + v.vtype + "')")
  	}
  }

  def getTextNode(id: String): TextNode = {
  	get(id) match {
  		case x: TextNode => x
  		case v: Vertex => throw WrongVertexType("on vertex: " + id + " (expected 'txt', found : '" + v.vtype + "')")
  	}
  }

  def getURLNode(id: String): URLNode = {
  	get(id) match {
  		case x: URLNode => x
  		case v: Vertex => throw WrongVertexType("on vertex: " + id + " (expected 'url', found : '" + v.vtype + "')")
  	}
  }

  def getSourceNode(id: String): SourceNode = {
  	get(id) match {
  		case x: SourceNode => x
  		case v: Vertex => throw WrongVertexType("on vertex: " + id + " (expected 'src', found : '" + v.vtype + "')")
  	}
  }

  def getImageNode(id: String): ImageNode = {
  	get(id) match {
  		case x: ImageNode => x
  		case v: Vertex => throw WrongVertexType("on vertex: " + id + " (expected 'img', found : '" + v.vtype + "')")
  	}
  }
}