package com.graphbrain.braingenerators

object Formatting {

  val ListRegex = """List(s?)\sof\s""".r
  val WikiLinkRegex="""\[\[([^\]]*)\]\]""".r
  val CategoryRegex="""category\:.*""".r
  val IrregularTitleRegex=""".*\:.*""".r
	
	def normalizeWikiTitle(rawTitle:String):String={
   		val title=rawTitle.trim.stripLineEnd
    	
    	var norm_title = ""
    	if (title.length > 0) {
        norm_title += title(0).toString.toUpperCase
      }
    
    	if (title.length > 1) {
        norm_title += title.substring(1, title.length)
    	}
    	norm_title = norm_title.replace("_", " ")
      
    	norm_title
  }

  def stripLink(link:String):String =
    link.replaceAll("""(\[\[)""", "").replaceAll("""\]\]""", "")

  def wikiLink(title:String):String =
    title.replace(" ", "_")

  def isCategory(titleString:String):Boolean =
    CategoryRegex.findAllIn(titleString).hasNext

  def isList(titleString:String):Boolean =
    ListRegex.findAllIn(titleString).hasNext

  def isIrregular(titleString:String):Boolean =
    IrregularTitleRegex.findAllIn(titleString).hasNext

  def separateWords(stringToSep:String):String = {
     val wordRegex="""[a-z]+|[A-Z][a-z]+""".r
     val words=wordRegex.findAllIn(stringToSep)
     
     var returnString=""
     while(words.hasNext) {
       returnString += " " + words.next().toLowerCase.trim
     }
     returnString
  }

  
}