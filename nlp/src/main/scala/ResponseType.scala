package com.graphbrain.nlp

import com.graphbrain.hgdb.Vertex




abstract class ResponseType 
  
case class HardcodedResponse(responseStrings: List[String]) extends ResponseType {
  override def toString: String = responseStrings.toString;
}
case class GraphResponse(hypergraphList: List[(List[Vertex], Vertex)]) extends ResponseType {
  override def toString: String = hypergraphList.toString;
}
case class QuestionFactResponse(responseStrings: List[String], hypergraphList: List[(List[Vertex], Vertex)]) extends ResponseType {
  override def toString = responseStrings.toString + " " + hypergraphList.toString;
}
case class SearchResponse(searchStrings: List[String]) extends ResponseType  {
  override def toString = searchStrings.toString;
}

