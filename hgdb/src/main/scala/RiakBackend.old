package com.graphbrain.hgdb

import com.basho.riak.client.raw.pbc.PBClientConfig
import com.basho.riak.client.RiakFactory
import com.basho.riak.client.IRiakObject


/** Interface to Riak, a distributed key/value store. */
class RiakBackend(val bucketName: String, ip: String="127.0.0.1", port: Int=8098) extends Backend {
  //val conf = new PBClientConfig.Builder().withHost("192.168.129.4").build()
  val conf = new PBClientConfig.Builder().withHost("127.0.0.1").build()
  val client = RiakFactory.newClient(conf)

  val bucket = client.createBucket(bucketName).execute()

  /** Gets a document by it's id in the form of a Map[String, Any] */
  def get(id: String) = {
    val value = bucket.fetch(id).execute()
    value match {
      case s: IRiakObject => str2map(s.getValueAsString)
      case _ => throw KeyNotFound(id)
    }
  }

  def rawget(id: String): String = {
    val value = bucket.fetch(id).execute()
    value match {
      case s: IRiakObject => s.getValueAsString
      case _ => throw KeyNotFound(id)
    }
  }

  /** Puts a document represented by a Map[String, Any] into the store */
  def put(id: String, doc: Map[String, Any]) = bucket.store(id, map2str(doc)).w(1).returnBody(false).execute()

  /** Updates a document identified by it's id with the information contained in the Map[String, Any] */
  def update(id: String, doc: Map[String, Any]) = {
    put(id, doc)
  }

  /** Removes document identified by id */
  def remove(id: String) = bucket.delete(id).execute()
}