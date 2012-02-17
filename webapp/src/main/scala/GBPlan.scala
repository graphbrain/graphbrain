package com.graphbrain.webapp

import unfiltered.request._
import unfiltered.response._

import unfiltered.netty._

object GBPlan extends cycle.Plan with cycle.SynchronousExecution with ServerErrorResponse {
  val logger = org.clapper.avsl.Logger(getClass)
  
  def intent = {
    case GET(Path("/")) => 
      logger.debug("GET /")
      view(Map.empty)(<p> What say you? </p>)
  }
  
  def palindrome(s: String) = s.toLowerCase.reverse == s.toLowerCase
  
  def view(params: Map[String, Seq[String]])(body: scala.xml.NodeSeq) = {
    def p(k: String) = params.get(k).flatMap { _.headOption } getOrElse("")
    Html(
     <html><body>
       { body }
       <form method="POST">
         Integer <input name="int" value={ p("int") } ></input>
         Palindrome <input name="palindrome" value={ p("palindrome") } />
         <input type="submit" />
       </form>
     </body></html>
   )
  }
}
