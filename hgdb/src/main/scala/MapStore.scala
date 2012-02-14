package com.graphbrain.hgdb

import com.basho.riak.client.raw.http.HTTPClientConfig
import com.basho.riak.client.IRiakClient
import com.basho.riak.client.RiakFactory
import com.basho.riak.client.IRiakObject
import com.basho.riak.client.bucket.Bucket

/** Interface to Riak, a distributed key/value store. */
class MapStore(val storeName: String) {
  case class Doc(params: Map[String, Any])

	val conf = new HTTPClientConfig.Builder().withHost("127.0.0.1").withPort(8098).build()
  val client = RiakFactory.newClient(conf)
  val bucket = client.createBucket(storeName).execute()

  /** Gets a document by it's id in the form of a Map[String, Any] */
  def get(id: String) = {
    val value = bucket.fetch(id).execute()
    value match {
      case s: IRiakObject => str2map(s.getValueAsString)
      case _ => Map[String, Any]()
    }
  }

  /** Puts a document represented by a Map[String, Any] into the store */
  def put(id: String, doc: Map[String, Any]) = bucket.store(id, map2str(doc)).execute()

  /** Updates a document identified by it's id with the information contained in the Map[String, Any] */
  def update(id: String, doc: Map[String, Any]) = {
    remove(id)
    put(id, doc)
  }

  /** Removes document identified by id */
  def remove(id: String) = bucket.delete(id).execute()

  private def map2str(map: Map[String, Any]) = {
    val paramList = for (item <- map) yield item._1.toString + " " + encodeValue(item._2.toString)
    paramList.reduceLeft(_ + "|" + _)
  }

  private def str2map(str: String): Map[String, Any] = {
    val strItems = str.split('|')
    val items = for (i <- strItems) yield i.split(" ", 2)
    items map { i => (i(0), decodeValue(i(1))) } toMap
  }

  private def encodeValue(value: String) = value.replace("#", "#1").replace("|", "#2")

  private def decodeValue(value: String) = value.replace("#2", "|").replace("#1", "#")
}

object MapStore {
  def apply(storeName: String) = new MapStore(storeName)
}