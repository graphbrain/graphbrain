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

    val p1 = parseConds(3, consqPos)
    val p2 = parseConds(consqPos + 1, tokens.length)
    new NlpRule(Array(p1, p2))
  }

  private def parseConds(start: Int, end: Int): ProgNode = {
    val count = (for (i <- start until end) yield
      if (tokens(i).ttype == TokenType.SColon) 1 else 0).reduceLeft(_ + _) + 1

    var cstart = start
    val params = new Array[ProgNode](count)
    var cond = 0

    for (pos <- start until end) {
      if (tokens(pos).ttype == TokenType.SColon) {
        params(cond) = parseCond(cstart, pos)
        cond += 1
        cstart = pos + 1
      }
    }

    params(cond) = parseCond(cstart, end)

    if (count == 1)
      params(0)
    else
      new CondsFun(params)
  }

  private def parseCond(start: Int, end: Int): ProgNode = {
    if (tokens(start).ttype == TokenType.Quote)
      parsePattern(start + 1, end - 1)
    else
      parseExpr(start, end)
  }

  private def parsePattern(start: Int, end: Int): ProgNode = {
    val params = new Array[ProgNode](end - start)

    for (pos <- start until end) {
      params(pos - start) = tokens(pos).ttype match {
        case TokenType.String => new StringNode(tokens(pos).text)
        case TokenType.Symbol => new StringVar(tokens(pos).text, "")
      }
    }

    new PatFun(params)
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
    var initialPar = true

    for (i <- start until end) {
      val tok = tokens(i)

      if (tok.ttype != TokenType.LPar)
        initialPar = false

      if (tok.ttype == TokenType.LParamPar) {
        paramDepth += 1
      }
      else if (tok.ttype == TokenType.RParamPar) {
        paramDepth -= 1
      }
      else if (paramDepth == 0) {
        if (tok.ttype == TokenType.LPar) {
          if (!initialPar) {
            pivot = i - 1
            maxPrecedence = tok.precedence
          }
        }
        else if (tok.ttype == TokenType.RPar) {
          pivot = i + 1
          maxPrecedence = tok.precedence
        }
        else if (tok.precedence > maxPrecedence) {
          pivot = i
          maxPrecedence = tok.precedence
          // what if first param?
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
        case TokenType.Minus => {
          val p1 = parseExpr(start, pivot)
          val p2 = parseExpr(pivot + 1, end)
          new SubFun(Array(p1, p2))
        }
        case TokenType.Mul => {
          val p1 = parseExpr(start, pivot)
          val p2 = parseExpr(pivot + 1, end)
          new MulFun(Array(p1, p2))
        }
        case TokenType.Div => {
          val p1 = parseExpr(start, pivot)
          val p2 = parseExpr(pivot + 1, end)
          new DivFun(Array(p1, p2))
        }
        case _ => {
          // error
          null
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