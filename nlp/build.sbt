import sbtassembly.Plugin._
import AssemblyKeys._

organization := "com.graphbrain"

name := "nlp"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.9.2"

scalacOptions += "-deprecation"

scalacOptions += "-unchecked"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "com.typesafe.akka" % "akka-actor" % "2.0.3"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.7.1" % "test"

libraryDependencies ++= Seq(
  "net.databinder" %% "unfiltered-netty-server" % "0.6.3",
  "net.databinder" %% "unfiltered-spec" % "0.6.3" % "test"
)

parallelExecution in Test := false 
