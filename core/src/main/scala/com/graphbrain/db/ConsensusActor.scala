package com.graphbrain.db

import akka.actor.Actor

class ConsensusActor(val store: UserOps) extends Actor {

  override def receive = {
    case edge: Edge =>
      Consensus.evalEdge(edge, store)
  }
}
