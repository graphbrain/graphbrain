package com.graphbrain.db;

import java.net.*;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class URLNode extends Vertex {
  
	private String url;
	private String title;
	private String icon;
	
	public URLNode(String url, String userId) {
		super(ID.urlId(url, userId));
		this.url = url;
		String[] tai = getTitleAndIcon(url);
		this.title = tai[0];
		this.icon = tai[1];
	}
	
	public URLNode(String url, String userId, String title, String icon) {
		super(ID.urlId(url, userId));
		this.url = url;
		this.title = title;
		this.icon = icon;
	}
	
	public URLNode(String id, Map<String, String> map) {
		super(id, map);
		this.url = map.get("url");
		this.title = map.get("title");
		this.icon = map.get("icon");
	}
	
	@Override
	public VertexType type() {return VertexType.URL;}
	
	//override def clone(newid: String) = URLNode(store, newid, userId, title)
	
	@Override
	public void fillMap(Map<String, String> map) {
		map.put("url", url);
		map.put("title", title);
		map.put("icon", icon);
	}
	
	private static boolean exists(String urlName) {
		try {
			HttpURLConnection.setFollowRedirects(false);
			// note : you may also need
			//        HttpURLConnection.setInstanceFollowRedirects(false)
			HttpURLConnection con = (HttpURLConnection)new URL(urlName).openConnection();
			con.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
			con.setRequestMethod("HEAD");
			return con.getResponseCode() == HttpURLConnection.HTTP_OK;
		}
		catch (Exception e) {
			return false;
		}
	}

	private static String getDomainName(String url) {
		try {
			URI uri = new URI(url);
			return uri.getHost();
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
			return "";
		}
	}

	private static String[] getTitleAndIcon(String url) {
		//ldebug("getTitleAndIcon " + url)

		try {
			Document doc = Jsoup.connect(url).header("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2").get();
    
			String title = doc.title();
			//ldebug("title: " + title)
    
			Elements links = doc.select("link[rel=shortcut icon]");
			Element link = links.first();
			String icon = "";
			if (link != null) {
				String icoUrl = link.attr("abs:href");
				icon = icoUrl;
			}
			else {
				String icoUrl = "http://" + getDomainName(url) + "/favicon.ico";
				//ldebug("default icon url: " + icoUrl)
				if (exists(icoUrl)) {
					icon = icoUrl;
				}
			}

			//ldebug("icon: " + icon)

			String[] res = {title, icon};
			return res;
		}
		catch (Exception e) {
			//ldebug(e.toString)
			return null;
		}
	}

	public Vertex makeGlobal() {
		id = ID.urlId(url);
		return this;
	}
	
	public Vertex makeUser(String newUserId) {
		id = ID.urlId(url, newUserId);
		return this;
	}

	public String raw() {
		return "type: " + "url<br />" +
				"url: " + url + "<br />" +
				"title: " + title + "<br />";
	}

	public boolean shouldUpdate() {
		return true;
	}
}