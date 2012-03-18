package com.graphbrain.hgdb

import dispatch._
import scala.xml._


class RiakSearchInterface(val index: String,
                          val host: String="127.0.0.1",
                          val port: Int=8098) extends SearchInterface {

  def initIndex(): Unit = {
	  val req = :/(host, port) / "riak" / index <<< 
  	  """{"props":{"precommit":[{"mod":"riak_search_kv_hook","fun":"precommit"}]}}""" <:<
  	  Map("content-type" -> "application/json")
  	Http(req >|)
  }

  def index(key: String, text: String): Unit = {
    val req = :/(host, port) / "buckets" / index / "keys" / key <<< text
    Http(req >|)
  }

  def query(text: String): SearchResults = {
    val req = :/(host, port) / "solr" / index / "select" <<? Map(("q" -> text))
    val response = Http(req as_str)

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
  def main(args: Array[String]): Unit = {
  	val rsi = new RiakSearchInterface("gbsearch3")
    rsi.initIndex()
    rsi.index("name5", "Xpto Menezes")
    println(rsi.query("Menezes"))
  }
}