package com.graphbrain.braingenerators;

import com.graphbrain.db.Graph;
import com.graphbrain.db.Vertex;
import com.graphbrain.eco.*;
import de.l3s.boilerpipe.BoilerpipeExtractor;
import de.l3s.boilerpipe.document.Image;
import de.l3s.boilerpipe.extractors.CommonExtractors;
import de.l3s.boilerpipe.sax.HTMLHighlighter;
import de.l3s.boilerpipe.sax.ImageExtractor;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.io.*;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PageReader {

    public static int nonWhitespaceLength(String str) {
        int length = 0;
        for (char c : str.toCharArray()) {
            if (!Character.isWhitespace(c)) {
                length++;
            }
        }
        return length;
    }

    private class TextAndHtml {
        private String text;
        private List<Tag> tags;
        private int curPos;

        public TextAndHtml() {
            text = "";
            tags = new LinkedList<>();
            curPos = 0;
        }

        public void addText(String text) {
            if (!this.text.isEmpty()) {
                this.text += " ";
            }
            this.text += text;
            curPos += nonWhitespaceLength(text);
        }

        public void addTextAndTag(String text, String tag, String href) {
            int start = curPos;
            addText(text);
            Tag t = new Tag(start, curPos, tag, href);
            tags.add(t);
        }

        public void addTextAndTag(String text, String tag) {
            addTextAndTag(text, tag, "");
        }

        public String getText() {
            return text;
        }

        public List<Tag> getTags() {
            return tags;
        }
    }


    private class Tag {
        public int start;
        public int end;
        public String tag;
        public String href;

        public Tag(int start, int end, String tag, String href) {
            this.start = start;
            this.end = end;
            this.tag = tag;
            this.href = href;
        }
    }


    private static final Pattern aPattern = Pattern.compile("<a href='(.+?)'>");
    private static final Pattern tagPattern = Pattern.compile("<(.+?)>");

    private String urlStr;
    private Prog prog;
    private SentenceDetector sentenceDetector;

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

        // init sentence detector
        sentenceDetector = null;

        InputStream modelIn;
        try {
            // Loading sentence detection model
            modelIn = Thread.currentThread().getContextClassLoader().getResourceAsStream("pos_models/en-sent.bin");
            final SentenceModel sentenceModel = new SentenceModel(modelIn);
            modelIn.close();

            sentenceDetector = new SentenceDetectorME(sentenceModel);
        }
        catch (final IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void parseNode(Node node, TextAndHtml th) {
        for (Node n : node.childNodes()) {
            if (n instanceof TextNode) {
                th.addText(((TextNode) n).text());
            }
            else if (n.nodeName().equals("a")) {
                Node cn = n.childNode(0);
                if (cn instanceof TextNode) {
                    th.addTextAndTag(((TextNode)cn).text(), "a", n.attr("abs:href"));
                }
            }
            else if (n.nodeName().equals("strong")) {
                Node cn = n.childNode(0);
                if (cn instanceof TextNode) {
                    th.addTextAndTag(((TextNode)cn).text(), "strong");
                }
            }
            else {
                parseNode(n, th);
            }
        }
    }

    private void parseDoc(Node doc) {

        TextAndHtml th = new TextAndHtml();
        parseNode(doc, th);

        if (!th.getText().isEmpty()) {
            String[] sentences = sentenceDetector.sentDetect(th.getText());
            for (String l : sentences) {
                String sentence = "";
                List<HtmlTag> htmlTags = new LinkedList<>();
                HtmlTag curHtmlTag = null;
                String[] tokens = POSTagger.tokenize(l);
                for (String token : tokens) {
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
            parseDoc(doc);


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
        //PageReader pr = new PageReader("http://judithcurry.com/2014/02/10/uk-us-workshop-on-climate-science-needed-to-support-robust-adaptation-decisions/", new Graph());
        //PageReader pr = new PageReader("http://www.realclimate.org/index.php/archives/2014/01/global-temperature-2013/");
        //PageReader pr = new PageReader("http://latimesblogs.latimes.com/world_now/2012/07/lack-of-exercise-kills-roughly-as-many-as-smoking-study-says.html", new Graph());
        PageReader pr = new PageReader("http://www.realclimate.org/index.php/archives/2014/02/going-with-the-wind/", new Graph());
        pr.read();
    }
}
