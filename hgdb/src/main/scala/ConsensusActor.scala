package com.graphbrain.hgdb

import akka.actor.{Actor, Props}


class ConsensusActor(val store: VertexStoreInterface) extends Actor {

  override protected def receive = {
    case edge: Edge =>
      val pids = edge.participantIds

      val v1 = store.get(pids(0))
  }
}