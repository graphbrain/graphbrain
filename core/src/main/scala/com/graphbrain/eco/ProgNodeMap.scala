package com.graphbrain.eco

import com.graphbrain.eco.nodes.ProgNode
import scala.collection.mutable

class ProgNodeMap[A]
  extends mutable.HashMap[ProgNode, A] with Cloneable {

  override def clone(): ProgNodeMap[A] = {
    val pnm = new ProgNodeMap[A]
    foreach {kv => pnm(kv._1) = kv._2}
    pnm
  }

  protected override def elemEquals(key1: ProgNode, key2: ProgNode): Boolean = key1 eq key2
}