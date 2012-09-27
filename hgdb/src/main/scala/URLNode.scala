package com.graphbrain.hgdb


case class URLNode(url: String="", userId: String = "", title: String="") extends Vertex {
  val auxId = ID.urlId(url)

  override val id = if (userId == "") auxId else ID.globalToUser(url, userId)

  override def clone(newid: String) = this

  def setTitle(newTitle: String) = copy(title=newTitle)
}