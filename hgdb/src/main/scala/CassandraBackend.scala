package com.graphbrain.hgdb

import java.util.Arrays

import me.prettyprint.hector.api.Cluster
import me.prettyprint.hector.api.Keyspace
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition
import me.prettyprint.hector.api.ddl.KeyspaceDefinition
import me.prettyprint.hector.api.ddl.ComparatorType
import me.prettyprint.hector.api.factory.HFactory
import me.prettyprint.cassandra.service.ThriftKsDef
import me.prettyprint.cassandra.service.template.ColumnFamilyTemplate
import me.prettyprint.cassandra.service.template.ThriftColumnFamilyTemplate
import me.prettyprint.cassandra.service.template.ColumnFamilyResult
import me.prettyprint.cassandra.service.template.ColumnFamilyUpdater
import me.prettyprint.cassandra.serializers.StringSerializer


/** Interface to Cassandra, a distributed key/value store. */
class CassandraBackend(storeName: String, ip: String="localhost", port: Int=9160) extends Backend {
  val replicationFactor = 1

  val cluster: Cluster = HFactory.getOrCreateCluster(storeName, ip + ":" + port)

  val keyspaceDef: KeyspaceDefinition = cluster.describeKeyspace("GBKeyspace")
  if (keyspaceDef == null) {
    createSchema()
  }

  val ksp: Keyspace = HFactory.createKeyspace("GBKeyspace", cluster)

  val template: ColumnFamilyTemplate[String, String] = new ThriftColumnFamilyTemplate[String, String](ksp, "GBColumnFamily", StringSerializer.get(), StringSerializer.get())


  private def createSchema() = {
    val cfDef: ColumnFamilyDefinition = HFactory.createColumnFamilyDefinition("GBKeyspace", "GBColumnFamily", ComparatorType.BYTESTYPE)
    val newKeyspace: KeyspaceDefinition = HFactory.createKeyspaceDefinition("GBKeyspace", ThriftKsDef.DEF_STRATEGY_CLASS, replicationFactor, Arrays.asList(cfDef))
    // Add the schema to the cluster.
    // "true" as the second param means that Hector will block until all nodes see the change.
    cluster.addKeyspace(newKeyspace, true)
  }

 
  /** Gets a document by it's id in the form of a Map[String, Any] */
  def get(id: String) = {
    try {
      val res: ColumnFamilyResult[String, String] = template.queryColumns(id)
      val value: String = res.getString("value")
      str2map(value)
    }
    catch {
      case _ => throw KeyNotFound(id)
    }
  }

  def rawget(id: String): String = {
    try {
      val res: ColumnFamilyResult[String, String] = template.queryColumns(id)
      val value: String = res.getString("value")
      value
    }
    catch {
      case _ => throw KeyNotFound(id)
    }
  }

  /** Puts a document represented by a Map[String, Any] into the store */
  def put(id: String, doc: Map[String, Any]) = {
    val updater: ColumnFamilyUpdater[String, String] = template.createUpdater(id)
    updater.setString("value", map2str(doc))
    template.update(updater)
  }

  /** Updates a document identified by it's id with the information contained in the Map[String, Any] */
  def update(id: String, doc: Map[String, Any]) = {
    put(id, doc)
  }

  /** Removes document identified by id */
  def remove(id: String) = template.deleteRow(id)
}
