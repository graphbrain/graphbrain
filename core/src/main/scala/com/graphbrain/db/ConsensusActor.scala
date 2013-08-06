package com.graphbrain.gbdb

import akka.actor.Actor
import akka.actor.Props


class ConsensusActor(val store: UserOps) extends Actor {

  override protected def receive = {
    case edge: Edge =>
      Consensus.evalEdge(edge, store)
  }
}
