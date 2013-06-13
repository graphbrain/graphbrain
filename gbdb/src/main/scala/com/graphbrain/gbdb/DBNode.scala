package com.graphbrain.gbdb

import org.fusesource.lmdbjni._
import org.fusesource.lmdbjni.Constants._



object DBNode {
  def main(args: Array[String]): Unit = {
    val env = new Env()
    try {
      env.open("dbnode")
      val db = env.openDatabase("test")

      for (i <- 0 to 100) {
        val key = "key" + i
        val value = "value" + i
        db.put(bytes(key), bytes(value))
      }

      //val value = string(db.get(bytes("key55")))
      //println(value)

      val tx = env.createTransaction(true)
      try {
        val cursor = db.openCursor(tx)
        try {
          cursor.seek(KEY, bytes("key9"))
          var entry = cursor.get(GET_CURRENT)
          while (entry != null) {
            val key = string(entry.getKey())
            val value = string(entry.getValue())
            println(key + " = " + value)
            entry = cursor.get(NEXT)
          }
		}
        finally {
          cursor.close()
        }
      }
      finally {
        tx.commit()
      }

      db.close()
	}
	finally {
		env.close()
	}
  }
}