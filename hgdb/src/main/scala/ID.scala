package com.graphbrain.hgdb


object ID {

  def sanitize(str: String): String = str.toLowerCase.replace("/", "_").replace(" ", "_")

  def brainId(name: String, userName: String) =
    "brain/" + userName + "/" + sanitize(name)

   def usergenerated_id(userName:String, thing:String, brainName:String)=
	brainId(brainName, userName) + "/" + thing

   def image_id(image_url:String)=
    "image/" + sanitize(image_url)
   def image_id(image_name:String, image_url:String="")=
	"image/" + sanitize(image_name) + "/" + sanitize(image_url)
  

   def video_id(video_url:String)=
    "video/" +  sanitize(video_url)

   def video_id(video_name:String, video_url:String="")=
	"video/" + sanitize(video_name) + "/" + sanitize(video_url)

  

   def text_id(thing:String):String=
	sanitize(thing)

   def relation_id(relation:String):String=
	sanitize(relation)

   def url_id(url:String):String=
    "web/"+sanitize(url)
  
  
  
  
}