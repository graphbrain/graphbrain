package com.graphbrain.webapp

import unfiltered.request._
import unfiltered.response._
import unfiltered.netty._

object ContextsPlan extends cycle.Plan with cycle.SynchronousExecution with ServerErrorResponse {
  def intent = {
    case req@POST(Path("/createcontext") & Params(params) & Cookies(cookies)) =>
      //val userNode = WebServer.getUser(cookies)
      val name = params("name")(0)
      println("$$$$$$$$$ create context: " + name)
      //WebServer.graph.createContext(name, userNode.id, "public")
      Ok
  }
}
