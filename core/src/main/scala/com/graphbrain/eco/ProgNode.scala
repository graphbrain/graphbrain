package com.graphbrain.eco

abstract class ProgNode(val prog: Prog) {
  def eval(): AnyVal
}