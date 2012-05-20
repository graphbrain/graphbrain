package com.graphbrain.hgdb


object ID {

  def sanitize(str: String): String = str.toLowerCase.replace("/", "_").replace(" ", "_")

  def brainId(name: String, userName: String) =
    "brain/" + userName + "/" + sanitize(name)
}