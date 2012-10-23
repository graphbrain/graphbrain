package com.graphbrain.webapp


object Seg2 {
  def unapply(path: String): Option[List[String]] = path.split("/").toList match {
    case "" :: head :: rest => {
      if (rest.length == 0)
        Some(List(head))
      else if (rest.length == 1)
        Some(List(head, rest(0)))
      else
        Some(List(head, rest.reduceLeft(_ + "/" + _)))
    }
    case head :: rest => {
      if (rest.length == 0)
        Some(List(head))
      else if (rest.length == 1)
        Some(List(head, rest(0)))
      else
        Some(List(head, rest.reduceLeft(_ + "/" + _)))
    }
    case all => Some(all)
  }
}