package com.graphbrain.eco;

public class HtmlTag {
    private String tag;
    private String href;

    public HtmlTag(String tag, String href) {
        this.tag = tag;
        this.href = href;
    }

    public HtmlTag(String tag) {
        this(tag, "");
    }

    public String getTag() {
        return tag;
    }

    public String getHref() {
        return href;
    }
}
