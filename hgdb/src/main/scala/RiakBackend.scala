package com.graphbrain.hgdb

import com.basho.riak.client.raw.http.HTTPClientConfig
import com.basho.riak.client.IRiakClient
import com.basho.riak.client.RiakFactory
import com.basho.riak.client.IRiakObject
import com.basho.riak.client.bucket.Bucket

/** Interface to Riak, a distributed key/value store. */
class RiakBackend(val bucketName: String) extends Backend {
	//val conf = new HTTPClientConfig.Builder().withHost("127.0.0.1").withPort(8098).build()
  val conf = new HTTPClientConfig.Builder().withHost("192.168.129.4").withPort(8098).build()
  val client = RiakFactory.newClient(conf)
  val bucket = client.createBucket(bucketName).execute()

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
    try {
      remove(id)
    }
    catch {
      case _ =>
    }
    put(id, doc)
  }

  /** Removes document identified by id */
  def remove(id: String) = bucket.delete(id).execute()
}