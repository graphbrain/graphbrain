package com.graphbrain.eco

import com.graphbrain.eco.nodes._

class Parser(val input: String) {
  val tokens = new Lexer(input).tokens
  val prog = new Prog(parse())

  private def parse(): ProgNode = {
    parseRule()
  }

  private def parseRule(): ProgNode = {
    var consqPos = -1
    var pos = 0

    while (consqPos < 0) {
      if (tokens(pos).ttype == TokenType.Consequence)
        consqPos = pos
      pos += 1
    }

    val p1 = parseExpr(3, consqPos)
    val p2 = parseExpr(consqPos + 1, tokens.length)
    new NlpRule(Array(p1, p2))
  }

  private def parseExpr(start: Int, end: Int): ProgNode = {

    // expression only has one element
    if ((end - start) == 1) {
      val tok = tokens(start)

      tok.ttype match {
        case TokenType.Number => return new NumberNode(tok.text.toDouble)
        case TokenType.Symbol => {
          tok.text match {
            case "true" => return new BoolNode(true)
            case "false" => return new BoolNode(false)
          }
        }
      }
    }

    // expression is surrounded by parenthesis
    if ((tokens(start).ttype == TokenType.LPar) &&
      (tokens(end - 1).ttype == TokenType.RPar))
      return parseExpr(start + 1, end - 1)

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
          // what if first param?
          if (tok.ttype == TokenType.LPar) pivot = i - 1 else pivot = i
        }
      }
    }

    if (pivot < 0) {
      null
    }
    else {
      tokens(pivot).ttype match {
        case TokenType.Plus => {
          val p1 = parseExpr(start, pivot)
          val p2 = parseExpr(pivot + 1, end)
          new SumFun(Array(p1, p2))
        }
      }
    }
  }
}

object Parser {
  def main(args: Array[String]) = {
    val p = new Parser("nlp test: 1 + 1 -> 2 + 2")
    println(p.prog)
  }
}