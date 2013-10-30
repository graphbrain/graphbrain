package com.graphbrain.eco

import com.graphbrain.eco.nodes.ProgNode

class ProgNodeMap[A]
  extends scala.collection.mutable.HashMap[ProgNode, A] with Cloneable {

  override def clone(): ProgNodeMap[A] = super.clone() match {
    case pnm: ProgNodeMap[A] => pnm
    case _ => null
  }

  protected override def elemEquals(key1: ProgNode, key2: ProgNode): Boolean = key1 eq key2
}