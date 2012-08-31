organization := "unfiltered"

name := "unfiltered-scalate"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.9.2"

libraryDependencies <++= scalaVersion { v => Seq(
  "net.databinder" %% "unfiltered" % "0.6.1",
  "org.fusesource.scalate" % "scalate-core" % "1.5.3",
  "org.fusesource.scalate" % "scalate-util" % "1.5.3" % "test",
  "org.scala-lang" % "scala-compiler" % v % "test",
  "org.mockito" % "mockito-core" % "1.8.5" % "test",
  "org.scala-tools.testing" % "specs_2.9.1" % "1.6.9" % "test"
) }
