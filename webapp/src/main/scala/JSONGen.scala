package com.graphbrain.webapp


object JSONGen {
  def json(x: Any): String = {
    x match {
      case s: String => "\"" + s + "\""
      case i: Int => i.toString
      case d: Double => d.toString
      case m: Map[_, _] => {
        if (m.size == 0)
          "{}"
        else
          "{" + (for (e <- m) yield json(e._1) + ":" + json(e._2)).reduceLeft(_ + "," + _) + "}"
      }
      case Nil => "[]"
      case l: Iterable[_] => {
        if (l.size == 0)
          "[]"
        else
          "[" + (for (e <- l) yield json(e)).reduceLeft(_ + "," + _) + "]"
      }
      case a: Array[_] => json(a.toList)
      case _ => ""
    }
  }

  /*
  def main(args: Array[String]) {
    println(json("Telmo Menezes"))
    println(json(12))
    println(json(12345.67))
    println(json(List("Telmo", "Menezes", 13)))
    println(json(Map(("name" -> "Telmo Menezes"), ("age" -> 13))))
    println(json(List()))
    println(json(Map()))
    println(json(Map(("name" -> "Telmo Menezes"), ("numbers" -> List(1, 2, "3")))))
    println(json(Array("Telmo", "Menesses")))
    println(json(Array()))
    println(json(Set("Telmo", "Menezes", 13)))
  }*/
}
