package com.graphbrain.tools


object Tools { 
  def main(args: Array[String]) : Unit = {
    if (args.size < 1) {
      println("Error: too few parameters.")
    }
    else {
      val toolArgs = args.slice(1, args.size)
      args(0) match {
        case "showvertex" => ShowVertex(toolArgs)
        case "neighbors" => Neighbors(toolArgs)
        case "edgetypes" => EdgeTypes(toolArgs)
        case _ => println("Error: unkown tool.")
      }
    }
  }
}