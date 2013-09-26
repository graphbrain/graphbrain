package com.graphbrain.eco

import com.graphbrain.eco.nodes._

class Parser(val input: String) {
  val tokens = new Lexer(input).tokens
  val prog = new Prog(parse(0))

  private def parse(pos: Int): ProgNode = {
    tokens(pos).ttype match {
      case TokenType.LPar => parseList(pos)
      case TokenType.String => new StringNode(tokens(pos).text, pos)
      case TokenType.Symbol => new StringVar(tokens(pos).text, "", pos)
    }
  }

  private def matchOpeningPar(pos: Int) =
    tokens(pos).ttype == TokenType.LPar

  private def matchClosingPar(pos: Int) =
    tokens(pos).ttype == TokenType.RPar

  private def parseList(pos: Int): ProgNode = {
    tokens(pos + 1).ttype match {
      case TokenType.Symbol => parseFun(pos + 1)
      case _ => null // error
    }
  }

  private def parseFun(pos: Int): ProgNode = {
    tokens(pos).text match {
      case "nlp" => parseNlp(pos + 1)
      case "?" => parsePattern(pos + 1)
      case _ => null // error
    }
  }

  private def parseNlp(pos: Int): ProgNode = {
    val p1 = parseConds(pos)
    val p2 = parseConds(p1.lastTokenPos + 1)

    if (matchClosingPar(p2.lastTokenPos + 1))
      new NlpRule(Array(p1, p2), p2.lastTokenPos + 1)
    else
      null // error
  }

  private def parseConds(pos: Int): ProgNode = {
    if (!matchOpeningPar(pos))
      return null // error

    val params = parseCondsList(pos + 1).toArray
    val lastParamsTokenPos = if (params == Nil) pos else params.last.lastTokenPos

    if (!matchClosingPar(lastParamsTokenPos + 1))
      return null // error

    new CondsFun(params, lastParamsTokenPos + 1)
  }

  private def parseCondsList(pos: Int): List[ProgNode] = {
    if (matchClosingPar(pos)) {
      Nil
    }
    else {
      val cond = parseFun(pos)
      cond :: parseCondsList(cond.lastTokenPos + 1)
    }
  }

  private def parsePatternParamsList(pos: Int): List[ProgNode] = {
    if (matchClosingPar(pos)) {
      Nil
    }
    else {
      val param = tokens(pos).ttype match {
        case TokenType.String => new StringNode(tokens(pos).text, pos)
        case TokenType.Symbol => new StringVar(tokens(pos).text, "", pos)
        case _ => null // error
      }
      param :: parsePatternParamsList(param.lastTokenPos + 1)
    }
  }

  private def parsePattern(pos: Int): ProgNode = {
    val params = parsePatternParamsList(pos).toArray

    val lastParamsTokenPos = if (params == Nil) pos else params.last.lastTokenPos

    if (!matchClosingPar(lastParamsTokenPos + 1))
      return null // error

    new PatFun(params, lastParamsTokenPos + 1)
  }
}

object Parser {
  def main(args: Array[String]) = {
    val p = new Parser("nlp test: 1 + 1 -> 2 + 2")
    println(p.prog)
  }
}