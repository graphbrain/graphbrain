package com.graphbrain.db;

import scala.collection.mutable.Set

import java.net.URLEncoder
import java.security.SecureRandom

import me.prettyprint.hector.api.factory.HFactory
import me.prettyprint.cassandra.serializers.StringSerializer


trait UserManagement extends VertexStore {

  def allUsers = {
    val users = Set[UserNode]()
    val rowCount = 100

    val rangeSlicesQuery = HFactory
      .createRangeSlicesQuery(backend.ksp, StringSerializer.get(), StringSerializer.get(), StringSerializer.get())
      .setColumnFamily("user")
      .setRange(null, null, false, 10)
      .setRowCount(rowCount)

    var lastKey: String = null

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
          val id = row.getKey()
          users.add(getUserNode(id))
        }
      }

      if (rows.getCount() < rowCount)
        done = true
    }

    users
  }
}