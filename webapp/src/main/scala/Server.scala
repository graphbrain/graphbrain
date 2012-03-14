package com.graphbrain.webapp

import java.net.URL

object Server {
  val logger = org.clapper.avsl.Logger(Server.getClass)
  val http = new dispatch.nio.Http

  def main(args: Array[String]) {
    unfiltered.netty.Http(8080)
      .handler(GBPlan)
      .resources(new URL(getClass().getResource("/robots.txt"), "."))
      .run { s =>
        logger.info("starting GraphBrain webapp at localhost on port %s".format(s.port))
        //unfiltered.util.Browser.open("http://127.0.0.1:%d/".format(s.port))
      }
    http.shutdown()
  }
}
