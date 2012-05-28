package com.graphbrain.webapp

import unfiltered.request._
import unfiltered.response._
import unfiltered.netty._
import unfiltered.scalate._

import com.codahale.logula.Logging


object LandingPlan extends cycle.Plan with cycle.SynchronousExecution with ServerErrorResponse with Logging {
  def nodeResponse(id: String, cookies: Map[String, Any], req: HttpRequest[Any]) = {
    val userNode = Server.getUser(cookies)
    val node = Server.store.get(id)
    log.info(Server.realIp(req) + " NODE " + id)
    NodePage(Server.store, node, userNode, Server.prod)
  }

  def intent = {
    case req@GET(Path("/")) => {
      Ok ~> Scalate(req, "Landing.ssp")
    }
    case req@GET(Path("/about")) => {
      Ok ~> Scalate(req, "about.ssp")
    }
    case req@GET(Path("/contact")) => {
      Ok ~> Scalate(req, "contact.ssp")
    }
    case req@GET(Path("/not_just_facts")) => {
      Ok ~> Scalate(req, "not_just_facts.ssp")
    }
    case req@GET(Path("/not_just_you")) => {
      Ok ~> Scalate(req, "not_just_you.ssp")
    }
    case req@GET(Path("/about_creators")) => {
      Ok ~> Scalate(req, "about_creators.ssp")
    }
  }
}