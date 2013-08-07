package com.graphbrain.db

case class KeyNotFound(message: String) extends Exception(message)

case class WrongVertexType(message: String) extends Exception(message)