package com.graphbrain.webapp

import unfiltered.request._
import unfiltered.response._
import unfiltered.netty._
import unfiltered.Cookie

import com.codahale.logula.Logging


object AIChatPlan extends async.Plan with ServerErrorResponse with Logging {
  def intent = {
    case req@GET(Path(Seg("xpto" :: Nil)) & Cookies(cookies)) =>
      req.respond(PlainTextContent ~> ResponseString("Hello asynch world!"))
  }
}