package com.graphbrain.eco

import com.graphbrain.eco.TokenType.TokenType

class Token(val text: String, val ttype: TokenType) {
  override def toString = text + " <" + ttype.toString + ">"

  override def equals(obj: Any) =
    obj.asInstanceOf[Token].text == text &&
    obj.asInstanceOf[Token].ttype == ttype
}

object Token {
  def apply(test: String, ttype: TokenType) = new Token(test, ttype)
}