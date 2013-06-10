package com.graphbrain.braingenerators

object ID {

  def sanitize(str: String): String = str.toLowerCase.replace("/", "_").replace(" ", "_")

  def url_id(url:String):String={
    val sanitized_url = url.toLowerCase.replace("/", "_")
    return "web/"+sanitized_url
  }

  def wikipedia_id(wptitle:String):String={
    val title = wptitle.toLowerCase.replace(" ", "_")
    return "wikipedia/"+title
  }

  def source_id(source:String):String={
	val sanitized_source=source.replace(" ", "_")
	return "source/"+source;	
  }

  def relation_id(relation:String):String={
	return relation.replace(" ", "_")
  }

  def image_id(image_name:String, image_url:String=""):String={
	return "image/" + image_name + "/" + image_url.toLowerCase.replace("/", "_");
  }

  def video_id(video_name:String, video_url:String=""):String={
	return "video/" + video_name + "/" + video_url.toLowerCase.replace("/", "_");
  }

  def text_id(thing:String, prefix:String):String={	
	return prefix+"/"+sanitize(thing);
  }

  def user_id(thingID:String, user:String): String = {
    return user + "/" + thingID
  }

  def reltype_id(relT:String, which:Int=1): String =
    "rtype/" + which.toString + "/" + sanitize(relT)


  def local_id(url:String):String={
	val sanitized_url = url.toLowerCase.replace("/", "_")
	return "localversion/"+sanitized_url;
  }

  def nounproject_ns(which: Int=1):String={
	return "svg/nounproject/" + which.toString
  }

  def usergenerated_ns(userID:String): String={
	return "usergenerated/" + userID
  }
}




