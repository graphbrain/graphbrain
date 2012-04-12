package com.graphbrain.webapp

import unfiltered.response._
import java.io.Writer
import com.graphbrain.searchengine.RiakSearchInterface


case class SearchResponse(query: String) extends ResponseWriter {
	def write(writer: Writer) {
		val si = RiakSearchInterface("gbsearch")
      	val results = si.query(query)
      	//println("number of matches: " + results.numResults)
      	//for (id <- results.ids) println(id)

		writer.write(results.toString)
	} 
}