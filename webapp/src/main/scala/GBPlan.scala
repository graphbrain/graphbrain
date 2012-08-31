package com.graphbrain.webapp

import unfiltered.request._
import unfiltered.response._
import unfiltered.netty._
import unfiltered.Cookie

import com.graphbrain.hgdb.SearchInterface


object GBPlan extends cycle.Plan with cycle.SynchronousExecution with ServerErrorResponse {
  def intent = {
    case req@POST(Path("/search") & Params(params)) => {
      val query = params("q")(0)
      val si = new SearchInterface(Server.store)
      val results = si.query(query)
      //log.info(Server.realIp(req) + " SEARCH " + query + "; results: " + results.size)
      
      val resultsList: Seq[List[String]] = (for (id <- results)
        yield List(id, Server.store.get(id).toString))
      
      val json = Map(("count" -> results.size), ("results" -> resultsList))
      ResponseString(JSONGen.json(json))
    }
    case req@POST(Path("/signup") & Params(params)) => {
      val name = params("name")(0)
      val username = params("username")(0)
      val email = params("email")(0)
      val password = params("password")(0)
      Server.store.createUser(username, name, email, password, "user")
      //log.info(Server.realIp(req) + " SIGNUP name: " + name + "; username: " + username + "; email:" + email)
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
        //log.info(Server.realIp(req) + " FAILED LOGIN login: " + login)
        ResponseString("failed")
      }
      else {
        //log.info(Server.realIp(req) + " LOGIN login: " + login)
        ResponseString(user.username + " " + user.session)
      } 
    }
  }
}
