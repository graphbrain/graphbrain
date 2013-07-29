package com.graphbrain.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Edge extends Vertex {
	
	private String[] ids;
	
	public static String idFromParticipants(String[] participants) {
		StringBuilder sb = new StringBuilder(100);
		for (int i = 0; i < participants.length; i++) {
			if (i > 0) {
				sb.append(" ");
			}
			sb.append(participants[i]);
		}
		return sb.toString();
	}
	
	public static String idFromParticipants(String edgeType, List<String> participantIds) {
		StringBuilder sb = new StringBuilder(100);
		sb.append(edgeType);
		for (String pid : participantIds) {
			sb.append(" ");
			sb.append(pid);
		}
		return sb.toString();
	}
	
	public static String[] participantsFromId(String id) {
		return id.split(" ");
	}
	
	public Edge(String id) {
		super(id);
	    ids = participantsFromId(id);
	}
	
	public Edge(String edgeType, List<String> participantIds) {
		this(idFromParticipants(edgeType, participantIds));
	}
	
	public Edge(String id, Map<String, String> map) {
		this(id);
	}
	
	@Override
	public VertexType type() {return VertexType.Edge;}
	
	@Override
	public Map<String, String> toMap() {
		Map<String, String> map = new HashMap<String, String>();
		return map;
	}

	public Edge negate() {
		ids[0] = "neg/" + ids[0];
		return this;
	}
	
	public boolean isPositive() {
		return ID.parts(ids[0])[0] != "neg";
	}

	public boolean isGlobal() {
		for (int i = 1; i < ids.length; i++) {
			String p = ids[i];
			if (!ID.isUserNode(p) && ID.isInUserSpace(p)) {
				return false;
			}
		}
		return true;
	}

	public boolean isInUserSpace() {
		for (int i = 1; i < ids.length; i++) {
			String p = ids[i];
			if (ID.isInUserSpace(p)) {
				return true;
			}
		}
		return false;
	}

  	public boolean isInContextSpace() {
  		for (int i = 1; i < ids.length; i++) {
			String p = ids[i];
  			if (!ID.isInContextSpace(p)) {
  				return false;
  			}
  		}
  		return true;
  	}

  	public Edge makeUser(String userId) {
  		for (int i = 1; i < ids.length; i++) {
  			ids[i] = (ID.globalToUser(ids[i], userId));
  		}
  		return this;
  	}
  
  	public Edge makeGlobal() {
  		for (int i = 1; i < ids.length; i++) {
  			ids[i] = (ID.userToGlobal(ids[i]));
  		}
  		return this;
  	}

  	@Override
  	public String toString() {
  		StringBuilder sb = new StringBuilder(100);
  		for (int i = 0; i < ids.length; i++) {
  			if (i > 0) {
  				sb.append(" ");
  			}
  			sb.append(ids[i]);
  		}
  		return sb.toString();
  	}

  	public String[] getIds() {
		return ids;
	}

	public String humanReadable2() {
  		return (ID.humanReadable(ids[1]))
  				+ " [" +  ID.humanReadable(ids[0]) + "] "
  				+ ID.humanReadable(ids[2]).replace(",", "");
  	}
}