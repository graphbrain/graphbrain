import scala.util.parsing.json._
import java.net.URL
import scala.io.Source
import scala.util.matching.Regex
import scala.xml._
import java.io.BufferedReader;
import java.io.InputStreamReader
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.HashMap

object FacebookGraphGenerator {
  val fbGraphBaseURL = "https://graph.facebook.com/"

  val sourceName = "facebook/user"

  def generateBasic(userName:String, authCode:String):Unit={
  	val url = fbGraphBaseURL + userName + "?access_token=" + authCode
    val profileDetails = Web.getPage(url);
    
    //Get map of details from JSON
    val jsonProfile:Option[Any] = JSON.parseFull(profileDetails)
    val profileMap:Map[String, Any] = jsonProfile.get.asInstanceOf[Map[String, Any]] 
    println(profileMap.get("name"))
    val name = profileMap.get("name")
    val firstName = profileMap.get("first_name")
    val lastName = profileMap.get("last_name")
    val username = profileMap.get("username")
    val gender = profileMap.get("gender")
    val locale = profileMap.get("locale")
    val pictureURL = profileMap.get("picture")
    val link = profileMap.get("link")
    val likes = profileMap.get("likes")
    val category = profileMap.get("category")
    val foundedDate = profileMap.get("founded")
    val descriptionText = profileMap.get("description")
    val aboutPageText = profileMap.get("about")
    val checkIns = profileMap.get("checkins")
    val talkingAboutCount = profileMap.get("talking_about_count")

    name match {
    	case a:Some[Any] => println("name: " + a.get.asInstanceOf[String].toString)
    	case None =>
    	
    }
    username match {
    	case a:Some[Any] => println("username: " + a.get.asInstanceOf[String].toString)
    	case None =>
    }
    pictureURL match {
    	case a:Some[Any] => println("picture: " + a.get.asInstanceOf[String].toString)	
    	case None =>
    }
	link match {
		case a:Some[Any] => println("link: " + a.get.asInstanceOf[String].toString)
		case None =>
	}
    likes match {
    	case a:Some[Any] => println("likes: " + a.get.asInstanceOf[Double].toString)
    	case None =>
    }
    category match {
    	case a:Some[Any] => println("category: " + a.get.asInstanceOf[String].toString)
    	case None =>
    }
    foundedDate match {
    	case a:Some[Any] => println("founded date: " + a.get.asInstanceOf[String].toString)
    	case None =>
    }
    descriptionText match {
    	case a:Some[Any] => println("description: " + a.get.asInstanceOf[String].toString)
    	case None =>
    }
    aboutPageText match {
    	case a:Some[Any] => println("about page: " + a.get.asInstanceOf[String].toString)
    	case None =>
    }
    checkIns match {
    	case a:Some[Any] => println("check ins: " + a.get.asInstanceOf[Double].toString)	
    	case None =>
    }
    talkingAboutCount match {
    	case a:Some[Any] => println("talking about count: " + a.get.asInstanceOf[Double].toString)
    	case None =>
    }


    /*println("name: " + name)
    println("first name: " + firstName)
    println("last name: " + lastName)
    println("gender: " + gender)
    println("locale: " + locale)
	println("username: " + username)
    println("picture: " + pictureURL)
    println("link: " + link)
    println("likes: " + likes)
    println("category: " + category)
    println("founded date: " + foundedDate)
    println("description: " + descriptionText)
    println("about page: " + aboutPageText)
    println("check ins: " + checkIns)
    println("talking about count: " + talkingAboutCount)*/


		
  }

  def generatePageBasic(pageName:String, authCode:String):Unit={
  	val url = fbGraphBaseURL + pageName + "?access_token=" + authCode
    val pageDetails = Web.getPage(url);
    
    //Get map of details from JSON
    val jsonProfile:Option[Any] = JSON.parseFull(pageDetails)
    val profileMap:Map[String, Any] = jsonProfile.get.asInstanceOf[Map[String, Any]] 

    val name = profileMap.get("name").get.asInstanceOf[String]
    val username = profileMap.get("username").get.asInstanceOf[String]
    val pictureURL = profileMap.get("picture").get.asInstanceOf[String]
    val link = profileMap.get("link").get.asInstanceOf[String]
    val likes = profileMap.get("likes").get.asInstanceOf[String]
    val category = profileMap.get("category").get.asInstanceOf[String]
    val foundedDate = profileMap.get("founded").get.asInstanceOf[String]
    val descriptionText = profileMap.get("description").get.asInstanceOf[String]
    val aboutPageText = profileMap.get("about").get.asInstanceOf[String]
    val checkIns = profileMap.get("checkins").get.asInstanceOf[String]
    val talkingAboutCount = profileMap.get("talking_about_count").get.asInstanceOf[String]

    name match {
    	case a:String => println("name: " + name)
    }
    username match {
    	case a:String => println("username: " + username)
    }
    pictureURL match {
    	case a:String => println("picture: " + pictureURL)	
    }
	link match {
		case a:String => println("link: " + link)
	}
    likes match {
    	case a:String => println("likes: " + likes)
    }
    category match {
    	case a:String => println("category: " + category)
    }
    foundedDate match {
    	case a:String => println("founded date: " + foundedDate)
    }
    descriptionText match {
    	case a:String => println("description: " + descriptionText)
    }
    aboutPageText match {
    	case a:String => println("about page: " + aboutPageText)
    }
    checkIns match {
    	case a:String => println("check ins: " + checkIns)	
    }
    talkingAboutCount match {
    	case a:String => println("talking about count: " + talkingAboutCount)
    }
		
  }

  

  def main(args : Array[String]) : Unit = {
  	FacebookGraphGenerator.generateBasic("chihchun.chen", "")
  	FacebookGraphGenerator.generateBasic("cocacola", "")
	
  }
}