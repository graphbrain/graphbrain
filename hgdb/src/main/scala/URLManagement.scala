package com.graphbrain.hgdb


import dispatch._
import dispatch.tagsoup.TagSoupHttp._

import java.net.URL
import java.util.regex.Matcher
import java.util.regex.Pattern
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.client.methods.HttpGet
import java.io.Reader
import java.io.InputStreamReader
import java.io.IOException


trait URLManagement extends VertexStoreInterface {

  def getTitle(urlStr: String): String = {
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