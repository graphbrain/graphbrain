package com.graphbrain.webapp


object Seg2 {
  def unapply(path: String): Option[List[String]] = path.split("/").toList match {
    case "" :: head :: rest => Some(List(head, rest.reduceLeft(_ + "/" + _)))
    case head :: rest => Some(List(head, rest.reduceLeft(_ + "/" + _)))
    case all => Some(all)
  }
}