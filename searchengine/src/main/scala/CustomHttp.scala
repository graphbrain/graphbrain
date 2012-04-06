package com.graphbrain.searchengine


import dispatch._
import org.apache.http.auth.AuthScope


/** May be used directly from any thread. */
object CustomHttp extends Http with thread.Safety {
  type CurrentCredentials = util.DynamicVariable[Option[(AuthScope, Credentials)]]

  override def make_logger =
    new Logger {
      def info(msg: String, items: Any*) {}
      def warn(msg: String, items: Any*) { 
        println("WARN: [console logger] dispatch: " +
                msg.format(items: _*))
      }
    }
}