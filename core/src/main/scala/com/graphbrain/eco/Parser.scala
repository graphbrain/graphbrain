package com.graphbrain.eco

import com.graphbrain.eco.TokenType.TokenType

class Parser(val input: String) {
  val tokens = (new Lexer(input)).tokens

  def parse() = {
    parseRule()
  }

  private def parseRule() = {
    var consqPos = -1
    var pos = 0

    while (consqPos < 0) {
      if (tokens(consqPos).ttype == TokenType.Consequence)
        consqPos = pos
      pos += 1
    }

    parseExpr(3, consqPos)
    parseExpr(consqPos + 1, tokens.length)
  }

  private def parseExpr(start: Int, end: Int) = {
    var maxPrecedence = 0
    var pivot = -1
    var paramDepth = 0

    for (i <- start until end) {
      val tok = tokens(i)

      if (tok.ttype == TokenType.LParamPar) {
        paramDepth += 1
      }
      else if (tok.ttype == TokenType.RParamPar) {
        paramDepth -= 1
      }
      else if (paramDepth == 0) {
        if (tok.precedence > maxPrecedence) {
          maxPrecedence = tok.precedence
          if (tok.ttype == TokenType.LPar) pivot = i - 1 else pivot = 1
        }
      }
    }

    if (pivot < 0) {

    }
    else {

    }
  }
}

object Parser {
  def main(args: Array[String]) = {
    val p = new Parser("1 + 1")
    p.parse()
  }
}