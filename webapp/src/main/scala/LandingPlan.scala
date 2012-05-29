package com.graphbrain.webapp

import unfiltered.request._
import unfiltered.response._
import unfiltered.netty._
import unfiltered.Cookie
import unfiltered.scalate._

import com.codahale.logula.Logging


object LandingPlan extends cycle.Plan with cycle.SynchronousExecution with ServerErrorResponse with Logging {
  def pageResponse(template: String, page: String, cookies: Map[String, Any], req: HttpRequest[Any]) = {
    val userNode = Server.getUser(cookies)
    log.info(Server.realIp(req) + " PAGE " + page)
    Ok ~> Scalate(req, template, ("navBar", NavBar(userNode, page).html))
  }

  def intent = {
    case req@GET(Path("/") & Cookies(cookies)) => {
      pageResponse("Landing.ssp", "home", cookies, req)
    }
    case req@GET(Path("/about") & Cookies(cookies)) => {
      pageResponse("about.ssp", "about", cookies, req)
    }
    case req@GET(Path("/contact") & Cookies(cookies)) => {
      pageResponse("contact.ssp", "contact", cookies, req)
    }
    case req@GET(Path("/not_just_facts") & Cookies(cookies)) => {
      pageResponse("not_just_facts.ssp", "about", cookies, req)
    }
    case req@GET(Path("/not_just_you") & Cookies(cookies)) => {
      pageResponse("not_just_you.ssp", "about", cookies, req)
    }
    case req@GET(Path("/about_creators") & Cookies(cookies)) => {
      Ok ~> Scalate(req, "about_creators.ssp")
      pageResponse("about_creators.ssp", "about", cookies, req)
    }
  }
}