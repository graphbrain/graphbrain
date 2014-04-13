package com.graphbrain.db;

import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class URLNode extends Vertex {

    private String title;
    private String icon;
    private String url;

    @Override
    public VertexType type() {return VertexType.URL;}

    public URLNode(String id, String title, String icon, int degree, long ts) {
        super(id, degree, ts);
        this.title = title;
        this.icon = icon;
        url = URLNode.idToUrl(id);
    }

    public URLNode(String id, String title, String icon) {
        this(id, title, icon, 0, -1);
    }

    public URLNode(String id, String title) {
        this(id, title, "");
    }

    public URLNode(String id) {
        this(id, "");
    }

    @Override
    public Vertex copy() {
        return new URLNode(id, title, icon, degree, ts);
    }

    @Override
    public String raw() {
        return "type: " + "url<br />" + "url: " + url
                + "<br />" + "title: " + title + "<br />";
  }

    public static String idToUrl(String id) {
        try {
            return URLDecoder.decode(ID.lastPart(id), "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String urlToId(String url) {
        return "url/" + url;

        /*
        try {
            return "url/" + URLEncoder.encode(url, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
        */
    }

    public static URLNode fromUrl(String url) {
        return fromUrl(url, "");
    }

    public static URLNode fromUrl(String url, String userId) {
        String auxId = urlToId(url);
        String id;

        if (userId.isEmpty()) {
            id = auxId;
        }
        else {
            id = ID.globalToUser(auxId, userId);
        }
        String[] titleAndIcon = getTitleAndIcon(url);
        return new URLNode(id, titleAndIcon[0], titleAndIcon[1]);
    }

    private static String[] getTitleAndIcon(String url) {
        try {
            Document doc = Jsoup.connect(url).header("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2").get();

            String title = doc.title();

            Elements links = doc.select("link[rel=shortcut icon]");
            Element link = links.first();

            String icon;

            if (link != null) {
                icon = link.attr("abs:href");
            }
            else {
                String icoUrl = "http://" + getDomainName(url) + "/favicon.ico";
                if (exists(icoUrl))
                    icon = icoUrl;
                else
                    icon = "";
            }

            return new String[]{title, icon};
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getDomainName(String url) {
        try {
            URI uri = new URI(url);
            return uri.getHost();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static boolean exists(String urlName) {
        try {
            HttpURLConnection.setFollowRedirects(false);
            // note : you may also need
            //        HttpURLConnection.setInstanceFollowRedirects(false)
            HttpURLConnection con = (HttpURLConnection)(new URL(urlName).openConnection());
            con.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
            con.setRequestMethod("HEAD");
            return con.getResponseCode() == HttpURLConnection.HTTP_OK;
        }
        catch (Exception e) {
            return false;
        }
    }

    public String getTitle() {
        return title;
    }

    public String getIcon() {
        return icon;
    }

    public String getUrl() {
        return url;
    }
}
