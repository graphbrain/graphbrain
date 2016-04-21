/**
 * Copyright 2011 The Open Source Research Group,
 *                University of Erlangen-Nürnberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.graphbrain.kr;

import java.util.LinkedList;
import java.util.regex.Pattern;

import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.nodes.EngPage;
import org.sweble.wikitext.parser.nodes.WtBold;
import org.sweble.wikitext.parser.nodes.WtExternalLink;
import org.sweble.wikitext.parser.nodes.WtHorizontalRule;
import org.sweble.wikitext.parser.nodes.WtIllegalCodePoint;
import org.sweble.wikitext.parser.nodes.WtImageLink;
import org.sweble.wikitext.parser.nodes.WtInternalLink;
import org.sweble.wikitext.parser.nodes.WtItalics;
import org.sweble.wikitext.parser.nodes.WtListItem;
import org.sweble.wikitext.parser.nodes.WtNode;
import org.sweble.wikitext.parser.nodes.WtNodeList;
import org.sweble.wikitext.parser.nodes.WtOrderedList;
import org.sweble.wikitext.parser.nodes.WtPageSwitch;
import org.sweble.wikitext.parser.nodes.WtParagraph;
import org.sweble.wikitext.parser.nodes.WtSection;
import org.sweble.wikitext.parser.nodes.WtTagExtension;
import org.sweble.wikitext.parser.nodes.WtTemplate;
import org.sweble.wikitext.parser.nodes.WtTemplateArgument;
import org.sweble.wikitext.parser.nodes.WtTemplateParameter;
import org.sweble.wikitext.parser.nodes.WtText;
import org.sweble.wikitext.parser.nodes.WtUnorderedList;
import org.sweble.wikitext.parser.nodes.WtUrl;
import org.sweble.wikitext.parser.nodes.WtWhitespace;
import org.sweble.wikitext.parser.nodes.WtXmlCharRef;
import org.sweble.wikitext.parser.nodes.WtXmlComment;
import org.sweble.wikitext.parser.nodes.WtXmlElement;
import org.sweble.wikitext.parser.nodes.WtXmlEntityRef;
import org.sweble.wikitext.parser.parser.LinkTargetException;

import de.fau.cs.osr.ptk.common.AstVisitor;
import de.fau.cs.osr.utils.StringUtils;

/**
 * A visitor to convert an article AST into a pure text representation. To
 * better understand the visitor pattern as implemented by the Visitor class,
 * please take a look at the following resources:
 * <ul>
 * <li><a
 * href="http://en.wikipedia.org/wiki/Visitor_pattern">http://en.wikipedia
 * .org/wiki/Visitor_pattern</a> (classic pattern)</li>
 * <li><a
 * href="http://www.javaworld.com/javaworld/javatips/jw-javatip98.html">http
 * ://www.javaworld.com/javaworld/javatips/jw-javatip98.html</a> (the version we
 * use here)</li>
 * </ul>
 * 
 * The methods needed to descend into an AST and visit the children of a given
 * node <code>n</code> are
 * <ul>
 * <li><code>dispatch(n)</code> - visit node <code>n</code>,</li>
 * <li><code>iterate(n)</code> - visit the <b>children</b> of node
 * <code>n</code>,</li>
 * <li><code>map(n)</code> - visit the <b>children</b> of node <code>n</code>
 * and gather the return values of the <code>visit()</code> calls in a list,</li>
 * <li><code>mapInPlace(n)</code> - visit the <b>children</b> of node
 * <code>n</code> and replace each child node <code>c</code> with the return
 * value of the call to <code>visit(c)</code>.</li>
 * </ul>
 */
public class WikiTextConverter extends AstVisitor<WtNode>
{
    private static final Pattern ws = Pattern.compile("\\s+");
	
    private final WikiConfig config;
	
    private final int wrapCol;
	
    private StringBuilder sb;
	
    private StringBuilder line;
	
    private int extLinkNum;
	
    /**
     * Becomes true if we are no long at the Beginning Of the whole Document.
     */
    private boolean pastBod;
    
    private int needNewlines;
	
    private boolean needSpace;
	
    private boolean noWrap;
	
    private LinkedList<Integer> sections;

    private LinkedList<String> links;
	
    // =========================================================================
	
    public WikiTextConverter(WikiConfig config, int wrapCol) {
	this.config = config;
	this.wrapCol = wrapCol;
    }
	
    @Override
    protected boolean before(WtNode node) {
	// This method is called by go() before visitation starts
	sb = new StringBuilder();
	line = new StringBuilder();
	extLinkNum = 1;
	pastBod = false;
	needNewlines = 0;
	needSpace = false;
	noWrap = false;
	sections = new LinkedList<Integer>();
	links = new LinkedList<String>();
	return super.before(node);
    }
	
    @Override
    protected Object after(WtNode node, Object result) {
	finishLine();
	
	// This method is called by go() after visitation has finished
	// The return value will be passed to go() which passes it to the caller
	return sb.toString();
    }
	
    // =========================================================================
	
    public void visit(WtNode n) {
	// Fallback for all nodes that are not explicitly handled below
	write("<");
	write(n.getNodeName());
	write(" />");
    }
	
    public void visit(WtNodeList n) {
	iterate(n);
    }
    
    public void visit(WtUnorderedList e) {
	iterate(e);
    }
	
    public void visit(WtOrderedList e) {
	iterate(e);
    }
	
    public void visit(WtListItem item) {
	newline(1);
	iterate(item);
    }
	
    public void visit(EngPage p) {
	iterate(p);
    }
    
