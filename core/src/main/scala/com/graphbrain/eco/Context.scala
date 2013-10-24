package com.graphbrain.eco

import scala.collection.mutable
import com.graphbrain.eco.NodeType.NodeType

class Context {
  val varTypes = mutable.Map[String, NodeType]()

  val stringVars = mutable.Map[String, String]()
  val numberVars = mutable.Map[String, Double]()
  val booleanVars = mutable.Map[String, Boolean]()
  val wordsVars = mutable.Map[String, Words]()
  val posVars = mutable.Map[String, String]()
  val vertexVars = mutable.Map[String, String]()

  private def setType(variable: String, value: NodeType) = varTypes(variable) = value

  def getType(variable: String) =
    if (varTypes.contains(variable))
      varTypes(variable)
    else
      NodeType.Unknown

  def setString(variable: String, value: String) = {
    setType(variable, NodeType.String)
    stringVars(variable) = value
  }
  def setNumber(variable: String, value: Double) = {
    setType(variable, NodeType.Number)
    numberVars(variable) = value
  }
  def setBoolean(variable: String, value: Boolean) = {
    setType(variable, NodeType.Boolean)
    booleanVars(variable) = value
  }
  def setWords(variable: String, value: Words) = {
    setType(variable, NodeType.Words)
    wordsVars(variable) = value
  }
  def setPOS(variable: String, value: String) = {
    setType(variable, NodeType.POS)
    posVars(variable) = value
  }
  def setVertex(variable: String, value: String) = {
    setType(variable, NodeType.Vertex)
    vertexVars(variable) = value
  }

  def getString(variable: String) = stringVars(variable)
  def getNumber(variable: String) = numberVars(variable)
  def getBoolean(variable: String) = booleanVars(variable)
  def getWords(variable: String) = wordsVars(variable)
  def getPOS(variable: String) = posVars(variable)
  def getVertex(variable: String) = vertexVars(variable)

  def print() = {
    println("context:")
    for (v <- stringVars) println(v._1 + " = " + v._2)
    for (v <- numberVars) println(v._1 + " = " + v._2)
    for (v <- booleanVars) println(v._1 + " = " + v._2)
    for (v <- wordsVars) println(v._1 + " = " + v._2)
    for (v <- posVars) println(v._1 + " = " + v._2)
    for (v <- vertexVars) println(v._1 + " = " + v._2)
  }
}