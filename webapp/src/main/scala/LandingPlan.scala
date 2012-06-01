package com.graphbrain.webapp

import unfiltered.request._
import unfiltered.netty._
import unfiltered.Cookie

import com.codahale.logula.Logging


object LandingPlan extends cycle.Plan with cycle.SynchronousExecution with ServerErrorResponse with Logging {
  def pageResponse(template: String, page: String, cookies: Map[String, Any], req: HttpRequest[Any]) = {
    log.info(Server.realIp(req) + " PAGE " + page)
    Server.scalateResponse(template, page, cookies, req)
  }

  def intent = {
    case req@GET(Path("/") & Cookies(cookies)) => {
      pageResponse("Landing.ssp", "home", cookies, req)
    }
    case req@GET(Path("/about") & Cookies(cookies)) => {
      pageResponse("About.ssp", "about", cookies, req)
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
      pageResponse("about_creators.ssp", "about", cookies, req)
    }
  }
}