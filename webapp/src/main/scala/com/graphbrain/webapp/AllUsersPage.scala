package com.graphbrain.webapp


import unfiltered.scalate._
import unfiltered.request._
import unfiltered.response._

import com.graphbrain.hgdb.VertexStore
import com.graphbrain.hgdb.Vertex
import com.graphbrain.hgdb.Edge
import com.graphbrain.hgdb.UserNode


case class AllUsersPage(user: UserNode, req: HttpRequest[Any], cookies: Map[String, Any]) {
  var html = "<h2>All Users</h2>"

  val users = Server.store.allUsers

  html += "<strong>Count:" + users.size + "</strong><br /><br />"

  for (u <- users)
    html += "<a href='/node/user/" + u.username + "'>" + u.username + "</a> " + u.name + " " + u.email + "<br />"

  def response = Server.scalateResponse("raw.ssp", "allusers", "All Users", cookies, req, html=html)
}


object AllUsersPage {

}
