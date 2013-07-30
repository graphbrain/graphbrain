package com.graphbrain.db;

import java.util.Map;
import java.math.BigInteger;
import com.graphbrain.utils.RandUtils;

import org.mindrot.jbcrypt.BCrypt;


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
		super(id, map);
		this.username = map.get("username");
		this.name = map.get("name");
		this.email = map.get("email");
		this.pwdhash = map.get("pwdhash");
		this.role = map.get("role");
		this.session = map.get("session");
		this.sessionTs = Long.parseLong(map.get("sessionTs"));
		this.lastSeen = Long.parseLong(map.get("lastSeen"));
	}
	
	public static UserNode create(String username, String name, String email, String password, String role) {
		String pwdhash = BCrypt.hashpw(password, BCrypt.gensalt());
		return new UserNode(username, name, email, pwdhash, role);
	}
	
	@Override
	public VertexType type() {return VertexType.User;}
	
	@Override
	public void fillMap(Map<String, String> map) {
		map.put("username", username);
		map.put("name", name);
		map.put("email", email);
		map.put("pwdhash", pwdhash);
		map.put("role", role);
		map.put("session", session);
		map.put("sessionTs", String.valueOf(sessionTs));
		map.put("lastSeen", String.valueOf(lastSeen));
	}

	public UserNode newSession() {
		session = (new BigInteger(130, RandUtils.secRand)).toString(32);
		return this;
	}
	
	public boolean checkPassword(String candidate) {
		return BCrypt.checkpw(candidate, pwdhash);
	}

	public boolean checkSession(String candidate) {
		return session == candidate;
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