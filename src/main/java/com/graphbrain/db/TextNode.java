package com.graphbrain.db;

import java.util.HashMap;
import java.util.Map;


public class TextNode extends Textual {

	private String text;
	
	public TextNode(String namespace, String text, String summary) {
		super(namespace + "/" + ID.sanitize(text).toLowerCase());
		this.text = text;
		this.summary = summary;
	}
	
	public TextNode(String id, Map<String, String> map) {
		super(id);
		this.text = map.get("text");
		this.summary = map.get("summary");
	}
	
	public TextNode(String id) {
	    this(ID.namespace(id), ID.humanReadable(id), "");
	}
	
	@Override
	public VertexType type() {return VertexType.Text;}
	
	@Override
	public Map<String, String> toMap() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("text", text);
		map.put("summary", summary);
		return map;
	}
	
	public Vertex copy(String newid) {
		return new TextNode(ID.namespace(newid), text, summary);
	}
	
	public TextNode makeGlobal() {
		id = ID.userToGlobal(id);
		return this;
	}
	
	public TextNode makeUser(String userId) {
		id = ID.globalToUser(id, userId);
		return this;
	}
	
	public TextNode removeContext() {
		id = ID.userToGlobal(ID.removeContext(id));
		return this;
	}
	
	public TextNode setContext(String newContext) {
		id = ID.setContext(id, newContext);
		return this;
	}
	
	public String raw() {
		return "type: " + "text<br />" +
				"text: " + text + "<br />" +
				"summary: " + summary + "<br />";
	}
	
	@Override
	public String toString() {
		return text;
	}

/*

  override def updateFromEdges(): Vertex = {
    val newSummary = generateSummary
    val newVertex = this.copy(summary=newSummary)

    if (this != newVertex) {
      put()
    }

    this
  }
  */
}