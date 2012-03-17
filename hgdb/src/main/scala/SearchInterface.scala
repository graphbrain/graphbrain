package com.graphbrain.hgdb


abstract trait SearchInterface {
  def initIndex(): Unit
  def index(key: String, text: String): Unit
  def query(text: String): SearchResults
}