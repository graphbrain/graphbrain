package com.graphbrain.eco

import scala.collection.mutable

class Context {
  val stringVars = mutable.Map[String, String]()
  val numberVars = mutable.Map[String, Double]()
  val booleanVars = mutable.Map[String, Boolean]()
  val treeVars = mutable.Map[String, PTree]()
  val posVars = mutable.Map[String, String]()
  val vertexVars = mutable.Map[String, String]()

  def setString(variable: String, value: String) = stringVars(variable) = value
  def setNumber(variable: String, value: Double) = numberVars(variable) = value
  def setBoolean(variable: String, value: Boolean) = booleanVars(variable) = value
  def setTree(variable: String, value: PTree) = treeVars(variable) = value
  def setPOS(variable: String, value: String) = posVars(variable) = value
  def setVertex(variable: String, value: String) = vertexVars(variable) = value

  def getString(variable: String) = stringVars(variable)
  def getNumber(variable: String) = numberVars(variable)
  def getBoolean(variable: String) = booleanVars(variable)
  def getTree(variable: String) = treeVars(variable)
  def getPOS(variable: String) = posVars(variable)
  def getVertex(variable: String) = vertexVars(variable)

  def print() = {
    println("context:")
    for (v <- treeVars) println(v._1 + " = " + v._2)
  }
}