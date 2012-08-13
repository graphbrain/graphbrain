organization := "com.graphbrain"

name := "hgdb"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.9.1"

scalacOptions += "-unchecked"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "com.typesafe.akka" % "akka-actor" % "2.0.2"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.7.1" % "test"

libraryDependencies += "com.basho.riak" % "riak-client" % "1.0.4"

//testOptions in Test += Tests.Argument("-oF")

libraryDependencies ~= { seq =>
  val vers = "0.8.8"
  seq ++ Seq(
    "net.databinder" %% "dispatch-core" % vers,
    "net.databinder" %% "dispatch-oauth" % vers,
    "net.databinder" %% "dispatch-nio" % vers,
    //
    "net.databinder" %% "dispatch-http" % vers,
    "net.databinder" %% "dispatch-tagsoup" % vers,
    "net.databinder" %% "dispatch-jsoup" % vers
  )
}

parallelExecution in Test := false 