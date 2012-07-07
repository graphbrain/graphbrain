package com.graphbrain.nlp

case class TooManyNodes(message: String) extends Exception(message)

case class QuestionException(message: String) extends Exception(message)