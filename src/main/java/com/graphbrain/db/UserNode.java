package com.graphbrain.db;

import java.util.HashMap;
import java.util.Map;

//import org.mindrot.jbcrypt.BCrypt;


public class UserNode extends Textual {

	private String username;
	private String name;
	private String email;
	private String pwdhash;
	private String role;
	private String session;
	private long sessionTs;
	private long lastSeen;
	
	public UserNode(String username, String name, String email, String pwdhash, String role) {
		super(ID.userIdFromUsername(username));
		this.username = username;
		this.name = name;
		this.email = email;
		this.pwdhash = pwdhash;
		this.role = role;
	}
	
	public UserNode(String id, Map<String, String> map) {
		super(id);
		this.username = map.get("username");
		this.name = map.get("name");
		this.email = map.get("email");
		this.pwdhash = map.get("pwdhash");
		this.role = map.get("role");
		this.session = map.get("session");
		this.sessionTs = Long.parseLong(map.get("sessionTs"));
		this.lastSeen = Long.parseLong(map.get("lastSeen"));
	}
	
	@Override
	public Map<String, String> toMap() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("username", username);
		map.put("name", name);
		map.put("email", email);
		map.put("pwdhash", pwdhash);
		map.put("role", role);
		map.put("session", session);
		map.put("sessionTs", String.valueOf(sessionTs));
		map.put("lastSeen", String.valueOf(lastSeen));
		return map;
	}

  
	@Override
	public String toString() {
		return name;
	}

	public String raw() {
		return "type: " + "user<br />" +
				"username: " + username + "<br />" +
				"name: " + name + "<br />" +
				"role: " + role + "<br />" +
				"lastSeen: " + lastSeen + "<br />";
	}
}