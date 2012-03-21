package com.graphbrain.tools

import com.graphbrain.hgdb.VertexStore
import com.graphbrain.searchengine.RiakSearchInterface


object Search {
  def apply(args: Array[String]) = {
    if (args.size == 0) {
      println("Error: too few parameters for neighbors. You need to specify the search terms.")
    }
    else {
      val si = RiakSearchInterface("gbsearch")
      val results = si.query(args.reduceLeft(_ + " " + _))
      println("number of matches: " + results.numResults)
      for (id <- results.ids) println(id)
    }
  }
}