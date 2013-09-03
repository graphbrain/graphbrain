package com.graphbrain.db

import java.net._
import org.jsoup.Jsoup
import com.typesafe.scalalogging.slf4j.Logging


case class URLNode(override val id: String,
                   title: String="",
                   icon: String="",
                   override val degree: Int = 0,
                   override val ts: Long = -1)
  extends Vertex(id, degree, ts) with Logging {

  val url = URLNode.idToUrl(id)

  def this(id: String, map: Map[String, String]) =
    this(id,
      map("title"),
      map("icon"),
      map("degree").toInt,
      map("ts").toLong)

  override def extraMap = Map("title" -> title,
                              "icon" -> icon)

  override def setId(newId: String): Vertex = copy(id=newId)

  override def setDegree(newDegree: Int): Vertex = copy(degree=newDegree)

  override def setTs(newTs: Long): Vertex = copy(ts=newTs)

  override def raw: String = {
    "type: " + "url<br />" +
    "url: " + url + "<br />" +
    "title: " + title + "<br />"
  }

  //override def shouldUpdate: Boolean = true
}


object URLNode {
  def fromUrl(url: String, userId: String = "") = {
    val auxId = urlToId(url)
    val id = if (userId == "") auxId else ID.globalToUser(auxId, userId)
    val titleAndIcon = getTitleAndIcon(url)
    URLNode(id, titleAndIcon._1, titleAndIcon._2)
  }

  def idToUrl(id: String) = URLDecoder.decode(ID.lastPart(id), "UTF-8")

  def urlToId(url: String) = "url/" + URLEncoder.encode(url, "UTF-8")

  private def getTitleAndIcon(url: String): (String, String) = {
    try {
      val doc = Jsoup.connect(url).header("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2").get()

      val title = doc.title()

      val links = doc.select("link[rel=shortcut icon]")
      val link = links.first()
      val icon = if (link != null) {
        val icoUrl = link.attr("abs:href")
        icoUrl
      }
      else {
        val icoUrl = "http://" + getDomainName(url) + "/favicon.ico"
        if (exists(icoUrl))
          icoUrl
        else
          ""
      }

      (title, icon)
    }
    catch {
      case e: Exception => {
        ("", "")
      }
    }
  }

  private def getDomainName(url: String) = {
    val uri = new URI(url)
    uri.getHost
  }

  private def exists(urlName: String): Boolean = {
    try {
      HttpURLConnection.setFollowRedirects(false)
      // note : you may also need
      //        HttpURLConnection.setInstanceFollowRedirects(false)
      val con = new URL(urlName).openConnection().asInstanceOf[HttpURLConnection]
      con.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2")
      con.setRequestMethod("HEAD")
      con.getResponseCode == HttpURLConnection.HTTP_OK
    }
    catch {
      case _: Throwable => false
    }
  }
}