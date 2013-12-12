package com.graphbrain.webapp

import unfiltered.request._

import com.graphbrain.db.UserNode
import scala.collection.JavaConversions._


case class AllUsersPage(user: UserNode, req: HttpRequest[Any], cookies: Map[String, Any]) {
  var html = "<h2>All Users</h2>"

  val users = WebServer.graph.allUsers

  html += "<strong>Count:" + users.size + "</strong><br /><br />"

  for (u <- users)
    html += "<a href='/node/user/" + u.getUsername + "'>" + u.getUsername + "</a> " + u.getName + " " + u.getEmail + " " + u.getPwdhash + "<br />"

  def response = WebServer.scalateResponse("raw.ssp", "allusers", "All Users", cookies, req, html=html)
}


object AllUsersPage {

}
