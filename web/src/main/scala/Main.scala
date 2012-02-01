package com.graphbrain.web

import unfiltered.request._
import unfiltered.response._
import unfiltered.jetty._
import unfiltered.scalate._
import unfiltered.jetty.Server

import org.clapper.avsl.Logger


object WebServer {
  val logger = Logger(WebServer getClass)

  def main(args: Array[String]){
    val port = 8080
    logger.info("starting GraphBrain web server at localhost on port %s" format port)
    Http(port).context("/static"){ ctx: ContextBuilder =>
      ctx.resources(new java.net.URL("file:web/src/main/resources/www"))
    }.filter(unfiltered.filter.Planify {
      case req @ GET(Path(Seg(Nil))) => {
        Ok ~> Scalate(req, "templates/login.mustache")
      }
      case req @ GET(Path(Seg("node" :: nodeId :: Nil))) => {
        logger.debug("GET /node/%s" format nodeId)
        Ok ~> Scalate(req, "templates/node.mustache", ("nodeId", nodeId))
      }
    }).run
  }
}