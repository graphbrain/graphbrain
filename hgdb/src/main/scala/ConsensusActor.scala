package com.graphbrain.hgdb

import akka.actor.Actor
import akka.actor.Props


class ConsensusActor(val store: VertexStore) extends Actor {

  override protected def receive = {
    case edgeId: String =>
      Consensus.evalEdge(edgeId, store)
  }
}
