package com.graphbrain.webapp

import unfiltered.request._
import unfiltered.response._
import unfiltered.netty._
import unfiltered.Cookie

import com.codahale.jerkson.Json._

import com.codahale.logula.Logging

import com.graphbrain.searchengine.RiakSearchInterface


object GBPlan extends cycle.Plan with cycle.SynchronousExecution with ServerErrorResponse with Logging {
  def intent = {
    // TODO: deactive in production
    case req@GET(Path("/exit")) =>
      log.info(Server.realIp(req) + " EXIT")
      if (!Server.prod)
        System.exit(0)
      ComingSoon(Server.prod)

    case req@GET(Path("/")) => {
      log.info(Server.realIp(req) + " GET /")
      ComingSoon(Server.prod)
    }
    case req@GET(Path("/secret")) => { 
      log.info(Server.realIp(req) + " GET /secret")
      Redirect("/node/welcome/graphbrain")
    }
    case req@POST(Path("/search") & Params(params)) => {
      val query = params("q")(0)
      val si = RiakSearchInterface("gbsearch")
      var results = si.query(query)
      // if not results are found for exact match, try fuzzier
      if (results.numResults == 0)
        results = si.query(query + "*")
      log.info(Server.realIp(req) + " SEARCH " + query + "; results: " + results.numResults)
      
      val resultsList: Seq[List[String]] = (for (id <- results.ids)
        yield List(id, Server.store.get(id).toString))
      
      val json = Map(("count" -> results.numResults), ("results" -> resultsList))
      ResponseString(generate(json))
    }
    case req@POST(Path("/add") & Params(params) & Cookies(cookies)) => {
      val userNode = Server.getUser(cookies)
      val textUrl = params("textUrl")(0)
      val relation = params("relation")(0)
      val brainId = params("curBrainId")(0)
      val rootId = params("rootId")(0)
      val root = Server.store.get(rootId)
      val results = Server.sparser.textToNode(textUrl, brainId)
      val node = results(0)
      println(node.id)
      Server.store.createAndConnectVertices(relation, Array(root, node))
      Redirect("/node/" + node.id)
    }
    case req@POST(Path("/addbrain") & Params(params) & Cookies(cookies)) => {
      val userNode = Server.getUser(cookies)
      val name = params("name")(0)
      val brainId = Server.store.createBrain(name, userNode, "public")
      Redirect("/node/" + brainId)
    }
    case req@POST(Path("/signup") & Params(params)) => {
      val name = params("name")(0)
      val username = params("username")(0)
      val email = params("email")(0)
      val password = params("password")(0)
      Server.store.createUser(username, name, email, password, "user")
      log.info(Server.realIp(req) + " SIGNUP name: " + name + "; username: " + username + "; email:" + email)
      ResponseString("ok")
    }
    case req@POST(Path("/checkusername") & Params(params)) => {
      val username = params("username")(0)
      if (Server.store.usernameExists(username)) {
        ResponseString("exists " + username)
      }
      else {
        ResponseString("ok " + username)
      }
    }
    case req@POST(Path("/checkemail") & Params(params)) => {
      val email = params("email")(0)
      if (Server.store.emailExists(email)) {
        ResponseString("exists " + email)
      }
      else {
        ResponseString("ok " + email) 
      }
    }
    case req@POST(Path("/login") & Params(params)) => {
      val login = params("login")(0)
      val password = params("password")(0)
      val user = Server.store.attemptLogin(login, password)
      if (user == null) {
        log.info(Server.realIp(req) + " FAILED LOGIN login: " + login)
        ResponseString("failed")
      }
      else {
        log.info(Server.realIp(req) + " LOGIN login: " + login)
        ResponseString(user.username + " " + user.session)
      } 
    }
  }
}