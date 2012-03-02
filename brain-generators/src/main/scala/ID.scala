
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
	
	return "source/"+source;	
}

}




