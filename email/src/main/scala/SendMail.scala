package com.graphbrain.email
import java.lang._

import scala.io.Source._

import java.util.Properties;
 
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


class SendMail (uname:String, pwd:String, apiKey:String) {
	


	def sendEmail(recipient: String, subject: String, messageText: String, from:String = "contact@graphbrain.com"):Boolean= {

		val props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.critsend.com");
		props.put("mail.smtp.port", "587");
 

		val session = Session.getInstance(props,
		  new javax.mail.Authenticator() {
			override def getPasswordAuthentication(): PasswordAuthentication ={
				return new PasswordAuthentication(uname, pwd);
			}
		  });
		try {
 
			val message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			message.setRecipients(Message.RecipientType.TO, recipient);
			message.setSubject(subject);
			message.setText(messageText);
 
			Transport.send(message);
 
			System.out.println("Done");
			return true;
 
		} catch  {
			case e => println(e); return false;

		}
	}
}

object SendMail {


    def main(args: Array[String]) {
      val params = fromFile("APIKey.txt").getLines
      val uname = try{params.next}catch{case e => ""}
      val pwd = try{params.next}catch{case e => ""}
      val apiKey = try{params.next}catch{case e => ""}

      println("UserName: " + uname)
      println("Pwd: " + pwd)	   
      println("APIKey: " + apiKey)
      val m = new SendMail(uname, pwd, apiKey);
      m.sendEmail("telmo@telmomenezes", "testing from scala code", "If this gets to you, it's working. \n")
      
      
      
        
    }

}
