import net.mxm.connector.*;
import java.lang.*;



import java.util.logging.Level;
import java.util.logging.Logger;

import net.mxm.connector.ArrayEmail;
import net.mxm.connector.CampaignParameters;
import net.mxm.connector.Content;
import net.mxm.connector.Email;
import net.mxm.connector.MxmConnect;
import net.mxm.connector.MxmConnectConfigurationException;
import net.mxm.connector.MxmConnectException;



public class SendMailJ  {
	
	public String uname;
	public String pwd;
	public String apiKey;
	
	public SendMailJ(String u, String p, String a) {
		uname = u;
		pwd = p;
		apiKey = a;
	}

	public String makeRandom() {
		Double r = new Double(Math.random() * 1000000);
		return r.toString();
	}

	public void sendEmail() throws MxmConnectConfigurationException, MxmConnectException  {

		MxmConnect mxm2 = new MxmConnect(uname, pwd);
		//MxmConnect mxm2 = mxm ;
		mxm2.setFastDelivery(true);
		CampaignParameters cp = new CampaignParameters();
		cp.setMailFrom("contact@graphbrain.com");
		cp.setMailFromFriendly("GraphBrain");
		cp.setReplyTo(makeRandom());
		cp.setReplyToFiltered(false);
		String tags[] = { "test" };
		cp.setTags(tags);

		Content c = new Content();
		

		c.setSubject("Your Subject");
		c.setText("test");

		StringBuffer sb = new java.lang.StringBuffer();

		String r = this.makeRandom();

		for (int i=0; i<2000; i++) {

			sb.append(r);
		}

		c.setHtml(sb.toString());

		Email e = new Email();
		e.setEmail("chihchun_chen@yahoo.co.uk");

		ArrayEmail ae = new ArrayEmail();
		ae.addEmail(e);

		String host = mxm2.getHost();
		System.out.println("Host: " + host);
		System.out.println("Content: " + c.toString());
		System.out.println("Campaign: " + cp.toString());

	
		System.out.println(mxm2.sendCampaign(c, cp, ae));
		

	}
	public static void main(String[] args) throws MxmConnectException, MxmConnectConfigurationException {
		String uname = "contact@graphbrain.com";
		String pwd = "G0rMIIvCbW5SD";
		String apiKey = "5kukVJVNsUtXpLZD";


      SendMailJ m = new SendMailJ(uname, pwd, apiKey);
      m.sendEmail();
           //m.testOthers();

	}
}
