

object Formatting {

  val ListRegex = """List(s?)\sof\s""".r
  val WikiLinkRegex="""\[\[([^\]]*)\]\]""".r
  val CategoryRegex="""category\:.*""".r
  val IrregularTitleRegex=""".*\:.*""".r
	
	def normalizeWikiTitle(rawTitle:String):String={
   		val title=rawTitle.trim.stripLineEnd
    	
    	var norm_title="";
    	if(title.length>0) {norm_title+=(title(0).toString().toUpperCase)}
    
    	if(title.length>1){
      		norm_title+=title.substring(1, title.length)
    	}
    	norm_title=norm_title.replace("_", " ")
      
    	return norm_title
  	}

  	def wikiLink(title:String):String ={
  		return title.replace(" ", "_").toLowerCase
  	}

    def isCategory(titleString:String):Boolean=
  {
    return CategoryRegex.findAllIn(titleString).hasNext
  }
  def isList(titleString:String):Boolean=
  {
    return ListRegex.findAllIn(titleString).hasNext

  }
  def isIrregular(titleString:String):Boolean=
  {
    return IrregularTitleRegex.findAllIn(titleString).hasNext
  }

  def separateWords(stringToSep:String):String=
  {
     
     val wordRegex="""[a-z]+|[A-Z][a-z]+""".r
     val words=wordRegex.findAllIn(stringToSep)
     
     var returnString=""
     while(words.hasNext)
     {
       returnString+=" " + words.next.toLowerCase.trim;
       
     }
     return returnString;
  }

  
}