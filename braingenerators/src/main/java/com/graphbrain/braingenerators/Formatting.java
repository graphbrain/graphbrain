package com.graphbrain.braingenerators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Formatting {

    private static Pattern listRegex = Pattern.compile("List(s?)\\sof\\s");
    private static Pattern wordRegex = Pattern.compile("[a-z]+|[A-Z][a-z]+");
    //val WikiLinkRegex="""\[\[([^\]]*)\]\]""".r
    //val CategoryRegex="""category\:.*""".r
    //val IrregularTitleRegex=""".*\:.*""".r
	
	public static String normalizeWikiTitle(String rawTitle) {
   		String title = rawTitle.trim();
    	
    	String norm_title = "";
    	if (title.length() > 0) {
            norm_title += ("" + title.charAt(0)).toUpperCase();
        }
    
    	if (title.length() > 1) {
            norm_title += title.substring(1, title.length());
    	}
    	norm_title = norm_title.replace("_", " ");
      
    	return norm_title;
    }

    public static boolean isList(String titleString) {
        return listRegex.matcher(titleString).find();
    }

    public static String separateWords(String stringToSep) {
        Matcher words = wordRegex.matcher(stringToSep);
        //val words = wordRegex.findAllIn(stringToSep)

        String returnString = "";
        while(words.find()) {
            returnString += " " + words.group().toLowerCase().trim();
        }
        return returnString;
    }

    /*
  def stripLink(link:String):String =
    link.replaceAll("""(\[\[)""", "").replaceAll("""\]\]""", "")

  def wikiLink(title:String):String =
    title.replace(" ", "_")

  def isCategory(titleString:String):Boolean =
    CategoryRegex.findAllIn(titleString).hasNext



  def isIrregular(titleString:String):Boolean =
    IrregularTitleRegex.findAllIn(titleString).hasNext
  */
  
}