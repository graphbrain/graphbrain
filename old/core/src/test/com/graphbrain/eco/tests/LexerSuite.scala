package com.graphbrain.eco.tests

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import com.graphbrain.eco._

@RunWith(classOf[JUnitRunner])
class LexerSuite extends FunSuite {
  test("(+ 1 1)") {
    val lexer = new Lexer("(+ 1 1)")
    val toks = lexer.tokens
    assert(toks === List(
      new Token("(", TokenType.LPar),
      new Token("+", TokenType.Symbol),
      new Token("1", TokenType.Number),
      new Token("1", TokenType.Number),
      new Token(")", TokenType.RPar)))
  }

  test("(+ x 1)") {
    val lexer = new Lexer("(+ x 1)")
    val toks = lexer.tokens
    assert(toks === List(
      new Token("(", TokenType.LPar),
      new Token("+", TokenType.Symbol),
      new Token("x", TokenType.Symbol),
      new Token("1", TokenType.Number),
      new Token(")", TokenType.RPar)))
  }

  test("(is-noun x)") {
    val lexer = new Lexer("(is-noun x)")
    val toks = lexer.tokens
    assert(toks === List(
      new Token("(", TokenType.LPar),
      new Token("is-noun", TokenType.Symbol),
      new Token("x", TokenType.Symbol),
      new Token(")", TokenType.RPar)))
  }

  test("(print \"eco\")") {
    val lexer = new Lexer("(print \"eco\")")
    val toks = lexer.tokens
    assert(toks === List(
      new Token("(", TokenType.LPar),
      new Token("print", TokenType.Symbol),
      new Token("eco", TokenType.String),
      new Token(")", TokenType.RPar)))
  }

  test("extra spaces, tabs and newlines") {
    val lexer = new Lexer("(+  \n\t  1  \t\t \t\n  1)")
    val toks = lexer.tokens
    assert(toks === List(
      new Token("(", TokenType.LPar),
      new Token("+", TokenType.Symbol),
      new Token("1", TokenType.Number),
      new Token("1", TokenType.Number),
      new Token(")", TokenType.RPar)))
  }

  test("(+ 1.0 -1.0)") {
    val lexer = new Lexer("(+ 1.0 -1.0)")
    val toks = lexer.tokens
    assert(toks === List(
      new Token("(", TokenType.LPar),
      new Token("+", TokenType.Symbol),
      new Token("1.0", TokenType.Number),
      new Token("-1.0", TokenType.Number),
      new Token(")", TokenType.RPar)))
  }

  test("(- 1.0 -1.0)") {
    val lexer = new Lexer("(- 1.0 -1.0)")
    val toks = lexer.tokens
    assert(toks === List(
      new Token("(", TokenType.LPar),
      new Token("-", TokenType.Symbol),
      new Token("1.0", TokenType.Number),
      new Token("-1.0", TokenType.Number),
      new Token(")", TokenType.RPar)))
  }
}