package com.graphbrain.webapp

import unfiltered.request._
import unfiltered.response._

import unfiltered.netty._

object GBPlan extends cycle.Plan with cycle.SynchronousExecution with ServerErrorResponse {
  val logger = org.clapper.avsl.Logger(getClass)
  
  def intent = {
    case GET(Path("/")) => 
      logger.debug("GET /")
      ComingSoon()
    case GET(Path("/search")) => 
      logger.debug("GET /search")
      SearchPage()
    case GET(Path("/node")) => 
      logger.debug("GET /node")
      NodePage("welcome/graphbrain")
  }
}
