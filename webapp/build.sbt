import sbtassembly.Plugin._
import AssemblyKeys._

organization := "com.graphbrain"

name := "webapp"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.9.2"

scalacOptions += "-deprecation"

scalacOptions += "-unchecked"

javaOptions in Revolver.reStart += "-Xmx1g"

resolvers ++= Seq("Coda Hales Repository" at "http://repo.codahale.com")

libraryDependencies += "net.databinder" %% "unfiltered-netty-server" % "0.6.3"

libraryDependencies += "org.jclarion" % "image4j" % "0.7"

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
  {
    case PathList("javax", "servlet", xs @ _*) => MergeStrategy.first
    case PathList("com", "eaio", "uuid", xs @ _*) => MergeStrategy.first
    case PathList("com", "eaio", "util", "lang", xs @ _*) => MergeStrategy.first
    case x => old(x)
  }
}
