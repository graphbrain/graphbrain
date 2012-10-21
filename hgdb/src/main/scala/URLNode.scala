package com.graphbrain.hgdb

import java.net.URL
import java.util.regex.Matcher
import java.util.regex.Pattern
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.client.methods.HttpGet
import java.io.Reader
import java.io.InputStreamReader
import java.io.IOException


case class URLNode(store: VertexStore, url: String="", userId: String = "", title: String="") extends Vertex {
  val auxId = ID.urlId(url)

  override val id = if (userId == "") auxId else ID.globalToUser(url, userId)

  private def getTitle(urlStr: String): String = {
    val url = new URL(urlStr)

    val client = new DefaultHttpClient()
    val request = new HttpGet(url.toURI())
    val response = client.execute(request)

    var reader: Reader = null
    var html = ""
    try {
      reader = new InputStreamReader(response.getEntity().getContent())

      val sb = new StringBuffer()
      val cbuf = new Array[Char](1024)
      var read = reader.read(cbuf)
      while (read != -1) {
        sb.append(cbuf, 0, read)
        read = reader.read(cbuf)
      }

      html = sb.toString()
    }
    finally {
      if (reader != null) {
        try {
          reader.close()
        }
        catch {
          case e: IOException => e.printStackTrace()
        }
      }
    }

    html = html.replaceAll("\\s+", " ")
    val p: Pattern = Pattern.compile("<title>(.*?)</title>")
    val m: Matcher = p.matcher(html)
    
    if (m.find()) {
      m.group(1)
    }
    else {
      ""
    }
  }

  override def put(): Vertex = {
    val template = if (ID.isInUserSpace(id)) store.backend.tpUserSpace else store.backend.tpGlobal
    val updater = template.createUpdater(id)
    updater.setString("url", url)
    val newTitle = if (title == "") getTitle(url) else title
    updater.setString("title", newTitle)
    template.update(updater)
    store.onPut(this)
    this
  }

  override def clone(newid: String) = this

  def setTitle(newTitle: String) = copy(title=newTitle)

  override def raw: String = {
    "type: " + "url<br />" +
    "url: " + url + "<br />" +
    "title: " + title + "<br />"
  }
}