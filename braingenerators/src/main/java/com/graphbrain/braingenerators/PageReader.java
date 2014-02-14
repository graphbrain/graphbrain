package com.graphbrain.braingenerators;

import de.l3s.boilerpipe.BoilerpipeExtractor;
import de.l3s.boilerpipe.extractors.CommonExtractors;
import de.l3s.boilerpipe.sax.HTMLHighlighter;
import java.net.URL;

public class PageReader {

    public PageReader() {

    }

    public static void main(String[] args) {
        try {
            URL url = new URL("http://judithcurry.com/2014/02/10/uk-us-workshop-on-climate-science-needed-to-support-robust-adaptation-decisions/");

            // choose from a set of useful BoilerpipeExtractors...
            final BoilerpipeExtractor extractor = CommonExtractors.ARTICLE_EXTRACTOR;
            // choose the operation mode (i.e., highlighting or extraction)
            final HTMLHighlighter hh = HTMLHighlighter.newExtractingInstance();

            //PrintWriter out = new PrintWriter("/tmp/highlighted.html", "UTF-8");
            //System.out.println("<base href=\"" + url + "\" >");
            //System.out.println("<meta http-equiv=\"Content-Type\" content=\"text-html; charset=utf-8\" />");
            System.out.println(hh.process(url, extractor));
            //out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
