package com.graphbrain.tools

import welcome.Welcome
import showvertex.ShowVertex


object Tools { 
  def main(args: Array[String]) : Unit = {
    if (args.size < 1) {
      println("Error: too few parameters.")
    }
    else {
      val toolArgs = args.slice(1, args.size)
      args(0) match {
        case "welcome" => Welcome(toolArgs)
        case "showvertex" => ShowVertex(toolArgs)
        case _ => println("Error: unkown tool.")
      }
    }
  }
}