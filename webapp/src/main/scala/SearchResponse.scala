package com.graphbrain.webapp

import unfiltered.response._
import java.io.Writer
import com.graphbrain.searchengine.RiakSearchInterface
import com.graphbrain.hgdb.VertexStore


case class SearchResponse(store: VertexStore, query: String) extends ResponseWriter {
	def write(writer: Writer) {
		val si = RiakSearchInterface("gbsearch")
      	var results = si.query(query)
      	// if not results are found for exact match, try fuzzier
      	if (results.numResults == 0)
      		results = si.query(query + "*")
      	
      	var html = ""
      	if (results.numResults == 0) {
      		html = "<p>Sorry, no results found.</p>"
      	}
      	else {
      		html="<p>" + results.numResults + " results found.</p>"
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