package com.graphbrain.hgdb


case class URLNode(store: VertexStore, url: String="", userId: String = "", title: String="") extends Vertex {
  val auxId = ID.urlId(url)

  override val id = if (userId == "") auxId else ID.globalToUser(url, userId)

  override def put(): Vertex = {
    val template = if (ID.isInUserSpace(id)) store.backend.tpUserSpace else store.backend.tpGlobal
    val updater = template.createUpdater(id)
    updater.setString("url", url)
    updater.setString("title", title)
    template.update(updater)
    this
  }

  override def clone(newid: String) = this

  def setTitle(newTitle: String) = copy(title=newTitle)
}