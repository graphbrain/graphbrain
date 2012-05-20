package com.graphbrain.braingenerators

object ID {

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
	return prefix+"/"+thing;
  }

  def local_id(url:String):String={
	val sanitized_url = url.toLowerCase.replace("/", "_")
	return "localversion/"+sanitized_url;
  }

  def nounproject_id(noun:String):String={
	return "nounproject/"+noun;
  }

  def usergenerated_id(userID:String, thing:String): String={
	return "usergenerated/" + userID + "/" + thing.replace("\"", "").replace("`", "").replace("'", "").trim.replace(" ", "_");
  }
}




