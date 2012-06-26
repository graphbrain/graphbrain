package com.graphbrain.hgdb


object ID {

  def sanitize(str: String): String = str.toLowerCase.replace("/", "_").replace(" ", "_")

  def parts(id: String) = id.split('/').size

  def systemId(id: String) = parts(id) == 1


  def usergenerated_id(userName:String, thing:String) =
	  userName + "/" + thing

  def image_id(image_url:String) =
    "image/" + sanitize(image_url)
  
  def image_id(image_name:String, image_url:String="") =
	  "image/" + sanitize(image_name) + "/" + sanitize(image_url)

  def video_id(video_url:String) =
    "video/" +  sanitize(video_url)

  def video_id(video_name:String, video_url:String="") =
	  "video/" + sanitize(video_name) + "/" + sanitize(video_url)

  def text_id(thing:String):String =
	  sanitize(thing)

  def relation_id(relation:String):String =
	  sanitize(relation)

  def relation_id(rel:String, node1ID:String, node2ID:String):String=
  {
    val tokens=List[String](rel)++Array[String](node1ID, node2ID)
    return tokens.reduceLeft(_+ " " +_)
  }

  def reltype_id(relT:String): String =
    "rtype/" + sanitize(relT)

  def rule_id(rule_name: String): String =
    "rule/" + sanitize(rule_name) 


  def url_id(url:String):String =
    "web/"+sanitize(url)

  def wikipedia_id(wptitle:String):String={
    val title = wptitle.toLowerCase.replace(" ", "_")
    return "wikipedia/"+title
  }


}