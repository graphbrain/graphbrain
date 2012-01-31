package com.graphbrain.web

import unfiltered.filter._
import unfiltered.request._
import unfiltered.response._
//import unfiltered.scalate._

import org.clapper.avsl.Logger

class MainPlan extends Plan {
  //val logger = Logger(classOf[UserPlan])
  
  def intent = {
    case req @ GET(Path(Seg("node" :: node :: Nil))) => {
      //logger.debug("GET /users/%s" format name)
      ResponseString("""<html>Hello GraphBrain!</html>""")
      //repository findUser name map { Ok ~> renderer(req)(_) } getOrElse NotFound
    }
  }
}