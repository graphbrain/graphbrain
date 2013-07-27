package com.graphbrain.db;

import java.util.LinkedList;
import java.util.List;

public class Edge {
	
	private String edgeType;
	private List<String> participantIds;
	private Edge originalEdge;
	
	public Edge(String edgeType, List<String> extParticipantIds, Edge originalEdge) {
		this.edgeType = edgeType;
		participantIds = new LinkedList<String>();
		for (String pid : extParticipantIds) {
			participantIds.add(Vertex.cleanId(pid));
		}
		this.originalEdge = originalEdge;
	}
	
	public Edge(String edgeType, List<String> extParticipantIds) {
		this(edgeType, extParticipantIds, null);
	}

	public Edge negate() {
		edgeType = "neg/" + edgeType;
		return this;
	}

	public Edge fromString(String edgeString) {
	    String[] tokens = edgeString.split(" ");
	    edgeType = tokens[0];
	    
	    participantIds = new LinkedList<String>();
	    for (int i = 1; i < tokens.length; i++) {
	    	participantIds.add(tokens[i]);
	    }
	    return this;
	}
	
	public boolean isPositive() {
		return ID.parts(edgeType)[0] != "neg";
	}

	public boolean isGlobal() {
		for (String p : participantIds) {
			if (!ID.isUserNode(p) && ID.isInUserSpace(p)) {
				return false;
			}
		}
		return true;
	}

	public boolean isInUserSpace() {
		for (String p : participantIds) {
			if (ID.isInUserSpace(p)) {
				return true;
			}
		}
		return false;
	}

  	public boolean isInContextSpace() {
  		for (String p : participantIds) {
  			if (!ID.isInContextSpace(p)) {
  				return false;
  			}
  		}
  		return true;
  	}

  	public Edge makeUser(String userId) {
  		List<String> pids = new LinkedList<String>();
  		for (String pid : participantIds) {
  			pids.add(ID.globalToUser(pid, userId));
  		}
  		participantIds = pids;
  		return this;
  	}
  
  	public Edge makeGlobal() {
  		List<String> pids = new LinkedList<String>();
  		for (String pid : participantIds) {
  			pids.add(ID.userToGlobal(pid));
  		}
  		participantIds = pids;
  		return this;
  	}

  	public Edge getOriginalEdge() {
  		if (originalEdge == null) {
  			return this;
  		}
  		else {
  			return originalEdge;
  		}
  	}

  	@Override
  	public String toString() {
  		StringBuilder sb = new StringBuilder(100);
  		sb.append(edgeType);
  		for (String p : participantIds) {
  			sb.append(" ");
  			sb.append(p);
  		}
  		return sb.toString();
  	}

  	public String humanReadable2() {
  		return (ID.humanReadable(participantIds.get(0))
  				+ " [" +  ID.humanReadable(edgeType) + "] "
  				+ ID.humanReadable(participantIds.get(1))).replace(",", "");
  	}
}