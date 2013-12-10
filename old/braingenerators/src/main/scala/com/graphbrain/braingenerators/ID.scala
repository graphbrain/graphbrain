package com.graphbrain.braingenerators

object ID {

  def sanitize(str: String): String = str.toLowerCase.replace("/", "_").replace(" ", "_")

  def url_id(url:String):String = {
    val sanitized_url = url.toLowerCase.replace("/", "_")
    "web/" + sanitized_url
  }

  def wikipedia_id(wptitle:String):String = {
    val title = wptitle.toLowerCase.replace(" ", "_")
    "wikipedia/" + title
  }

  def source_id(source:String):String =
	  "source/" + source

  def relation_id(relation:String):String =
	  relation.replace(" ", "_")

  def image_id(image_name:String, image_url:String=""):String =
	  "image/" + image_name + "/" + image_url.toLowerCase.replace("/", "_")

  def video_id(video_name:String, video_url:String=""):String =
	  "video/" + video_name + "/" + video_url.toLowerCase.replace("/", "_")

  def text_id(thing:String, prefix:String):String =
	  prefix + "/" + sanitize(thing)

  def user_id(thingID:String, user:String): String =
    user + "/" + thingID

  def reltype_id(relT:String, which:Int=1): String =
    "r/" + which.toString + "/" + sanitize(relT)

  def local_id(url:String):String = {
	  val sanitized_url = url.toLowerCase.replace("/", "_")
	  "localversion/"+sanitized_url
  }

  def nounproject_ns(which: Int=1):String =
	  "svg/nounproject/" + which.toString

  def usergenerated_ns(userID:String): String =
	  "usergenerated/" + userID
}




