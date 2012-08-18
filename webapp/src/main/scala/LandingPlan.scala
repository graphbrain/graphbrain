package com.graphbrain.webapp

import unfiltered.request._
import unfiltered.netty._
import unfiltered.Cookie

import com.codahale.logula.Logging


object LandingPlan extends cycle.Plan with cycle.SynchronousExecution with ServerErrorResponse with Logging {
  def pageResponse(template: String, page: String, title: String, cookies: Map[String, Any], req: HttpRequest[Any]) = {
    log.info(Server.realIp(req) + " PAGE " + page)
    Server.scalateResponse(template, page, title, cookies, req)
  }

  def intent = {
    case req@GET(Path("/") & Cookies(cookies)) => {
      pageResponse("ComingSoon.ssp", "comingsoon", "Coming soon", cookies, req)
    }
    case req@GET(Path("/secret") & Cookies(cookies)) => {
      pageResponse("Landing.ssp", "home", "Welcome", cookies, req)
    }
    case req@GET(Path("/about") & Cookies(cookies)) => {
      pageResponse("About.ssp", "about", "About", cookies, req)
    }
    case req@GET(Path("/contact") & Cookies(cookies)) => {
      pageResponse("contact.ssp", "contact", "Contact", cookies, req)
    }
    case req@GET(Path("/not_just_facts") & Cookies(cookies)) => {
      pageResponse("not_just_facts.ssp", "about", "About", cookies, req)
    }
    case req@GET(Path("/not_just_you") & Cookies(cookies)) => {
      pageResponse("not_just_you.ssp", "about", "About", cookies, req)
    }
    case req@GET(Path("/about_creators") & Cookies(cookies)) => {
      pageResponse("about_creators.ssp", "about", "About", cookies, req)
    }
  }
}
