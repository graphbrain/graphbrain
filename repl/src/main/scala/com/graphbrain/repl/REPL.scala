package com.graphbrain.repl

import com.graphbrain.eco.Parser
import com.graphbrain.eco.nodes.DummyFun

object REPL {
  def main(args: Array[String]) = {
    println("Welcome to the Eco REPL.")

    for(ln <- io.Source.stdin.getLines()) {
      val p = new Parser(ln)

      p.prog.root match {
        case d: DummyFun => {
          d.name match {
            case "load" => println("loading " + d.params(0).stringValue(null, null))
            case "exit" => {
              println("bye.")
              sys.exit(0)
            }
          }
        }
      }
    }
  }
}
