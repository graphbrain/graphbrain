package com.graphbrain.crawlers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class Crawler {

    private String baseUrl;
    private String baseDir;
    private Set<String> pages;
    private int count;

    public Crawler(String baseUrl) {
        this.baseUrl = baseUrl;

        baseDir = "crawldata/" + url2path(baseUrl);

        pages = new HashSet<>();
        count = 0;

        createBaseDir();
    }

    public void crawl() {
        processPage(baseUrl);
    }

    private void createBaseDir() {
        File dir = new File(baseDir);

        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    private String url2path(String urlStr) {
        return urlStr.replaceAll("\\/", "_");
    }

    private boolean savePage(String pageUrlStr) {
        URL url;
        try {
            url = new URL(pageUrlStr);
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }

        if (url.getRef() != null) {
            return false;
        }

        String urlStr = pageUrlStr.toLowerCase();

        if (urlStr.contains("comment-page")) {
            return false;
        }
        else if (urlStr.contains("langswitch_lang")) {
            return false;
        }
        else if (urlStr.contains("comments_popup")) {
            return false;
        }
        else if (urlStr.contains("wpmp_switcher")) {
            return false;
        }
        else if (urlStr.contains("submit=search")) {
            return false;
        }
        else if (urlStr.endsWith(".png")) {
            return false;
        }
        else if (urlStr.endsWith(".gif")) {
            return false;
        }
        else if (urlStr.endsWith(".jpg")) {
            return false;
        }
        else if (urlStr.endsWith(".jpeg")) {
            return false;
        }
        else if (urlStr.endsWith(".pdf")) {
            return false;
        }
        else if (urlStr.endsWith(".r")) {
            return false;
        }

        return true;
    }

    private void processPage(String page) {
        pages.add(page);

        count++;
        System.out.println("#" + count + " " + page);

        /*
        if (page.matches("http:\\/\\/www\\.realclimate\\.org\\/index\\.php\\/archives\\/\\d{4}\\/\\d{2}\\/.*")) {
            System.out.println("!");
        }
        */

        try {
            Document doc = Jsoup.connect(page).get();

            String path = url2path(page);
            PrintWriter out = new PrintWriter(baseDir + "/" + path);
            out.println(doc.html());
            out.close();

            Elements links = doc.select("a");

            for (Element e : links) {
                String urlStr = e.attr("href");

                if (!urlStr.isEmpty()) {
                    if (urlStr.startsWith(baseUrl)) {
                        if (!pages.contains(urlStr)) {
                            if (savePage(urlStr)) {
                                processPage(urlStr);
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Crawler crawler = new Crawler(args[0]);
        crawler.crawl();
    }
}