package com.graphbrain.braingenerators

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
    val startTime = profileMap.get("start_time")
    val endTime = profileMap.get("endTime")
    val location = profileMap.get("location")
    val privacy = profileMap.get("privacy")
    val updatedTime = profileMap.get("updatedTime")
    val owner = profileMap.get("owner")
    var venue = profileMap.get("venue")


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
    startTime match {
        case a:Some[Any] => println("start_time: " + a.get.asInstanceOf[String].toString)
        case None =>
    }
    endTime match {
        case a:Some[Any] => println("end_time: " + a.get.asInstanceOf[String].toString)
        case None =>
    }
    //TODO: Sort out location since different for events (just one) and pages (with longitude and lattitude)
    location match {
        case a:Some[Any] => try {println("location: " + a.get.asInstanceOf[String].toString)} catch {
            case e => if(venue==None) {venue = location}
        }
        case None =>
    }
    privacy match {
        case a:Some[Any] => println("privacy: " + a.get.asInstanceOf[String].toString)
        case None =>
    }
    updatedTime match {
        case a:Some[Any] => println("updated_time: " + a.get.asInstanceOf[String].toString)
        case None =>
    }

    owner match {
        case a:Some[Any] => val ownerDetails = a.get.asInstanceOf[Map[String, Any]];
          
            val ownerName:String = ownerDetails.get("name").get.asInstanceOf[String]
            println("Owner name: "+ ownerName)
            val ownerID:String = ownerDetails.get("id").get.asInstanceOf[String] 
            println("Owner id: " + ownerID)
            
          
        case None =>
    }
    
    venue match {
        case a:Some[Any] => val venueDetails = a.get.asInstanceOf[Map[String, Any]];
          try{val street:String = venueDetails.get("street").get.asInstanceOf[String]; 
          println("Street: " + street)}catch{case e =>}
          try{val city:String = venueDetails.get("city").get.asInstanceOf[String];
          println("City: " + city)}catch{case e =>}
          try{val state:String = venueDetails.get("state").get.asInstanceOf[String]
          println("State: " + state)}catch{case e =>}
          try{val country:String = venueDetails.get("country").get.asInstanceOf[String]; 
          println("Country: " + country)}catch{case e =>}
          try{val latitude:String = venueDetails.get("latitude").get.asInstanceOf[Double].toString; 
          println("Latitude: " + latitude)}catch{case e =>}
          try{val longitude:String = venueDetails.get("longitude").get.asInstanceOf[Double].toString
          println("Longitude: " + longitude)}catch{case e =>}
        case None =>
    }

		
  }

  

  def main(args : Array[String]) : Unit = {
    //User
  	FacebookGraphGenerator.generateBasic("chihchun.chen", "")
    //Page
  	FacebookGraphGenerator.generateBasic("cocacola", "")
    //Event:
    FacebookGraphGenerator.generateBasic("331218348435", "")
	
  }
}