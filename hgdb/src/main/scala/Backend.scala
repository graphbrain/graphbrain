package com.graphbrain.hgdb

/** Generic interface to a key/value store. */
trait Backend {
  
  /** Gets a document by it's id in the form of a Map[String, Any] */
  def get(id: String): Map[String, Any]

  def rawget(id: String): String

  /** Puts a document represented by a Map[String, Any] into the store */
  def put(id: String, doc: Map[String, Any])
  /** Updates a document identified by it's id with the information contained in the Map[String, Any] */
  def update(id: String, doc: Map[String, Any])
  /** Removes document identified by id */
  def remove(id: String)
  
  protected def map2str(map: Map[String, Any]) = {
    val paramList = for (item <- map) yield item._1.toString + " " + encodeValue(item._2.toString)
    paramList.reduceLeft(_ + "|" + _)
  }

  protected def str2map(str: String): Map[String, Any] = {
    val strItems = str.split('|')
    val items = for (i <- strItems) yield i.split(" ", 2)
    items map { i => (i(0), decodeValue(i(1))) } toMap
  }

  protected def encodeValue(value: String) = value.replace("#", "#1").replace("|", "#2")

  protected def decodeValue(value: String) = value.replace("#2", "|").replace("#1", "#")
}