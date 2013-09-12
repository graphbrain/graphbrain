package com.graphbrain.eco.nodes

import com.graphbrain.eco.Prog

abstract class FunNode(prog: Prog, val name: String, val params: Array[ProgNode]) extends ProgNode(prog) {

}