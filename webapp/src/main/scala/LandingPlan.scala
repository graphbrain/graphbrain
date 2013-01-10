package com.graphbrain.webapp

import unfiltered.request._
import unfiltered.netty._
import unfiltered.Cookie


object LandingPlan extends cycle.Plan with cycle.SynchronousExecution with ServerErrorResponse {
  def pageResponse(template: String, page: String, title: String, cookies: Map[String, Any], req: HttpRequest[Any]) = {
    
    Server.log(req, cookies, "PAGE " + page)
    
    Server.scalateResponse(template, page, title, cookies, req)
  }

  def intent = {
    case req@GET(Path("/") & Cookies(cookies)) => {
      pageResponse("ComingSoon.ssp", "comingsoon", "Coming soon", cookies, req)
    }
    case req@GET(Path("/secret") & Cookies(cookies)) => {
      pageResponse("Landing.ssp", "home", "Welcome", cookies, req)
    }
    // Google Web Tools verification
    case req@GET(Path("/googlec78be0b8a9e576fe.html") & Cookies(cookies)) => {
      pageResponse("google.ssp", "google", "google", cookies, req)
    }
  }
}
