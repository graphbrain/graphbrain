package com.graphbrain.webapp

import unfiltered.response._
import java.io.Writer
import com.graphbrain.hgdb.VertexStore
import com.graphbrain.searchengine.SearchResults


case class SearchResponse(store: VertexStore, results: SearchResults) extends ResponseWriter {
	def write(writer: Writer) {
      	var html = "<div>"
      	if (results.numResults == 0) {
      		html += "<p>Sorry, no results found.</p>"
      	}
      	else {
      		html += "<p>" + results.numResults + " results found.</p>"
      		for (id <- results.ids) {
      			val vertex = store.get(id)
      			if (vertex.toString != "") {
      				html += "<p><a href=\"/node/" + id + "\">" + vertex + "</a></p>"
      			}
      		}
      	}
            html += "</div>"

		writer.write(html)
	} 
}