package com.graphbrain.hgdb


object IdFamily extends Enumeration {
  type IdFamily = Value
  val Global, User, Email, UserSpace, EdgeType = Value

  def family(id: String) = {
    val parts = ID.parts(id)
    val nparts = ID.numberOfParts(id)
    if (parts(0) == "user")
      if (nparts == 1)
        Global
      else if (nparts == 2)
        User
      else
        UserSpace
    else if (parts(0) == "email")
      if (nparts == 1)
        Global
      else
        Email
    else if (parts(0) == "rtype")
      if (nparts == 1)
        Global
      else
        EdgeType
    else
      Global
  }
}