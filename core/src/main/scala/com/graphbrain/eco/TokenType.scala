package com.graphbrain.eco

object TokenType extends Enumeration {
  type TokenType = Value
	val Unknown,
      Symbol,
      Number,
      String,
      Vertex,
      LPar,
      RPar = Value
}
