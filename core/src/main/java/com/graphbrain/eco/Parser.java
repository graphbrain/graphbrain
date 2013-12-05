package com.graphbrain.eco;

import com.graphbrain.eco.nodes.*;
import com.graphbrain.eco.NodeType;
import com.graphbrain.db.Vertex;

public class Parser {

    public Parser(String input) {

    }

  val varTypes = mutable.Map[String, NodeType]()
  val tokens = new Lexer(input).tokens
  val expr = parse(0)

  private def parse(pos: Int): ProgNode = {
    tokens(pos).ttype match {
      case TokenType.LPar => parseList(pos)
      case TokenType.String => new StringNode(tokens(pos).text, pos)
      case TokenType.Symbol => parseSymbol(pos)
      case TokenType.Number => new NumberNode(tokens(pos).text.toDouble, pos)
      case TokenType.Vertex => new VertexNode(Vertex.fromId(tokens(pos).text), pos)
    }
  }

  private def parseSymbol(pos: Int): ProgNode = tokens(pos).text match {
    case "true" => new BoolNode(true, pos)
    case "false" => new BoolNode(false, pos)
    case _ => parseVar(pos)
  }

  private def parseVar(pos: Int): ProgNode = {
    val vn = VarNode(tokens(pos).text, pos)
    if (varTypes.contains(vn.name)) vn.varType = varTypes(vn.name)
    vn
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
      case "wv" => parseWV(pos + 1)
      case "ww" => parseWW(pos + 1)
      case "let" => parseLet(pos + 1)
      case "!" => parseNotFun(pos + 1)
      case "build" => parseBuildVert(pos + 1)
      case ":wv" => parseWVRecursion(pos + 1)
      case ":ww" => parseWWRecursion(pos + 1)
      case "rel-vert" => parseRelVert(pos + 1)
      case "txt-vert" => parseTxtVert(pos + 1)
      case "is-pos" => parseNlpFun(NlpFunType.IS_POS, pos + 1)
      case "is-pos-pre" => parseNlpFun(NlpFunType.IS_POSPRE, pos + 1)
      case "are-pos" => parseNlpFun(NlpFunType.ARE_POS, pos + 1)
      case "are-pos-pre" => parseNlpFun(NlpFunType.ARE_POSPRE, pos + 1)
      case "contains-pos" => parseNlpFun(NlpFunType.CONTAINS_POS, pos + 1)
      case "contains-pos-pre" => parseNlpFun(NlpFunType.CONTAINS_POSPRE, pos + 1)
      case "is-lemma" => parseNlpFun(NlpFunType.IS_LEMMA, pos + 1)
      case "max-depth" => parseMaxDepth(pos + 1)
      case "filter-min" => parseFilter(FilterFun.FilterMin, pos + 1)
      case "filter-max" => parseFilter(FilterFun.FilterMax, pos + 1)
      case "len" => parseLenFun(pos + 1)
      case "pos" => parsePosFun(pos + 1)
      case "+" => parseSum(pos + 1)
      case "ends-with" => parseEndsWith(pos + 1)
      case s: String => parseDummy(s, pos + 1)
    }
  }

  private def parseWV(pos: Int): ProgNode = {
    val p1 = parsePattern(pos)
    val p2 = parseConds(p1.lastTokenPos + 1)
    val p3 = parse(p2.lastTokenPos + 1)

    if (matchClosingPar(p3.lastTokenPos + 1))
      new WVRule(Array(p1, p2, p3), p3.lastTokenPos + 1)
    else
      null // error
  }

  private def parseWW(pos: Int): ProgNode = {
    val p1 = parsePattern(pos)
    val p2 = parseConds(p1.lastTokenPos + 1)
    val p3 = parse(p2.lastTokenPos + 1)

    if (matchClosingPar(p3.lastTokenPos + 1))
      new WWRule(Array(p1, p2, p3), p3.lastTokenPos + 1)
    else
      null // error
  }

  private def parseElems(pos: Int): (Array[ProgNode], Int) = {
    if (!matchOpeningPar(pos))
      return null // error

    val params = parseElemList(pos + 1).toArray
    val lastParamsTokenPos = if (params.size == 0) pos else params.last.lastTokenPos

    if (!matchClosingPar(lastParamsTokenPos + 1))
      return null // error

    (params, lastParamsTokenPos + 1)
  }

  private def parseElemList(pos: Int): List[ProgNode] = {
    if (matchClosingPar(pos)) {
      Nil
    }
    else {
      val elem = parse(pos)
      elem :: parseElemList(elem.lastTokenPos + 1)
    }
  }

  private def parseConds(pos: Int): ProgNode = {
    val e = parseElems(pos)
    new CondsFun(e._1, e._2)
  }

  private def parseParamsList(pos: Int): List[ProgNode] = {
    if (matchClosingPar(pos)) {
      Nil
    }
    else {
      val param = tokens(pos).ttype match {
        case TokenType.String => new StringNode(tokens(pos).text, pos)
        case TokenType.Symbol => parseVar(pos)
        case TokenType.LPar => parsePattern(pos + 1)
        case _ => null // error
      }
      param :: parseParamsList(param.lastTokenPos + 1)
    }
  }

  private def parsePattern(pos: Int): ProgNode = {
    if (!matchOpeningPar(pos))
      return null // error

    val params = parseParamsList(pos + 1).toArray

    // set var types
    for (p <- params) p match {
      case v: VarNode => {
        v.varType = NodeType.Words
        varTypes(v.name) = NodeType.Words
      }
      case _ =>
    }

    val lastParamsTokenPos = if (params.size == 0) pos + 1 else params.last.lastTokenPos

    if (!matchClosingPar(lastParamsTokenPos + 1))
      return null // error

    new PatFun(params, lastParamsTokenPos + 1)
  }

  private def parseLet(pos: Int): ProgNode = {
    val p1 = parse(pos)
    val p2 = parse(p1.lastTokenPos + 1)

    // set var type
    p1 match {
      case v: VarNode => {
        v.varType = p2.ntype
        varTypes(v.name) = p2.ntype
      }
      case _ =>
    }

    if (matchClosingPar(p2.lastTokenPos + 1))
      new LetFun(Array(p1, p2), p2.lastTokenPos + 1)
    else
      null // error
  }

  private def parseBuildVert(pos: Int): ProgNode = {
    var lastPos = pos
    var paramList = List[ProgNode]()

    while (!matchClosingPar(lastPos)) {
      val p = parse(lastPos)
      lastPos = p.lastTokenPos + 1
      paramList ::= p
    }

    val params = paramList.reverse.toArray
    new VertexFun(VertexFun.BuildVert, params, lastPos)
  }

  private def parseRelVert(pos: Int): ProgNode = {
    val p1 = parse(pos)

    if (matchClosingPar(p1.lastTokenPos + 1))
      new VertexFun(VertexFun.RelVert, Array(p1), p1.lastTokenPos + 1)
    else
      null // error
  }

  private def parseTxtVert(pos: Int): ProgNode = {
    val p1 = parse(pos)

    if (matchClosingPar(p1.lastTokenPos + 1))
      new VertexFun(VertexFun.TxtVert, Array(p1), p1.lastTokenPos + 1)
    else
      null // error
  }

  private def parseDummy(name: String, pos: Int): ProgNode = {
    var lastPos = pos
    var paramList = List[ProgNode]()

    while (!matchClosingPar(lastPos)) {
      val p = parse(lastPos)
      lastPos = p.lastTokenPos + 1
      paramList ::= p
    }

    val params = paramList.reverse.toArray
    new DummyFun(name, params, lastPos)
  }

  private def parseNlpFun(ftype: NlpFunType, pos: Int): ProgNode = {
    val params = parseParamsList(pos).toArray

    val lastParamsTokenPos = if (params.size == 0) pos else params.last.lastTokenPos

    if (matchClosingPar(lastParamsTokenPos + 1))
      new NlpFun(ftype, params, lastParamsTokenPos + 1)
    else
      null // error
  }

  private def parseWVRecursion(pos: Int): ProgNode = {
    val p1 = parse(pos)

    if (matchClosingPar(p1.lastTokenPos + 1))
      new WVRecursion(Array(p1), p1.lastTokenPos + 1)
    else
      null // error
  }

  private def parseWWRecursion(pos: Int): ProgNode = {
    val p1 = parse(pos)

    if (matchClosingPar(p1.lastTokenPos + 1))
      new WWRecursion(Array(p1), p1.lastTokenPos + 1)
    else
      null // error
  }

  private def parseNotFun(pos: Int): ProgNode = {
    val p1 = parse(pos)

    if (matchClosingPar(p1.lastTokenPos + 1))
      new NotFun(Array(p1), p1.lastTokenPos + 1)
    else
      null // error
  }

  private def parseMaxDepth(pos: Int): ProgNode = {
    val p1 = parse(pos)

    if (matchClosingPar(p1.lastTokenPos + 1))
      new MaxDepthFun(Array(p1), p1.lastTokenPos + 1)
    else
      null // error
  }

  private def parseFilter(fun: FilterFun.FilterFun, pos: Int): ProgNode = {
    val p1 = parse(pos)

    if (matchClosingPar(p1.lastTokenPos + 1))
      new FilterFun(fun, Array(p1), p1.lastTokenPos + 1)
    else
      null // error
  }

  private def parseLenFun(pos: Int): ProgNode = {
    val p1 = parse(pos)

    if (matchClosingPar(p1.lastTokenPos + 1))
      new LenFun(Array(p1), p1.lastTokenPos + 1)
    else
      null // error
  }

  private def parsePosFun(pos: Int): ProgNode = {
    val p1 = parse(pos)

    if (matchClosingPar(p1.lastTokenPos + 1))
      new PosFun(Array(p1), p1.lastTokenPos + 1)
    else
      null // error
  }

  private def parseSum(pos: Int): ProgNode = {
    val params = parseParamsList(pos).toArray
    val lastParamsTokenPos = if (params.size == 0) pos else params.last.lastTokenPos

    if (!matchClosingPar(lastParamsTokenPos + 1))
      return null // error

    new SumFun(params, lastParamsTokenPos + 1)
  }

  private def parseEndsWith(pos: Int): ProgNode = {
    val p1 = parse(pos)
    val p2 = parse(p1.lastTokenPos + 1)

    if (matchClosingPar(p2.lastTokenPos + 1))
      new WordsFun(WordsFun.EndsWith, Array(p1, p2), p2.lastTokenPos + 1)
    else
      null // error
  }
}

object Parser {
  def main(args: Array[String]) = {
    val p = new Parser("(nlp test ((? x \"is\" y)) (true))")
    println(p.expr)
  }
}