package com.graphbrain.eco

import com.graphbrain.eco.nodes._

class Parser(val input: String) {
  val tokens = new Lexer(input).tokens
  val prog = new Prog(parse(0))

  private def parse(pos: Int): ProgNode = {
    tokens(pos).ttype match {
      case TokenType.LPar => parseList(pos)
      case TokenType.String => new StringNode(tokens(pos).text, pos)
      case TokenType.Symbol => parseSymbol(pos)
      case TokenType.Number => new NumberNode(tokens(pos).text.toDouble, pos)
    }
  }

  private def parseSymbol(pos: Int): ProgNode = tokens(pos).text match {
    case "true" => new BoolNode(true, pos)
    case "false" => new BoolNode(false, pos)
    case _ => new TreeVar(tokens(pos).text, "", pos)
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
      case "tree" => parseTree(pos + 1)
      case "?" => parsePattern(pos + 1)
      case "is" => parseIs(pos + 1)
      case "is-leaf" => parseIsLeaf(pos + 1)
      case "+" => parseSum(pos + 1)
      case _ => null // error
    }
  }

  private def parseRuleName(pos: Int): ProgNode = {
    tokens(pos).ttype match {
      case TokenType.Symbol => new RuleNameNode(tokens(pos).text, pos)
      case _ => null // error
    }
  }

  private def parseNlp(pos: Int): ProgNode = {
    val p1 = parseRuleName(pos)
    val p2 = parseConds(p1.lastTokenPos + 1)
    val p3 = parseConds(p2.lastTokenPos + 1)

    if (matchClosingPar(p3.lastTokenPos + 1))
      new NlpRule(Array(p1, p2, p3), p3.lastTokenPos + 1)
    else
      null // error
  }

  private def parseTree(pos: Int): ProgNode = {
    val p1 = parseRuleName(pos)
    val p2 = parseConds(p1.lastTokenPos + 1)
    val p3 = parseConds(p2.lastTokenPos + 1)

    if (matchClosingPar(p3.lastTokenPos + 1))
      new TreeRule(Array(p1, p2, p3), p3.lastTokenPos + 1)
    else
      null // error
  }

  private def parseConds(pos: Int): ProgNode = {
    if (!matchOpeningPar(pos))
      return null // error

    val params = parseCondsList(pos + 1).toArray
    val lastParamsTokenPos = if (params.size == 0) pos else params.last.lastTokenPos

    if (!matchClosingPar(lastParamsTokenPos + 1))
      return null // error

    new CondsFun(params, lastParamsTokenPos + 1)
  }

  private def parseCondsList(pos: Int): List[ProgNode] = {
    if (matchClosingPar(pos)) {
      Nil
    }
    else {
      val cond = parse(pos)
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
        case TokenType.Symbol => new TreeVar(tokens(pos).text, "", pos)
        case TokenType.POS => new POSNode(tokens(pos).text, pos)
        case TokenType.LPar => parsePattern(pos + 1)
        case _ => null // error
      }
      param :: parsePatternParamsList(param.lastTokenPos + 1)
    }
  }

  private def parsePattern(pos: Int): ProgNode = {
    val params = parsePatternParamsList(pos).toArray

    val lastParamsTokenPos = if (params.size == 0) pos else params.last.lastTokenPos

    if (!matchClosingPar(lastParamsTokenPos + 1))
      return null // error

    new PatFun(params, lastParamsTokenPos + 1)
  }

  private def parseSum(pos: Int): ProgNode = {
    val p1 = parse(pos)
    val p2 = parse(p1.lastTokenPos + 1)

    if (matchClosingPar(p2.lastTokenPos + 1))
      new SumFun(Array(p1, p2), p2.lastTokenPos + 1)
    else
      null // error
  }

  private def parseIs(pos: Int): ProgNode = {
    val p1 = parse(pos)
    val p2 = parse(p1.lastTokenPos + 1)

    if (matchClosingPar(p2.lastTokenPos + 1))
      new TreeFun(TreeFun.Is, Array(p1, p2), p2.lastTokenPos + 1)
    else
      null // error
  }

  private def parseIsLeaf(pos: Int): ProgNode = {
    val p1 = parse(pos)

    if (matchClosingPar(p1.lastTokenPos + 1))
      new TreeFun(TreeFun.IsLeaf, Array(p1), p1.lastTokenPos + 1)
    else
      null // error
  }
}

object Parser {
  def main(args: Array[String]) = {
    val p = new Parser("(nlp test ((? x \"is\" y)) (true))")
    println(p.prog)
  }
}