package com.graphbrain.webapp

import unfiltered.request._
import unfiltered.response._
import unfiltered.netty._
import unfiltered.Cookie
import akka.actor.Actor
import akka.actor.Props

import com.graphbrain.utils.SimpleLog


object ContextsPlan extends cycle.Plan with cycle.SynchronousExecution with ServerErrorResponse {
  def intent = {
    case req@POST(Path("/createcontext") & Params(params) & Cookies(cookies)) =>
      val userNode = Server.getUser(cookies)
      val name = params("name")(0)
      println("$$$$$$$$$ create context: " + name)
      Ok
  }
}
