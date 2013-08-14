package com.graphbrain.webapp

import unfiltered.request._

import com.graphbrain.db.UserNode


case class AllUsersPage(user: UserNode, req: HttpRequest[Any], cookies: Map[String, Any]) {
  var html = "<h2>All Users</h2>"

  val users = WebServer.graph.allUsers

  html += "<strong>Count:" + users.size + "</strong><br /><br />"

  for (u <- users)
    html += "<a href='/node/user/" + u.username + "'>" + u.username + "</a> " + u.name + " " + u.email + " " + u.pwdhash + "<br />"

  def response = WebServer.scalateResponse("raw.ssp", "allusers", "All Users", cookies, req, html=html)
}


object AllUsersPage {

}
