package com.graphbrain.webapp

import unfiltered.request._
import unfiltered.response._
import unfiltered.netty._

import com.codahale.logula.Logging

import com.graphbrain.hgdb.VertexStore
import com.graphbrain.hgdb.SimpleCaching
import com.graphbrain.hgdb.UserManagement
import com.graphbrain.searchengine.RiakSearchInterface


object GBPlan extends cycle.Plan with cycle.SynchronousExecution with ServerErrorResponse with Logging {
  val store = new VertexStore("gb") with SimpleCaching with UserManagement

  val databaseName = System.getProperty("myapp.db.name")

  def realIp(req: HttpRequest[Any]) = {
    val headers = req.headers("X-Forwarded-For")
    if (headers.hasNext)
      headers.next
    else
      req.remoteAddr
  }

  def intent = {
    // TODO: deactive in production
    case req@GET(Path("/exit")) =>
      log.info(realIp(req) + " EXIT")
      if (!Server.prod)
        System.exit(0)
      ComingSoon(Server.prod)

    case req@GET(Path("/")) => {
      log.info(realIp(req) + " GET /")
      ComingSoon(Server.prod)
    }
    case req@GET(Path("/secret")) => { 
      log.info(realIp(req) + " GET /secret")
      Redirect("/node/welcome/graphbrain")
    }
    case req@GET(Path(Seg("node" :: n1 :: Nil))) => {
      val id = n1 
      log.info(realIp(req) + " NODE " + id)
      NodePage(store, id, Server.prod)
    }
    case req@GET(Path(Seg("node" :: n1 :: n2 :: Nil))) => {
      val id = n1 + "/" + n2 
      log.info(realIp(req) + " NODE " + id)
      NodePage(store, id, Server.prod)
    }
    case req@GET(Path(Seg("node" :: n1 :: n2 :: n3 :: Nil))) => {
      val id = n1 + "/" + n2 + "/" + n3
      log.info(realIp(req) + " NODE " + id)
      NodePage(store, id, Server.prod)
    }
    case req@GET(Path(Seg("node" :: n1 :: n2 :: n3 :: n4 :: Nil))) => {
      val id = n1 + "/" + n2 + "/" + n3 + "/" + n4
      log.info(realIp(req) + " NODE " + id)
      NodePage(store, id, Server.prod)
    }
    case req@GET(Path(Seg("node" :: n1 :: n2 :: n3 :: n4 :: n5 :: Nil))) => {
      val id = n1 + "/" + n2 + "/" + n3 + "/" + n4 + "/" + n5
      log.info(realIp(req) + " NODE " + id)
      NodePage(store, id, Server.prod)
    }
    case req@POST(Path("/search") & Params(params)) => {
      val query = params("q")(0)
      val si = RiakSearchInterface("gbsearch")
      var results = si.query(query)
      // if not results are found for exact match, try fuzzier
      if (results.numResults == 0)
        results = si.query(query + "*")
      log.info(realIp(req) + " SEARCH " + query + "; results: " + results.numResults)
      SearchResponse(store, results)
    }
    case req@POST(Path("/signup") & Params(params)) => {
      val name = params("name")(0)
      val username = params("username")(0)
      val email = params("email")(0)
      val password = params("password")(0)
      store.createUser(username, name, email, password, "user")
      log.info(realIp(req) + " SIGNUP name: " + name + "; username: " + username + "; email:" + email)
      ResponseString("ok")
    }
    case req@POST(Path("/checkusername") & Params(params)) => {
      val username = params("username")(0)
      if (store.usernameExists(username)) {
        ResponseString("exists " + username)
      }
      else {
        ResponseString("ok " + username)
      }
    }
    case req@POST(Path("/checkemail") & Params(params)) => {
      val email = params("email")(0)
      if (store.emailExists(email)) {
        ResponseString("exists " + email)
      }
      else {
        ResponseString("ok " + email) 
      }
    }
  }
}