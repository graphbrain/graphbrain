package com.graphbrain.eco

import scala.collection.mutable
import com.graphbrain.eco.NodeType.NodeType
import com.graphbrain.eco.nodes.ProgNode

class Context(val parent: Contexts,
  val varTypes: mutable.Map[String, NodeType] = mutable.Map[String, NodeType](),
  val stringVars: mutable.Map[String, String] = mutable.Map[String, String](),
  val numberVars: mutable.Map[String, Double] = mutable.Map[String, Double](),
  val booleanVars: mutable.Map[String, Boolean] = mutable.Map[String, Boolean](),
  val wordsVars: mutable.Map[String, Words] = mutable.Map[String, Words](),
  val vertexVars: mutable.Map[String, String] = mutable.Map[String, String](),
  private val retStringMap: ProgNodeMap[String] = new ProgNodeMap[String],
  private val retNumberMap: ProgNodeMap[Double] = new ProgNodeMap[Double],
  private val retBooleanMap: ProgNodeMap[Boolean] = new ProgNodeMap[Boolean],
  private val retWordsMap: ProgNodeMap[Words] = new ProgNodeMap[Words],
  private val retVertexMap: ProgNodeMap[String] = new ProgNodeMap[String],
  private val retVerticesMap: ProgNodeMap[Set[String]] = new ProgNodeMap[Set[String]],
  private var topRet: ProgNode = null,
  var subContexts: List[Context] = List[Context]())

  extends Cloneable {

  override def clone() = new Context(parent,
      varTypes.clone(),
      stringVars.clone(),
      numberVars.clone(),
      booleanVars.clone(),
      wordsVars.clone(),
      vertexVars.clone(),
      retStringMap.clone(),
      retNumberMap.clone(),
      retBooleanMap.clone(),
      retWordsMap.clone(),
      retVertexMap.clone(),
      retVerticesMap.clone(),
      topRet,
      subContexts)

  def merge(ctxt: Context) = {
    ctxt.varTypes.foreach(kv => varTypes(kv._1) = kv._2)

    ctxt.stringVars.foreach(kv => stringVars(kv._1) = kv._2)
    ctxt.numberVars.foreach(kv => numberVars(kv._1) = kv._2)
    ctxt.booleanVars.foreach(kv => booleanVars(kv._1) = kv._2)
    ctxt.wordsVars.foreach(kv => wordsVars(kv._1) = kv._2)
    ctxt.vertexVars.foreach(kv => vertexVars(kv._1) = kv._2)

    ctxt.retStringMap.foreach(kv => retStringMap(kv._1) = kv._2)
    ctxt.retNumberMap.foreach(kv => retNumberMap(kv._1) = kv._2)
    ctxt.retBooleanMap.foreach(kv => retBooleanMap(kv._1) = kv._2)
    ctxt.retWordsMap.foreach(kv => retWordsMap(kv._1) = kv._2)
    ctxt.retVertexMap.foreach(kv => retVertexMap(kv._1) = kv._2)
    ctxt.retVerticesMap.foreach(kv => retVerticesMap(kv._1) = kv._2)

    topRet = ctxt.topRet

    subContexts = ctxt.subContexts
  }

  def getRetString(p: ProgNode) = retStringMap(p)
  def getRetNumber(p: ProgNode) = retNumberMap(p)
  def getRetBoolean(p: ProgNode) = retBooleanMap(p)
  def getRetWords(p: ProgNode) = retWordsMap(p)
  def getRetVertex(p: ProgNode) = retVertexMap(p)
  def getRetVertices(p: ProgNode) = retVerticesMap(p)

  def getTopRetString = retStringMap(topRet)
  def getTopRetNumber = retNumberMap(topRet)
  def getTopRetBoolean = retBooleanMap(topRet)
  def getTopRetWords = retWordsMap(topRet)
  def getTopRetVertex = retVertexMap(topRet)
  def getTopRetVertices = retVerticesMap(topRet)

  def setRetString(p: ProgNode, value: String) = {
    retStringMap(p) = value
    topRet = p
  }
  def setRetNumber(p: ProgNode, value: Double) = {
    retNumberMap(p) = value
    topRet = p
  }
  def setRetBoolean(p: ProgNode, value: Boolean) = {
    retBooleanMap(p) = value
    topRet = p
  }
  def setRetWords(p: ProgNode, value: Words) = {
    retWordsMap(p) = value
    topRet = p
  }
  def setRetVertex(p: ProgNode, value: String) = {
    retVertexMap(p) = value
    topRet = p
  }
  def setRetVertices(p: ProgNode, value: Set[String]) = {
    retVerticesMap(p) = value
    topRet = p
  }

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
  def setVertex(variable: String, value: String) = {
    setType(variable, NodeType.Vertex)
    vertexVars(variable) = value
  }

  def getString(variable: String) = stringVars(variable)
  def getNumber(variable: String) = numberVars(variable)
  def getBoolean(variable: String) = booleanVars(variable)
  def getWords(variable: String) = wordsVars(variable)
  def getVertex(variable: String) = vertexVars(variable)

  def addSubContext(subCtxt: Context) =
    subContexts :+= subCtxt

  def printCallStack(indent: Int = 0): Unit = {
    (0 to indent).foreach(x => print("...."))
    println(parent.rule.name + " [" + parent.sentence + "]")
    for (sctxts <- subContexts)
      sctxts.printCallStack(indent + 1)
  }

  override def toString = {
    val sb = new mutable.StringBuilder()
    sb.append("context:")
    for (v <- stringVars) sb.append(v._1 + " = " + v._2)
    for (v <- numberVars) sb.append(v._1 + " = " + v._2)
    for (v <- booleanVars) sb.append(v._1 + " = " + v._2)
    for (v <- wordsVars) sb.append(v._1 + " = " + v._2)
    for (v <- vertexVars) sb.append(v._1 + " = " + v._2)
    sb.toString()
  }
}