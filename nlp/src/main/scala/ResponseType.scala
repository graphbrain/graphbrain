package com.graphbrain.nlp

import com.graphbrain.hgdb.Vertex




abstract class ResponseType 
  

case class HardcodedResponse(responseString: List[String]) extends ResponseType
case class GraphResponse(hypergraphList: List[(List[Vertex], Vertex)]) extends ResponseType
case class QuestionFactResponse(responseString: List[String], hypergraphList: List[(List[Vertex], Vertex)]) extends ResponseType
case class SearchResponse(searchString: List[String]) extends ResponseType
