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
	
	val registrationEmailFile = "RegistrationMessage.txt";
	val referralEmailFile = "ReferralMessage.txt";
	val registrationEmail = "Welcome #r_e_c_i_p_i_e_n_t,\n \n Your GraphBrain registration has been successful! You can now create as many brains as you like. Simply log in with your username: #u_s_e_r_n_a_m_e and password.\n \n As GraphBrain is still very much in its infancy, we would be grateful for any feedback or suggestions. E-mail us at: contact@graphbrain.com\n \n Happy brain-building,\n \n The GraphBrain team\n \n GraphBrain: a beautiful way to organise, explore and share information";
	val referralEmail = "Hello #r_e_c_i_p_i_e_n_t, \n \n #s_e_n_d_e_r would like to invite you to use GraphBrain, a beautiful way to organize and explore information. To find out more and register, simply go to http://graphbrain.com and sign up! \n \n Looking forward to having you in GraphBrain,\n \n The GraphBrain team";

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

	//For marketing campaigns:
	def sendEmails(recipientEmails: List[String], subject:String, messageBaseText: String, from:String = "contact@graphbrain.com",defaultRecipientName: String = "", defaultSenderName: String = "", recipientNames: List[String]=Nil, senderNames: List[String] = Nil, userNames: List[String] = Nil): Boolean = {
		var success = 0;
		
		for(i <- 0 to recipientEmails.length-1) {
			var messageText = messageBaseText;
			if(recipientNames != Nil) {
				messageText = messageText.replace("#r_e_c_i_p_i_e_n_t", recipientNames(i))
			}
			else {
				messageText = messageText.replace("#r_e_c_i_p_i_e_n_t", defaultRecipientName)	
			}
			if(senderNames != Nil) {
				messageText = messageText.replace("#s_e_n_d_e_r", senderNames(i))
			}
			else {
				messageText = messageText.replace("#s_e_n_d_e_r", defaultSenderName)
			}
			if(userNames != Nil) {
				messageText = messageText.replace("#u_s_e_r_n_a_m_e", userNames(i))
			}

			if(sendEmail(recipientEmails(i), subject, messageText, from)) {success +=1}
		}
		println("Emails attempted: " + recipientEmails.length.toString)
		println("Successful attempts: " + success.toString)
		return true;
	}

	def sendRegistrationEmail(recipientEmails: List[String], recipientNames: List[String], userNames: List[String]): Boolean = {

		return sendEmails(recipientEmails, subject = "Welcome to GraphBrain", messageBaseText = registrationEmail, defaultRecipientName = "", defaultSenderName = "", recipientNames = recipientNames, userNames = userNames)

	}

	def sendReferralEmail(recipientEmails: List[String], recipientNames: List[String], senderNames: List[String]): Boolean = {
		return sendEmails(recipientEmails, subject = "Invitation to GraphBrain", messageBaseText = referralEmail, defaultRecipientName = "", defaultSenderName = "", recipientNames = recipientNames, senderNames = senderNames)
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
      //m.sendEmail("chihchun_chen@yahoo.co.uk", "testing from scala code", "If this gets to you, it's working. \n")
      
      val recipients = List("chihchun_chen@yahoo.co.uk")
      val recipientNames = List("Aliana Hepwood")
      val userNames = List("chihchun_chen")
      val senderNames = List("Ch-Ch")
      val message = fromFile("TestMessage.txt").mkString
      //m.sendEmails(recipients, "testing", message, "contact@graphbrain.com", recipientNames, senderNames)
      m.sendRegistrationEmail(recipients, recipientNames, userNames);
      m.sendReferralEmail(recipients, recipientNames, senderNames);
      
        
    }

}
