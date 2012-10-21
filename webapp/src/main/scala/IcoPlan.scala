package com.graphbrain.webapp

import java.net._
import java.io._

import unfiltered.request._
import unfiltered.response._
import unfiltered.netty._
import unfiltered.Cookie

import net.sf.image4j.codec.ico.ICODecoder
import javax.imageio.ImageIO


object IcoPlan extends cycle.Plan with cycle.SynchronousExecution with ServerErrorResponse {
  def intent = {
    case req@GET(Path(Seg("favicon" :: domain :: Nil))) => {
      val url = new URL("http://www.oracle.com/")
      val in = url.openStream()

      val image = ICODecoder.read(in)

      val fileName = "/var/www/favicons/" + domain
      ImageIO.write(image.get(0), "png", new File(fileName))

      Redirect("http://graphbrain.com/favicons/" + domain)
    }
  }
}
