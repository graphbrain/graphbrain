package com.example

import unfiltered.request._
import unfiltered.response._

import unfiltered.netty._

/** Asynchronous plan that gets the time in a ridiculous fashion.
 *  (But imagine that it's using a vital external HTTP service to
 *  inform its response--this is a fine way to do that.) */
object Time extends async.Plan
  with ServerErrorResponse {
  val logger = org.clapper.avsl.Logger(getClass)
  
  def intent = {
    case req @ GET(Path("/time")) => 
      logger.debug("GET /time")
      import dispatch._
      // the call below is non-blocking, so we return quickly
      // and free netty's worker thread
      Server.http(:/("127.0.0.1", 8080).POST / "time" >- { time =>
        // later, we respond to the request
        req.respond(view(time))
      })
    case req @ POST(Path("/time")) =>
      logger.debug("POST /time")
      // since we don't have to do any blocking IO for this request
      // we can call respond right way
      req.respond(ResponseString(new java.util.Date().toString))
  }
  def view(time: String) = {
    Html(
     <html><body>
       The current time is: { time }
     </body></html>
   )
  }
}
