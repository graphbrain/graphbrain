package com.graphbrain.eco

class Tests(testData: String) {
  val lines = testData.split("\\r?\\n").map(_.trim).filter(_ != "")

  val tests = for (l <- lines) yield
    l.split("\\|").map(_.trim)
}
