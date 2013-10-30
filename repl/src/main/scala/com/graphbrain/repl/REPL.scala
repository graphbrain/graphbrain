package com.graphbrain.repl

import com.graphbrain.eco.{Prog, Parser}
import com.graphbrain.eco.nodes.DummyFun

object REPL {
  var prog = new Prog()

  def main(args: Array[String]) = {
    println("Welcome to the Eco REPL.")

    print("> ")
    for(ln <- io.Source.stdin.getLines()) {
      val p = new Parser(ln)

      p.expr match {
        case d: DummyFun => {
          d.name match {
            case "load" => {
              /*
              val progFile = d.params(0).stringValue(null, null)
              println("loading " + progFile)
              prog = Prog.load(progFile)
              */
            }
            case "list" => println(prog)
            case "exit" => {
              println("bye.")
              sys.exit(0)
            }
            case "parse" => {
              /*
              val sentence = d.params(0).stringValue(null, null)
              prog.wv(Words.fromString(sentence))
              */
            }
          }
        }
      }

      print("> ")
    }
  }
}
