package com.graphbrain.webapp

import unfiltered.response._
import java.io.Writer
import com.graphbrain.searchengine.RiakSearchInterface
import com.graphbrain.hgdb.VertexStore


case class SearchResponse(store: VertexStore, query: String) extends ResponseWriter {
	def write(writer: Writer) {
		val si = RiakSearchInterface("gbsearch")
      	val results = si.query(query + "*")
      	var html = ""
      	if (results.numResults == 0) {
      		html = "<p>Sorry, no results found.</p>"
      	}
      	else {
      		for (id <- results.ids) {
      			val vertex = store.get(id)
      			if (vertex.toString != "") {
      				html += "<p><a href=\"/node/" + id + "\">" + vertex + "</a></p>"
      			}
      		}
      	}

		writer.write(html)
	} 
}