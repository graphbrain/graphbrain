package com.graphbrain.eco

import com.graphbrain.eco.TokenType.TokenType
import scala.collection.mutable.ListBuffer

class Lexer(val input: String) {
  private var pos: Int = 0
  private var c: Char = input(pos)
  private val EOF: Char = (-1).toChar

  def tokens = {
    val toks = ListBuffer[Token]()

    var tok = nextToken()
    while (tok != null) {
      toks.append(tok)
      tok = nextToken()
    }

    toks.toList
  }

  private def nextToken(): Token = {
    if (c == EOF) {
      null
    }
    else {
      while (c.isWhitespace) consume()

      val nt = predict match {
        case TokenType.Symbol => tokSymbol
        case TokenType.Number => tokNumber
        case TokenType.String => tokString
        case TokenType.LPar => tokLPar
        case TokenType.RPar => tokRPar
      }

      nt
    }
  }

  private def consume() = {
    pos += 1
    if (pos >= input.length)
      c = EOF
    else
      c = input.charAt(pos)
  }

  private def onLastChar = pos >= input.length - 1

  private def predict: TokenType = {
    if (c.isDigit)
      TokenType.Number
    else
      c match {
        case '"' => TokenType.String
        case '(' => TokenType.LPar
        case ')' => TokenType.RPar
        case '-' => {
          if (onLastChar) {
            TokenType.Symbol
          }
          else {
            val next = input(pos + 1)
            if (next.isDigit)
              TokenType.Number
            else
              next match {
                case '.' => TokenType.Number
                case _ => TokenType.Symbol
              }
          }
        }
        case _ => TokenType.Symbol
    }
  }

  private def tokSymbol: Token = {
    val sb = new StringBuilder(25)
    var done = false

    while (!done) {
      sb.append(c)
      consume()

      if ((!c.isLetter)
        && (!c.isDigit)
        && (c != '-')
        && (c != '_')) {

        done = true
      }
    }

    new Token(sb.toString(), TokenType.Symbol)
  }

  private def tokNumber: Token = {
    val sb = new StringBuilder(25)
    var done = false
    var dotSeen = false

    while (!done) {
      if (c == '.') dotSeen = true

      sb.append(c)
      consume()

      if ((!c.isDigit)
        && ((c != '.') || ((c == '.') && dotSeen))) {

        done = true
      }
    }

    new Token(sb.toString(), TokenType.Number)
  }

  private def tokString: Token = {
    consume()
    if (onLastChar) return new Token("", TokenType.String)

    val sb = new StringBuilder(25)
    var done = false

    while (!done) {
      sb.append(c)
      consume()

      if (c == '"') {

        done = true
      }
    }

    consume()

    new Token(sb.toString(), TokenType.String)
  }

  private def tokLPar: Token = {
    consume()
    new Token("(", TokenType.LPar)
  }

  private def tokRPar: Token = {
    consume()
    new Token(")", TokenType.RPar)
  }
}

object Lexer {
  def main(args: Array[String]) = {
    val l = new Lexer("1 + 1")
    println(l.tokens)
  }
}