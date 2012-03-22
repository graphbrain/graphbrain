import sbt._
import Keys._


import sbtassembly.Plugin._
import AssemblyKeys._

object GraphbrainBuild extends Build {
  lazy val hgdb = Project(id = "hgdb",
                           base = file("hgdb"),
                           settings = Defaults.defaultSettings ++ assemblySettings)

  lazy val inference = Project(id = "inference",
                           base = file("inference"))

  lazy val webapp = Project(id = "webapp",
                           base = file("webapp"),
                           settings = Defaults.defaultSettings ++ assemblySettings) dependsOn(hgdb)

  lazy val brain_generators = Project(id = "braingenerators",
  							base = file("braingenerators"),
                settings = Defaults.defaultSettings ++ assemblySettings) dependsOn(hgdb, searchengine)

  lazy val tools = Project(id = "tools",
                base=file("tools"),
                settings = Defaults.defaultSettings ++ assemblySettings) dependsOn(hgdb, searchengine)

  lazy val searchengine = Project(id = "searchengine",
                base=file("searchengine"),
                settings = Defaults.defaultSettings ++ assemblySettings) dependsOn(hgdb)

  lazy val root = Project(id = "gb",
                base = file(".")) aggregate(hgdb, inference, webapp, brain_generators, tools, searchengine)
}