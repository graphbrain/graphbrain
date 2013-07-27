package com.graphbrain.gbdb

case class KeyNotFound(message: String) extends Exception(message)

case class WrongVertexType(message: String) extends Exception(message)