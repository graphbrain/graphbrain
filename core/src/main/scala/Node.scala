package com.graphbrain

import com.mongodb.Mongo
import com.mongodb.DB
import com.mongodb.DBCollection
import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.mongodb.DBCursor
import scala.collection.JavaConverters._
import scala.collection.mutable.Map

class Node(fields: Map[String, AnyRef]) {
  val _id = fields.getOrElse("_id", "").toString
  val label = fields.getOrElse("label", "").toString
  val targs = fields.getOrElse("targs", "").toString

  override def toString: String = _id + ": " + label + " ... " + targs
}

object Node {
  val m = new Mongo()
  val db = m.getDB("gb")
  val coll = db.getCollection("nodes")

  //def apply(fields: Map[String, AnyRef]) = new Node(fields) 

  def apply(_id: String) = {
    val query = new BasicDBObject();
    query.put("_id", _id);
    val map = coll.findOne(query).asInstanceOf[java.util.Map[String, AnyRef]].asScala

    new Node(map)
  }
/*
  def main(args: Array[String]) = {
    println(Node("wikipedia/alan_ball_(screenwriter)"))
  }
*/
}