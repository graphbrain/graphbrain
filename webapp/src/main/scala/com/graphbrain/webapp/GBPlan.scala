package com.graphbrain.webapp

import unfiltered.request._
import unfiltered.response._
import unfiltered.netty._
import unfiltered.Cookie

import com.graphbrain.db.Edge
import com.graphbrain.db.SearchInterface
import com.graphbrain.utils.JSONGen


object GBPlan extends cycle.Plan with cycle.SynchronousExecution with ServerErrorResponse {
  def intent = {
    case req@POST(Path("/search") & Params(params) & Cookies(cookies)) => {
      val query = params("q")(0)
      val si = new SearchInterface(WebServer.graph)
      val results = si.query(query)
      
      val resultsList: Seq[List[String]] = for (id <- results)
      yield List(id, WebServer.graph.description(id))
      
      val json = Map("count" -> results.size, "results" -> resultsList)
      
      WebServer.log(req, cookies, "SEARCH query: " + query + "; results: " + results.size)

      ResponseString(JSONGen.json(json))
    }
    case req@POST(Path("/signup") & Params(params)) => {
      val name = params("name")(0)
      val username = params("username")(0)
      val email = params("email")(0)
      val password = params("password")(0)
      WebServer.graph.createUser(username, name, email, password, "user")

      WebServer.log(req, null, "SIGNUP name: " + name + "; username: " + username + "; email:" + email)
      
      ResponseString("ok")
    }
    case req@POST(Path("/checkusername") & Params(params)) => {
      val username = params("username")(0)
      if (WebServer.graph.usernameExists(username)) {
        ResponseString("exists " + username)
      }
      else {
        ResponseString("ok " + username)
      }
    }
    case req@POST(Path("/checkemail") & Params(params)) => {
      val email = params("email")(0)
      if (WebServer.graph.emailExists(email)) {
        ResponseString("exists " + email)
      }
      else {
        ResponseString("ok " + email) 
      }
    }
    case req@POST(Path("/login") & Params(params)) => {
      val login = params("login")(0)
      val password = params("password")(0)
      val user = WebServer.graph.attemptLogin(login, password)
      if (user == null) {
        WebServer.log(req, null, "FAILED LOGIN login: " + login + " passwd: " + password)
        ResponseString("failed")
      }
      else {
        WebServer.log(req, null, "LOGIN login: " + login)
        ResponseString(user.username + " " + user.session)
      } 
    }
    case req@POST(Path("/undo_fact") & Params(params) & Cookies(cookies)) => {
      val userNode = WebServer.getUser(cookies)

      val rel = params("rel")(0)
      val participants = params("participants")(0)

      val participantIds = participants.split(" ")

      // undo connection
      WebServer.graph.remove(Edge.fromParticipants(rel, participantIds), userNode.id)
      // force consesnsus re-evaluation of affected edge
      val edge = Edge.fromParticipants(rel, participantIds)
      WebServer.consensusActor ! edge

      ResponseString(JSONGen.json(""))
    }
    case req@GET(Path("/allusers") & Cookies(cookies)) => {
      val userNode = WebServer.getUser(cookies)
      AllUsersPage(userNode, req, cookies).response
    }

    // TEMPORARY: Amazon backdoor
    case req@GET(Path("/amazon") & Params(params)) => {
      val login = "amazon"
      val user = WebServer.graph.forceLogin(login)

      WebServer.log(req, null, "AMAZON access ")
      
      ResponseCookies(Cookie("username", user.username)) ~> ResponseCookies(Cookie("session", user.session)) ~> Redirect("/node/user/amazon") 
    }
  }
}
