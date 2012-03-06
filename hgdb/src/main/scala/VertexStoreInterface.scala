package com.graphbrain.hgdb

abstract trait VertexStoreInterface {
  def get(id: String): Vertex
  def put(vertex: Vertex): Vertex
  def update(vertex: Vertex): Vertex
  def remove(vertex: Vertex): Vertex

  def getEdge(id: String): Edge = {
  	get(id) match {
  		case x: Edge => x
  		case _ => null
  	}
  }

  def getEdgeType(id: String): EdgeType = {
  	get(id) match {
  		case x: EdgeType => x
  		case _ => null
  	}
  }

  def getTextNode(id: String): TextNode = {
  	get(id) match {
  		case x: TextNode => x
  		case _ => null
  	}
  }

  def getURLNode(id: String): URLNode = {
  	get(id) match {
  		case x: URLNode => x
  		case _ => null
  	}
  }

  def getSourceNode(id: String): SourceNode = {
  	get(id) match {
  		case x: SourceNode => x
  		case _ => null
  	}
  }

  def getImageNode(id: String): ImageNode = {
  	get(id) match {
  		case x: ImageNode => x
  		case _ => null
  	}
  }
}