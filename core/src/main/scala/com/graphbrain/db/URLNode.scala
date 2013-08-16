package com.graphbrain.db

import java.net._
import org.jsoup.Jsoup
import com.graphbrain.utils.SimpleLog


case class URLNode(override val id: String,
                   title: String="",
                   icon: String="",
                   override val degree: Int = 0,
                   override val ts: Long = -1)
  extends Vertex(id, degree, ts) with SimpleLog {

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

  private def getDomainName = {
    val uri = new URI(url)
    uri.getHost
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

  override def raw: String = {
    "type: " + "url<br />" +
    "url: " + url + "<br />" +
    "title: " + title + "<br />"
  }

  //override def shouldUpdate: Boolean = true
}


object URLNode {
  def fromUrl(url: String, userId: String = "", title: String="", icon: String="") = {
    val auxId = urlToId(url)
    val id = if (userId == "") auxId else ID.globalToUser(auxId, userId)
    URLNode(id, title, icon)
  }

  def idToUrl(id: String) = URLDecoder.decode(ID.lastPart(id), "UTF-8")

  def urlToId(url: String) = "url/" + URLEncoder.encode(url, "UTF-8")
}