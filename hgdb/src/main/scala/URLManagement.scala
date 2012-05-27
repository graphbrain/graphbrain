package com.graphbrain.hgdb


import dispatch._
import dispatch.tagsoup.TagSoupHttp._

import java.io.DataInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

trait URLManagement extends VertexStoreInterface {

  def getTitle(urlStr: String): String = {
    val url: URL = new URL(urlStr)
    val urlConnection: URLConnection = url.openConnection()
    val dis: DataInputStream = new DataInputStream(urlConnection.getInputStream())
    var html = ""
    var tmp = dis.readUTF()
    while (tmp != null) {
      html += " " + tmp
      try {
        tmp = dis.readUTF()
      }
      catch {
        case _ => tmp = null
      }
    }
    dis.close()

    //var html: String = Http(url(urlStr) as_str)

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

  abstract override def put(vertex: Vertex): Vertex = {
    vertex match {
      case u: URLNode => {
        val title = getTitle(u.url)
        //println("url: " + u.url)
        //println("title: " + title)
        super.put(u.setTitle(title))
      }
      case v => super.put(v)
    }
  } 
}