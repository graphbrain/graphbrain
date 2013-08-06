package com.graphbrain.gbdb


import java.net._
import java.io._

import org.jsoup.Jsoup
import org.jsoup.helper.Validate
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

import com.graphbrain.utils.SimpleLog


case class URLNode(store: VertexStore, url: String="", userId: String = "", title: String="", icon: String="") extends Vertex with SimpleLog{
  val auxId = ID.urlId(url)

  override val id = if (userId == "") auxId else ID.globalToUser(auxId, userId)

  private def exists(urlName: String): Boolean = {
    try {
      HttpURLConnection.setFollowRedirects(false)
      // note : you may also need
      //        HttpURLConnection.setInstanceFollowRedirects(false)
      val con = new URL(urlName).openConnection().asInstanceOf[HttpURLConnection]
      con.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2")
      con.setRequestMethod("HEAD")
      con.getResponseCode() == HttpURLConnection.HTTP_OK
    }
    catch {
       case _ => false
    }
  }

  private def getDomainName = {
    val uri = new URI(url)
    uri.getHost()
  }

  private def getTitleAndIcon: (String, String) = {
    ldebug("getTitleAndIcon " + url)

    try {
      val doc = Jsoup.connect(url).header("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2").get()
    
      val title = doc.title()
      ldebug("title: " + title)
    
      val links = doc.select("link[rel=shortcut icon]")
      val link = links.first()
      val icon = if (link != null) {
        val icoUrl = link.attr("abs:href")
        icoUrl
      }
      else {
        val icoUrl = "http://" + getDomainName + "/favicon.ico"
        ldebug("default icon url: " + icoUrl)
        if (exists(icoUrl))
          icoUrl
        else
          ""
      }

      ldebug("icon: " + icon)

      (title, icon)
    }
    catch {
      case e: Exception => {
        ldebug(e.toString)
        ("", "")
      }
    }
  }

  override def put(): Vertex = {
    ldebug("put " + id)
    ldebug("in userspace? " + ID.isInUserSpace(id))
    val template = if (ID.isInUserSpace(id)) store.backend.tpUserSpace else store.backend.tpGlobal
    val updater = template.createUpdater(id)
    updater.setString("url", url)

    val titleAndIcon = getTitleAndIcon
    updater.setString("title", titleAndIcon._1)
    updater.setString("icon", titleAndIcon._2)
    template.update(updater)
    store.onPut(this)
    this
  }

  override def clone(newid: String) = URLNode(store, newid, userId, title)

  override def toGlobal: Vertex = URLNode(store, url, "", title)

  override def toUser(newUserId: String): Vertex = URLNode(store, url, newUserId, title)

  def setTitle(newTitle: String) = copy(title=newTitle)

  override def raw: String = {
    "type: " + "url<br />" +
    "url: " + url + "<br />" +
    "title: " + title + "<br />"
  }

  override def shouldUpdate: Boolean = true
}


object URLNode {
  def main(args : Array[String]) : Unit = {
    val urlNode = new URLNode(null, "http://graphbrain.com/secret")
    urlNode.getTitleAndIcon
  }
}