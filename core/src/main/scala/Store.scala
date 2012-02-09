package com.graphbrain

import com.mongodb.Mongo
import com.mongodb.DB
import com.mongodb.DBCollection
import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.mongodb.DBCursor
import scala.collection.JavaConversions._

class Store(dbName: String, collName: String) {
	val conn = new Mongo()
  val db = conn.getDB(dbName)
  val coll = db.getCollection(collName)

  def get(_id: String) = {
    val query = new BasicDBObject();
    query.put("_id", _id);
    coll.findOne(query) match {
      case x: BasicDBObject => x.asInstanceOf[java.util.Map[String, Any]].toMap
      case _ => Map[String, Any]()
    }
  }

  def put(doc: Map[String, Any]) = coll.insert(mapToBasicDBObj(doc))

  def update(_id: String, doc: Map[String, Any]) = {
    val query = new BasicDBObject();
    query.put("_id", _id);
    coll.update(query, mapToBasicDBObj(doc))
  }

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
  def apply(dbName: String, collName: String) = new Store(dbName, collName)
}