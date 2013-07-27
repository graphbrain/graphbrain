organization := "com.graphbrain"

name := "gbdb"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.9.2"

scalacOptions += "-unchecked"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "com.typesafe.akka" % "akka-actor" % "2.0.3"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.7.1" % "test"

libraryDependencies += "me.prettyprint" % "hector-core" % "1.0-1"

libraryDependencies += "org.jsoup" % "jsoup" % "1.7.1"

libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.25"

libraryDependencies += "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.7"

//testOptions in Test += Tests.Argument("-oF")

parallelExecution in Test := false 
