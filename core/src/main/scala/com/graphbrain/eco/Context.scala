package com.graphbrain.eco

import scala.collection.mutable

class Context {
  val stringVars = mutable.Map[String, String]()
  val numberVars = mutable.Map[String, Double]()
  val booleanVars = mutable.Map[String, Boolean]()
  val phraseVars = mutable.Map[String, Phrase]()

  def setString(variable: String, value: String) = stringVars(variable) = value
  def setNumber(variable: String, value: Double) = numberVars(variable) = value
  def setBoolean(variable: String, value: Boolean) = booleanVars(variable) = value
  def setPhrase(variable: String, value: Phrase) = phraseVars(variable) = value

  def getString(variable: String) = stringVars(variable)
  def getNumber(variable: String) = numberVars(variable)
  def getBoolean(variable: String) = booleanVars(variable)
  def getPhrase(variable: String) = phraseVars(variable)

  def print() = {
    println("context:")
    for (v <- phraseVars) println(v._1 + " = " + v._2)
  }
}