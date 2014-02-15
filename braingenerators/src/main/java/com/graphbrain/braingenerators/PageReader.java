package com.graphbrain.braingenerators;

import com.graphbrain.db.Graph;
import com.graphbrain.db.Vertex;
import com.graphbrain.eco.*;
import de.l3s.boilerpipe.BoilerpipeExtractor;
import de.l3s.boilerpipe.document.Image;
import de.l3s.boilerpipe.extractors.CommonExtractors;
import de.l3s.boilerpipe.sax.HTMLHighlighter;
import de.l3s.boilerpipe.sax.ImageExtractor;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.DocumentPreprocessor;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PageReader {

    private static final Pattern aPattern = Pattern.compile("<a href='(.+?)'>");
    private static final Pattern tagPattern = Pattern.compile("<(.+?)>");

    private String urlStr;
    private Prog prog;

    public PageReader(String urlStr, Graph graph) {
        this.urlStr = urlStr;

        File file = new File("eco/page.eco");
        String progStr = "";
        try {
            progStr = FileUtils.readFileToString(file);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        prog = Prog.fromString(progStr, graph);
    }

    private void parseNode(Node node) {
        String text = "";

        for (Node n : node.childNodes()) {
            if (n instanceof TextNode) {
                text += " " + ((TextNode)n).text();
            }
            else if (n.nodeName().equals("a")) {
                Node cn = n.childNode(0);
                if (cn instanceof TextNode) {
                    text += " <a href='" + n.attr("abs:href") + "'>" + ((TextNode)cn).text() + "</a>";
                }
            }
            else if (n.nodeName().equals("strong")) {
                Node cn = n.childNode(0);
                if (cn instanceof TextNode) {
                    text += " <strong>" + ((TextNode)cn).text() + "</strong>";
                }
            }
            else {
                parseNode(n);
            }
        }

        if (!text.isEmpty()) {
            DocumentPreprocessor dp = new DocumentPreprocessor(new StringReader(text));
            for (List<HasWord> l : dp) {
                String sentence = "";
                List<HtmlTag> htmlTags = new LinkedList<>();
                HtmlTag curHtmlTag = null;
                for (HasWord h : l) {
                    String token = h.toString();
                    if ((token.charAt(0) == '<') && (token.length() > 1)) {
                        if (token.charAt(1) == '/') {
                            curHtmlTag = null;
                        }
                        else {
                            Matcher matcher = aPattern.matcher(token);
                            if (matcher.find()) {
                                String href = matcher.group(1);
                                curHtmlTag = new HtmlTag("a", href);
                            }
                            else {
                                matcher = tagPattern.matcher(token);
                                if (matcher.find()) {
                                    String tag = matcher.group(1);
                                    curHtmlTag = new HtmlTag(tag);
                                }
                            }
                        }
                    }
                    else {
                        sentence += token + " ";
                        htmlTags.add(curHtmlTag);
                    }
                }
                System.out.println("s: " + sentence);
                Words words = Words.fromStringAndHtmlTags(sentence, htmlTags);

                if (words.getWords().length <= 25) {

                List<Contexts> ctxtsList = prog.wv(words, 0, null);
                for (Contexts ctxts : ctxtsList) {
                    for (Context ctxt : ctxts.getCtxts()) {
                        Vertex v = ctxt.getTopRetVertex();
                        System.out.println(v);
                    }
                }

                }
            }
        }
    }

    public void read() {
        try {
            URL url = new URL(urlStr);

            final BoilerpipeExtractor extractor = CommonExtractors.ARTICLE_EXTRACTOR;
            final HTMLHighlighter hh = HTMLHighlighter.newExtractingInstance();

            String html = hh.process(url, extractor);

            Document doc = Jsoup.parse(html);
            parseNode(doc);


            final ImageExtractor ie = ImageExtractor.INSTANCE;

            List<Image> imgUrls = ie.process(url, extractor);

            // automatically sorts them by decreasing area, i.e. most probable true positives come first
            Collections.sort(imgUrls);

            for(Image img : imgUrls) {
                System.out.println("* " + img);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        PageReader pr = new PageReader("http://judithcurry.com/2014/02/10/uk-us-workshop-on-climate-science-needed-to-support-robust-adaptation-decisions/", new Graph());
        //PageReader pr = new PageReader("http://www.realclimate.org/index.php/archives/2014/01/global-temperature-2013/");
        pr.read();
    }
}
