package com.graphbrain

import com.mongodb.Mongo
import com.mongodb.DB
import com.mongodb.DBCollection
import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.mongodb.DBCursor
import scala.collection.JavaConversions._

object Store {
	val conn = new Mongo()
  val db = conn.getDB("gb")
  val coll = db.getCollection("ver")

  def get(_id: String) = {
    val query = new BasicDBObject();
    query.put("_id", _id);
    coll.findOne(query).asInstanceOf[java.util.Map[String, Any]].toMap
  }

  def put(doc: Map[String, Any]) = {
    val dbobj = new BasicDBObject()
    for ((key, value) <- doc) dbobj.put(key, value)
    coll.insert(dbobj)
  }
}