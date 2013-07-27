package com.graphbrain.db;


public abstract class Textual extends Vertex {
	protected String summary;
	
	public Textual(String id) {
		super(id);
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}
	
	/*
	// Score edge in terms of its ability to generate a summary for this TextNode
	private int scoreForSummary(Edge edge) {
    // Only consider edges where this TextNode is the first participant
    if (edge.participantIds(0) != id)
      return -1

    edge.edgeType match {
      case "rtype/1/as_in" => 100
      case "rtype/1/is_a" => 10
      case _ => -1
    }
  }

  def generateSummary: String = {
    val edges = store.neighborEdges(id)

    var bestEdge: Edge = null
    var maxScore = -1
    for (e <- edges) {
      val score = scoreForSummary(e)
      if (score > maxScore) {
        bestEdge = e
        maxScore = score
      }
    }

    if (bestEdge == null)
      ""
    else
      "(" + store.get(bestEdge.participantIds(1)) + ")"
  }

  override def description: String = toString + " " + generateSummary
  */
}