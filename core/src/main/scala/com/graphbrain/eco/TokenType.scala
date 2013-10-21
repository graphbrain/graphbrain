package com.graphbrain.eco

object TokenType extends Enumeration {
  type TokenType = Value
	val Unknown,
      Symbol,
      POS,
      Number,
      String,
      LPar,
      RPar = Value
}
