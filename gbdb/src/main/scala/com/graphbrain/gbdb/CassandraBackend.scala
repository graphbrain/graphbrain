package com.graphbrain.gbdb

import java.util.Arrays

import me.prettyprint.hector.api.Cluster
import me.prettyprint.hector.api.Keyspace
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition
import me.prettyprint.hector.api.ddl.KeyspaceDefinition
import me.prettyprint.hector.api.ddl.ComparatorType
import me.prettyprint.hector.api.ddl.ColumnType
import me.prettyprint.hector.api.factory.HFactory
import me.prettyprint.cassandra.service.ThriftKsDef
import me.prettyprint.cassandra.service.template.ColumnFamilyTemplate
import me.prettyprint.cassandra.service.template.ThriftColumnFamilyTemplate
import me.prettyprint.cassandra.service.template.ColumnFamilyResult
import me.prettyprint.cassandra.service.template.ColumnFamilyUpdater
import me.prettyprint.cassandra.serializers.StringSerializer
import me.prettyprint.cassandra.serializers.CompositeSerializer
import me.prettyprint.hector.api.beans.Composite


class CassandraBackend(clusterName: String, keyspaceName: String, ip: String="localhost", port: Int=9160) {
  val replicationFactor = 1

  val cluster: Cluster = HFactory.getOrCreateCluster(clusterName, ip + ":" + port)

  val keyspaceDef: KeyspaceDefinition = cluster.describeKeyspace(keyspaceName)
  if (keyspaceDef == null)
    createSchemas()

  val ksp: Keyspace = HFactory.createKeyspace(keyspaceName, cluster)

  val tpGlobal = new ThriftColumnFamilyTemplate[String, String](ksp, "global", StringSerializer.get(), StringSerializer.get())
  val tpUser = new ThriftColumnFamilyTemplate[String, String](ksp, "user", StringSerializer.get(), StringSerializer.get())
  val tpEmail = new ThriftColumnFamilyTemplate[String, String](ksp, "email", StringSerializer.get(), StringSerializer.get())
  val tpUserSpace = new ThriftColumnFamilyTemplate[String, String](ksp, "userspace", StringSerializer.get(), StringSerializer.get())
  val tpEdgeType = new ThriftColumnFamilyTemplate[String, String](ksp, "edgetype", StringSerializer.get(), StringSerializer.get())
  val tpEdges = new ThriftColumnFamilyTemplate[String, Composite](ksp, "edges", StringSerializer.get(), new CompositeSerializer())
  val tpVertexEdgeType = new ThriftColumnFamilyTemplate[String, String](ksp, "vertexedgetype", StringSerializer.get(), StringSerializer.get())
  val tpGlobalUser = new ThriftColumnFamilyTemplate[String, String](ksp, "globaluser", StringSerializer.get(), StringSerializer.get())
  val tpOwners = new ThriftColumnFamilyTemplate[String, String](ksp, "owners", StringSerializer.get(), StringSerializer.get())
  val tpDegrees = new ThriftColumnFamilyTemplate[String, String](ksp, "degrees", StringSerializer.get(), StringSerializer.get())
  val tpInstances = new ThriftColumnFamilyTemplate[String, String](ksp, "instances", StringSerializer.get(), StringSerializer.get())


  private def createSchemas() = {
    // How to delete keysapce:
    // start cassandra-cli
    // connect localhost/9160;
    // drop keyspace testhgdb;

    val cfGlobal = HFactory.createColumnFamilyDefinition(keyspaceName, "global", ComparatorType.UTF8TYPE)
    val cfUser = HFactory.createColumnFamilyDefinition(keyspaceName, "user", ComparatorType.UTF8TYPE)
    val cfEmail = HFactory.createColumnFamilyDefinition(keyspaceName, "email", ComparatorType.UTF8TYPE)
    val cfUserSpace = HFactory.createColumnFamilyDefinition(keyspaceName, "userspace", ComparatorType.UTF8TYPE)
    val cfEdgeType = HFactory.createColumnFamilyDefinition(keyspaceName, "edgetype", ComparatorType.UTF8TYPE)
    
    // edges are stored in map of vertex ids to edge definitions (redundant, denormalized)
    val cfEdges = HFactory.createColumnFamilyDefinition(keyspaceName, "edges", ComparatorType.COMPOSITETYPE)
    // (edge_type, position, vertex1, vertex2, vertexN)
    cfEdges.setComparatorTypeAlias("(UTF8Type, IntegerType, UTF8Type, UTF8Type, UTF8Type)")

    // maps vertices to types of edges they participate in
    val cfVertexEdgeType = HFactory.createColumnFamilyDefinition(keyspaceName, "vertexedgetype", ComparatorType.UTF8TYPE)
    cfVertexEdgeType.setDefaultValidationClass(ComparatorType.COUNTERTYPE.getClassName())
    cfVertexEdgeType.setColumnType(ColumnType.STANDARD)
    
    // global to user space map (which userspace vertices correspond to a given global vertex)
    val cfGlobalUser = HFactory.createColumnFamilyDefinition(keyspaceName, "globaluser", ComparatorType.UTF8TYPE)
    
    // map users to the userspace vertices they own
    val cfOwners = HFactory.createColumnFamilyDefinition(keyspaceName, "owners", ComparatorType.UTF8TYPE)
    
    // maintains vertices' degrees
    val cfDegrees = HFactory.createColumnFamilyDefinition(keyspaceName, "degrees", ComparatorType.UTF8TYPE)
    cfDegrees.setDefaultValidationClass(ComparatorType.COUNTERTYPE.getClassName())
    cfDegrees.setColumnType(ColumnType.STANDARD)

    // maintains edgetypes' number of instances
    val cfInstances = HFactory.createColumnFamilyDefinition(keyspaceName, "instances", ComparatorType.UTF8TYPE)
    cfInstances.setDefaultValidationClass(ComparatorType.COUNTERTYPE.getClassName())
    cfInstances.setColumnType(ColumnType.STANDARD)

    val newKeyspace = HFactory.createKeyspaceDefinition(keyspaceName,
      ThriftKsDef.DEF_STRATEGY_CLASS, replicationFactor,
      Arrays.asList(cfGlobal, cfUser, cfEmail, cfUserSpace, cfEdgeType, cfEdges, cfVertexEdgeType, cfGlobalUser, cfOwners, cfDegrees, cfInstances))
    
    // "true" as the second param means that Hector will block until all nodes see the change.
    cluster.addKeyspace(newKeyspace, true)
  }
}
