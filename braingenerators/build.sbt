import sbtassembly.Plugin._
import AssemblyKeys._

organization := "com.graphbrain"

name := "braingenerators"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.9.1"

scalacOptions += "-unchecked"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.7.1" % "test"

libraryDependencies += "com.basho.riak" % "riak-client" % "1.0.4"

//testOptions in Test += Tests.Argument("-oF")

parallelExecution in Test := false 

mainClass in assembly := Some("GenerateDBPedia")