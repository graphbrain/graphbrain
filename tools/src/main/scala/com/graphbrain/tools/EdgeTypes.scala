package com.graphbrain.tools


import scala.collection.mutable.Map

import me.prettyprint.hector.api.factory.HFactory
import me.prettyprint.cassandra.serializers.StringSerializer

import com.graphbrain.gbdb.VertexStore
import com.graphbrain.gbdb.ID
import com.graphbrain.gbdb.Edge


object EdgeTypes {
  val store: VertexStore = new VertexStore("gb")

  def apply(args: Array[String]) = {
    val edgeTypes = Map[String, String]() 

    val rowCount = 100

    val rangeSlicesQuery = HFactory
      .createRangeSlicesQuery(store.backend.ksp, StringSerializer.get(), StringSerializer.get(), StringSerializer.get())
      .setColumnFamily("userspace")
      .setRange(null, null, false, 10)
      .setRowCount(rowCount)

    var lastKey: String = null

    var nodeCount = 0
    var typeCount = 0
    var done = false
    while (!done) {
      rangeSlicesQuery.setKeys(lastKey, null)

      val result = rangeSlicesQuery.execute()
      val rows = result.get()
      val rowsIterator = rows.iterator()

      if (lastKey != null && rowsIterator != null) rowsIterator.next()   

      while (rowsIterator.hasNext()) {
        val row = rowsIterator.next()
        lastKey = row.getKey()

        if (!row.getColumnSlice().getColumns().isEmpty()) {
          nodeCount += 1
          val id = row.getKey()
          if (id.startsWith("user/dbpedia")) {
            val edges = store.neighborEdges(id)
            for (e <- edges) {
              val et = e.edgeType
              if (!edgeTypes.contains(et)) {
                typeCount += 1
                edgeTypes(et) = e.toString
                println(ID.humanReadable(et) + ", " + e.humanReadable2)
              }
            }
          }
        }
      }

      if (rows.getCount() < rowCount)
        done = true
    }
  }

  def main(args: Array[String]) : Unit = {
    EdgeTypes(null)
  }
}