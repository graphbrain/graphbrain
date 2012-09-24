package com.graphbrain.webapp


object JSONGen {
  def json(x: Any): String = {
    x match {
      case s: String => "\"" + s.replaceAllLiterally("\\", "\\\\").replaceAllLiterally("\"", "\\\"") + "\""
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
}
