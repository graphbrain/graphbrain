package com.graphbrain.braingenerators

object ID{

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

def image_id(image_name:String):String={
	return "image/"+image_name;
}

def text_id(thing:String, prefix:String):String={
	
	return prefix+"/"+thing;
}

def local_id(url:String):String={
	val sanitized_url = url.toLowerCase.replace("/", "_")
	return "localversion/"+sanitized_url;
}
}




