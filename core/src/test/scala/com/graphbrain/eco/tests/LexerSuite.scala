package com.graphbrain.eco.tests

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import com.graphbrain.eco._

@RunWith(classOf[JUnitRunner])
class LexerSuite extends FunSuite {
  test("1 + 1") {
    val lexer = new Lexer("1 + 1")
    val toks = lexer.tokens
    assert(toks === List(
      Token("1", TokenType.Number),
      Token("+", TokenType.Plus),
      Token("1", TokenType.Number)))
  }

  test("x + 1") {
    val lexer = new Lexer("x + 1")
    val toks = lexer.tokens
    assert(toks === List(
      Token("x", TokenType.Symbol),
      Token("+", TokenType.Plus),
      Token("1", TokenType.Number)))
  }

  test("is-noun(x)") {
    val lexer = new Lexer("is-noun(x)")
    val toks = lexer.tokens
    assert(toks === List(
      Token("is-noun", TokenType.Symbol),
      Token("(", TokenType.LParamPar),
      Token("x", TokenType.Symbol),
      Token(")", TokenType.RPar)))
  }

  test("print(\"eco\")") {
    val lexer = new Lexer("print(\"eco\")")
    val toks = lexer.tokens
    assert(toks === List(
      Token("print", TokenType.Symbol),
      Token("(", TokenType.LParamPar),
      Token("eco", TokenType.String),
      Token(")", TokenType.RPar)))
  }

  test("extra spaces, tabs and newlines") {
    val lexer = new Lexer("1  \n\t  +  \t\t \t\n  1")
    val toks = lexer.tokens
    assert(toks === List(
      Token("1", TokenType.Number),
      Token("+", TokenType.Plus),
      Token("1", TokenType.Number)))
  }

  test("1.0 + -1.0") {
    val lexer = new Lexer("1.0 + -1.0")
    val toks = lexer.tokens
    assert(toks === List(
      Token("1.0", TokenType.Number),
      Token("+", TokenType.Plus),
      Token("-1.0", TokenType.Number)))
  }

  test("1.0 - -1.0") {
    val lexer = new Lexer("1.0 - -1.0")
    val toks = lexer.tokens
    assert(toks === List(
      Token("1.0", TokenType.Number),
      Token("-", TokenType.Minus),
      Token("-1.0", TokenType.Number)))
  }
}