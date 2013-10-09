package com.graphbrain.eco

import scala.collection.mutable

class Context {
  val stringVars = mutable.Map[String, String]()
  val numberVars = mutable.Map[String, Double]()
  val booleanVars = mutable.Map[String, Boolean]()

  def setString(variable: String, value: String) = stringVars(variable) = value
  def setNumber(variable: String, value: Double) = numberVars(variable) = value
  def setBoolean(variable: String, value: Boolean) = booleanVars(variable) = value

  def getString(variable: String) = stringVars(variable)
  def getNumber(variable: String) = numberVars(variable)
  def getBoolean(variable: String) = booleanVars(variable)

  def print() = {
    println("context:")
    for (v <- stringVars) (println(v._1 + " = " + v._2))
  }
}