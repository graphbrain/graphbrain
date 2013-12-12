package com.graphbrain.webapp

import unfiltered.request._
import unfiltered.response._
import unfiltered.netty._

object LandingPlan extends cycle.Plan with cycle.SynchronousExecution with ServerErrorResponse {
  def pageResponse(template: String, page: String, title: String, cookies: Map[String, Any], req: HttpRequest[Any]) = {
    
    WebServer.log(req, cookies, "PAGE " + page)
    
    WebServer.scalateResponse(template, page, title, cookies, req)
  }

  def intent = {
    case req@GET(Path("/") & Cookies(cookies)) => {
      val userNode = WebServer.getUser(cookies)

      if (userNode == null) {
        pageResponse("Landing.ssp", "home", "Graphbrain", cookies, req)
      }
      else {
        Redirect("/node/" + userNode.id)
      }
    }
    // Google Web Tools verification
    case req@GET(Path("/googlec78be0b8a9e576fe.html") & Cookies(cookies)) => {
      pageResponse("google.ssp", "google", "google", cookies, req)
    }
  }
}
