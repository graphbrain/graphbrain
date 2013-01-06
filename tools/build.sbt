import sbtassembly.Plugin._
import AssemblyKeys._


organization := "com.graphbrain"

name := "tools"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.9.2"

scalacOptions += "-unchecked"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.7.1" % "test" 

mainClass in assembly := Some("com.graphbrain.tools.Tools")

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
  {
    case PathList("com", "eaio", "uuid", xs @ _*) => MergeStrategy.first
    case PathList("com", "eaio", "util", "lang", xs @ _*) => MergeStrategy.first
    case x => old(x)
  }
}