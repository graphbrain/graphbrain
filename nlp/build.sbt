organization := "com.graphbrain"

name := "nlp"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.9.1"

scalacOptions += "-deprecation"

scalacOptions += "-unchecked"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "com.typesafe.akka" % "akka-actor" % "2.0.2"

libraryDependencies ++= Seq(
  "net.databinder" %% "unfiltered-netty-server" % "0.6.1",
  "net.databinder" %% "unfiltered-spec" % "0.5.3" % "test"
)