package com.graphbrain.searchengine

import dispatch._
import scala.xml._
import java.net.URLEncoder


class RiakSearchInterface(val index: String,
                          val host: String="127.0.0.1",
                          val port: Int=8098) extends SearchInterface {

  def initIndex(): Unit = {
	  val req = :/(host, port) / "riak" / index <<< 
  	  """{"props":{"precommit":[{"mod":"riak_search_kv_hook","fun":"precommit"}]}}""" <:<
  	  Map("content-type" -> "application/json")
  	CustomHttp(req >|)
  }

  def index(key: String, text: String): Unit = {
    val safeKey = URLEncoder.encode(key, "UTF-8")
    val req = :/(host, port) / "buckets" / index / "keys" / safeKey <<< text.toLowerCase
    CustomHttp(req >|)
  }

  def query(text: String): SearchResults = {
    val req = :/(host, port) / "solr" / index / "select" <<? Map(("q" -> text), ("rows" -> "100"))
    val response = CustomHttp(req as_str)

    val ns = XML.loadString(response)

    val res = ns \\ "result"
    val numFound = (res \ "@numFound").toString.toInt
    val docs = res \\ "doc"
    val ids = for (doc <- docs) yield
      (for (str <- (doc \\ "str") if (str \ "@name" text) == "id")
          yield (str text).trim()) head

    SearchResults(numFound, ids)
  }
}

object RiakSearchInterface {
  def apply(index: String,
              host: String="127.0.0.1",
              port: Int=8098) = {
    new RiakSearchInterface(index, host, port)
  }
}