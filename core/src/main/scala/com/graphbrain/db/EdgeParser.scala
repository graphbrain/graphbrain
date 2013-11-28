package com.graphbrain.db

class EdgeParser(input: String) {
  private var pos: Int = 0
  private var c: Char = input(pos)
  private val EOF: Char = (-1).toChar

  private def consume() = {
    pos += 1
    if (pos >= input.length)
      c = EOF
    else
      c = input.charAt(pos)
  }

  def parse: Vertex = {
    if (c != '(') {
      Vertex.fromId(nextToken)
    }
    else {
      consume()
      var params = List[Vertex]()
      while (c != ')') {
        params = parse :: params
      }
      new Edge(params.reverse.toArray)
    }
  }

  private def nextToken = {
    val start = pos

    while ((c != ' ') && (c != ')') && (c != EOF))
      consume()

    input.substring(start, pos)
  }
}

object EdgeParser {
  def apply(id: String) =
    new EdgeParser(id).parse
}