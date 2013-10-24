package com.graphbrain.eco

object NodeType extends Enumeration {
  type NodeType = Value
	val Unknown,
      Boolean,
      Number,
      String,
      Words,
      POS,
      Vertex,
      RuleName = Value
}
