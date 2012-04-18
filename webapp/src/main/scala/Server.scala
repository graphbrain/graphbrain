package com.graphbrain.webapp

import java.net.URL
import com.codahale.logula.Logging
import org.apache.log4j.Level


object Server {
  val logger = org.clapper.avsl.Logger(Server.getClass)
  var http: unfiltered.netty.Http = null

  def start() = {
    http = unfiltered.netty.Http(8080)
      .handler(GBPlan)
      .resources(new URL(getClass().getResource("/robots.txt"), "."))
      
      http.run { s =>
        logger.info("starting GraphBrain webapp at localhost on port %s".format(s.port))
        //unfiltered.util.Browser.open("http://127.0.0.1:%d/".format(s.port))
      }
  }

  def main(args: Array[String]) {
    Logging.configure { log =>
      log.registerWithJMX = true

      log.level = Level.INFO
      log.loggers("com.graphbrain.webapp") = Level.INFO

      log.console.enabled = false

      log.file.enabled = true
      log.file.filename = "./logs/webapp.log"
      log.file.threshold = Level.INFO
      log.file.maxSize = 1024 * 1024 // Kb
      log.file.retainedFiles = 5 // keep five old logs around

      log.syslog.enabled = false
    }

    start()
  }
}