    public void visit(WtText text) {
	write(text.getContent());
    }
	
    public void visit(WtWhitespace w) {
	write(" ");
    }
	
    public void visit(WtBold b) {
	// write("**");
	iterate(b);
	// write("**");
    }
	
    public void visit(WtItalics i) {
	// write("//");
	iterate(i);
	// write("//");
    }
	
    public void visit(WtXmlCharRef cr) {
	write(Character.toChars(cr.getCodePoint()));
    }
	
    public void visit(WtXmlEntityRef er) {
	String ch = er.getResolved();
	if (ch == null) {
	    write('&');
	    write(er.getName());
	    write(';');
	}
	else {
	    write(ch);
	}
    }
	
    public void visit(WtUrl wtUrl) {
	if (!wtUrl.getProtocol().isEmpty()) {
	    write(wtUrl.getProtocol());
	    write(':');
	}
	write(wtUrl.getPath());
    }
	
    public void visit(WtExternalLink link) {
	write('[');
	write(extLinkNum++);
	write(']');
    }
	
    public void visit(WtInternalLink link) {
	try {
	    if (link.getTarget().isResolved()) {
		PageTitle page = PageTitle.make(config,
						link.getTarget().getAsString());
		if (page.getNamespace().equals(config.getNamespace("Category")))
		    return;
	    }
	}
	catch (LinkTargetException e) {}

	links.add(((WtText)link.getTarget().get(0)).getContent());

	System.out.println(link.getTarget());
	write(link.getPrefix());
	if (!link.hasTitle()) {
	    iterate(link.getTarget());
	}
	else {
	    iterate(link.getTitle());
	}
	write(link.getPostfix());
    }
    
    public void visit(WtSection s) {
	finishLine();
	StringBuilder saveSb = sb;
	boolean saveNoWrap = noWrap;
	
	sb = new StringBuilder();
	noWrap = true;
		
	iterate(s.getHeading());
	finishLine();
	String title = sb.toString().trim();
		
	sb = saveSb;

	// System.out.println(title);
	// System.out.println(s.getLevel());
	
	if (s.getLevel() >= 1) {
	    while (sections.size() > s.getLevel())
		sections.removeLast();
	    while (sections.size() < s.getLevel())
		sections.add(1);
	    
	    StringBuilder sb2 = new StringBuilder();
	    for (int i = 0; i < sections.size(); ++i) {
		if (i < 1)
		    continue;
			
		sb2.append(sections.get(i));
		sb2.append('.');
	    }
			
	    if (sb2.length() > 0)
		sb2.append(' ');
	    sb2.append(title);
	    title = sb2.toString();
	}
		
	newline(1);
	write(title);
	newline(1);
		
	noWrap = saveNoWrap;
	
	iterate(s.getBody());
		
	while (sections.size() > s.getLevel())
	    sections.removeLast();
	sections.add(sections.removeLast() + 1);
    }
	
    public void visit(WtParagraph p) {
	iterate(p);
	newline(2);
    }
	
    public void visit(WtHorizontalRule hr) {
	newline(1);
	write(StringUtils.strrep('-', wrapCol));
	newline(2);
    }
	
    public void visit(WtXmlElement e) {
	if (e.getName().equalsIgnoreCase("br")) {
	    newline(1);
	}
	else {
	    iterate(e.getBody());
	}
    }
	
    // =========================================================================
    // Stuff we want to hide
	
    public void visit(WtImageLink n) {}
	
    public void visit(WtIllegalCodePoint n) {}
	
    public void visit(WtXmlComment n) {}
	
    public void visit(WtTemplate n) {}
	
    public void visit(WtTemplateArgument n) {}
	
    public void visit(WtTemplateParameter n) {}
	
    public void visit(WtTagExtension n) {}
	
    public void visit(WtPageSwitch n) {}
	
    // =========================================================================
	
    private void newline(int num) {
	if (pastBod) {
	    if (num > needNewlines)
		needNewlines = num;
	}
    }
	
    private void wantSpace() {
	if (pastBod)
	    needSpace = true;
    }
	
    private void finishLine() {
	sb.append(line.toString());
	line.setLength(0);
    }
	
    private void writeNewlines(int num) {
	finishLine();
	sb.append(StringUtils.strrep('\n', num));
	needNewlines = 0;
	needSpace = false;
    }
	
    private void writeWord(String s) {
	int length = s.length();
	if (length == 0)
	    return;
		
	if (!noWrap && needNewlines <= 0) {
	    if (needSpace)
		length += 1;
			
	    if (line.length() + length >= wrapCol && line.length() > 0)
		writeNewlines(1);
	}
		
	if (needSpace && needNewlines <= 0)
	    line.append(' ');
		
	if (needNewlines > 0)
	    writeNewlines(needNewlines);
		
	needSpace = false;
	pastBod = true;
	line.append(s);
    }
	
    private void write(String s) {
	if (s.isEmpty())
	    return;
		
	if (Character.isSpaceChar(s.charAt(0)))
	    wantSpace();
		
	String[] words = ws.split(s);
	for (int i = 0; i < words.length;) {
	    writeWord(words[i]);
	    if (++i < words.length)
		wantSpace();
	}
		
	if (Character.isSpaceChar(s.charAt(s.length() - 1)))
	    wantSpace();
    }
	
    private void write(char[] cs) {
	write(String.valueOf(cs));
    }
	
    private void write(char ch) {
	writeWord(String.valueOf(ch));
    }
	
    private void write(int num) {
	writeWord(String.valueOf(num));
    }

    public LinkedList<String> getLinks() {
	return links;
    }
}
