import sbtassembly.Plugin._
import AssemblyKeys._

organization := "com.graphbrain"

name := "inference"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.9.1"

scalacOptions += "-unchecked"



libraryDependencies += "org.scalatest" %% "scalatest" % "1.7.1" % "test"