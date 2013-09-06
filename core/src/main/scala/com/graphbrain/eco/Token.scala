package com.graphbrain.eco

import com.graphbrain.eco.TokenType.TokenType

class Token(val text: String, val ttype: TokenType) {
  override def toString = text + " <" + ttype.toString + ">"
}
