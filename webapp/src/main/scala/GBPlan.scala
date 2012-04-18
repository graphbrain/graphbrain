package com.graphbrain.webapp

import unfiltered.request._
import unfiltered.response._
import unfiltered.netty._

import com.codahale.logula.Logging

import com.graphbrain.hgdb.VertexStore
import com.graphbrain.hgdb.SimpleCaching
import com.graphbrain.searchengine.RiakSearchInterface


object GBPlan extends cycle.Plan with cycle.SynchronousExecution with ServerErrorResponse with Logging {
  val store = new VertexStore("gb") with SimpleCaching

  val databaseName = System.getProperty("myapp.db.name")

  def intent = {
    // TODO: deactive in production
    case req@GET(Path("/exit")) =>
      log.info(req.remoteAddr + " EXIT")
      System.exit(0)
      ComingSoon()

    case req@GET(Path("/")) => {
      log.info(req.remoteAddr + " GET /")
      ComingSoon()
    }
    case req@GET(Path("/secret")) => { 
      log.info(req.remoteAddr + " GET /secret")
      Redirect("/node/welcome/graphbrain")
    }
    case req@GET(Path(Seg("node" :: n1 :: Nil))) => {
      val id = n1 
      log.info(req.remoteAddr + " NODE " + id)
      NodePage(store, id, Server.prod)
    }
    case req@GET(Path(Seg("node" :: n1 :: n2 :: Nil))) => {
      val id = n1 + "/" + n2 
      log.info(req.remoteAddr + " NODE " + id)
      NodePage(store, id, Server.prod)
    }
    case req@GET(Path(Seg("node" :: n1 :: n2 :: n3 :: Nil))) => {
      val id = n1 + "/" + n2 + "/" + n3
      log.info(req.remoteAddr + " NODE " + id)
      NodePage(store, id, Server.prod)
    }
    case req@GET(Path(Seg("node" :: n1 :: n2 :: n3 :: n4 :: Nil))) => {
      val id = n1 + "/" + n2 + "/" + n3 + "/" + n4
      log.info(req.remoteAddr + " NODE " + id)
      NodePage(store, id, Server.prod)
    }
    case req@GET(Path(Seg("node" :: n1 :: n2 :: n3 :: n4 :: n5 :: Nil))) => {
      val id = n1 + "/" + n2 + "/" + n3 + "/" + n4 + "/" + n5
      log.info(req.remoteAddr + " NODE " + id)
      NodePage(store, id, Server.prod)
    }
    case req@POST(Path("/search") & Params(params)) => {
      val query = params("q")(0)
      val si = RiakSearchInterface("gbsearch")
      var results = si.query(query)
      // if not results are found for exact match, try fuzzier
      if (results.numResults == 0)
        results = si.query(query + "*")
      log.info(req.remoteAddr + " SEARCH " + query + "; results: " + results.numResults)
      SearchResponse(store, results)
    }
  }
}