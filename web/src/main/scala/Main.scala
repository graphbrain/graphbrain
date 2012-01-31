package com.graphbrain.web

import unfiltered.request._
import unfiltered.response._
import unfiltered.jetty.Server

import org.clapper.avsl.Logger

object Main {
  val logger = Logger(Main getClass)
  val plans = Seq(new MainPlan)
  
  def applyPlans = plans.foldLeft(_: Server)(_ filter _)
  
  def main(args: Array[String]) {
    val port = 8080
    logger.info("starting GraphBrain web server at localhost on port %s" format port)
    applyPlans(unfiltered.jetty.Http(port)).run() 
  }
}