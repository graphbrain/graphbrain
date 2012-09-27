package com.graphbrain.hgdb


//To store the rule body
case class RuleNode(id: String="", rule: String="") extends Vertex {
  
  override def clone(newid: String) = RuleNode(newid, rule)

  override def toString: String = rule
}