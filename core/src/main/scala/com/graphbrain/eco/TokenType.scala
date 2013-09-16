package com.graphbrain.eco

object TokenType extends Enumeration {
  type TokenType = Value
	val Unknown,
      Symbol,
      Number,
      String,
      Consequence,
      LPar,
      RPar,
      LParamPar,
      RParamPar,
      LSPar,
      RSPar,
      Quote,
      Colon,
      SColon,
      Plus,
      Minus,
      Mul,
      Div = Value
}
