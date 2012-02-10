package com.graphbrain

import com.mongodb.Mongo
import com.mongodb.DB
import com.mongodb.DBCollection
import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.mongodb.DBCursor
import scala.collection.JavaConversions._

/** Interface to a simple key/value store.
  *
  * An underalying distributed database (currently MongoDB) is abstracted to expose a very
  * simple key/value store interface. 
  */
class Store(storeName: String) {
	val nameParts = storeName.split('.')
  val dbName = nameParts(0)
  val collName = nameParts(1)

  val conn = new Mongo()
  val db = conn.getDB(dbName)
  val coll = db.getCollection(collName)

  /** Gets a document by it's _id in the form of a Map[String, Any] */
  def get(_id: String) = {
    val query = new BasicDBObject();
    query.put("_id", _id);
    coll.findOne(query) match {
      case x: BasicDBObject => x.asInstanceOf[java.util.Map[String, Any]].toMap
      case _ => Map[String, Any]()
    }
  }

  /** Puts a document represented by a Map[String, Any] into the store */
  def put(doc: Map[String, Any]) = coll.insert(mapToBasicDBObj(doc))

  /** Updates a document identified by it's _id with the information contained in the Map[String, Any] */
  def update(_id: String, doc: Map[String, Any]) = {
    val query = new BasicDBObject();
    query.put("_id", _id);
    coll.update(query, mapToBasicDBObj(doc))
  }

  /** Removes document identified by _id */
  def remove(_id: String) = {
    val query = new BasicDBObject();
    query.put("_id", _id);
    coll.remove(query)
  }

  private def mapToBasicDBObj(doc: Map[String, Any]) = {
    val dbobj = new BasicDBObject()
    for ((key, value) <- doc) value match {
      case value: String => dbobj.put(key, value)
      case value: Array[String] => dbobj.put(key, value)
      case _ =>
    }
    dbobj
  }
}

object Store {
  def apply(storeName: String) = new Store(storeName)
}