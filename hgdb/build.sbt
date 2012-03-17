organization := "com.graphbrain"

name := "hgdb"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.9.1"

scalacOptions += "-unchecked"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.7.1" % "test"

libraryDependencies += "com.basho.riak" % "riak-client" % "1.0.4"

libraryDependencies ~= { seq =>
  val vers = "0.8.7"
  seq ++ Seq(
    "net.databinder" %% "dispatch-core" % vers,
    "net.databinder" %% "dispatch-oauth" % vers,
    "net.databinder" %% "dispatch-nio" % vers,
    /* Twine doesn't need the below dependencies, but it simplifies
     * the Dispatch tutorials to keep it here for now. */
    "net.databinder" %% "dispatch-http" % vers,
    "net.databinder" %% "dispatch-tagsoup" % vers,
    "net.databinder" %% "dispatch-jsoup" % vers
  )
}

//testOptions in Test += Tests.Argument("-oF")

parallelExecution in Test := false 