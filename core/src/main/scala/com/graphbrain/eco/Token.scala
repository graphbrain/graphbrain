package com.graphbrain.eco

import com.graphbrain.eco.TokenType.TokenType

class Token(val text: String, val ttype: TokenType) {
  override def toString = text + " <" + ttype.toString + ">"

  override def equals(obj: Any) =
    obj.asInstanceOf[Token].text == text &&
    obj.asInstanceOf[Token].ttype == ttype

  def precedence = ttype match {
    case TokenType.LPar => 3
    case TokenType.Mul => 2
    case TokenType.Div => 2
    case TokenType.Plus => 1
    case TokenType.Minus => 1
    case _ => 0
  }
}

object Token {
  def apply(test: String, ttype: TokenType) = new Token(test, ttype)
}