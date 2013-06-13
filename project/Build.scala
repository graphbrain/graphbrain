import sbt._
import Keys._
import sbtassembly.Plugin._
import AssemblyKeys._
import cc.spray.revolver.RevolverPlugin._

object GraphbrainBuild extends Build {
  lazy val gbdb = Project(id = "gbdb",
                           base = file("gbdb"),
                           settings = Defaults.defaultSettings ++ assemblySettings) dependsOn(utils)

  lazy val inference = Project(id = "inference",
                           base = file("inference"))

  lazy val unfilteredScalate = Project(id = "unfiltered-scalate",
                base=file("unfiltered-scalate"))

  lazy val webapp = Project(id = "webapp",
                           base = file("webapp"),
                           settings = Defaults.defaultSettings ++ assemblySettings ++ Revolver.settings) dependsOn(gbdb, nlp, unfilteredScalate, utils)

  lazy val braingenerators = Project(id = "braingenerators",
  							base = file("braingenerators"),
                settings = Defaults.defaultSettings ++ assemblySettings) dependsOn(gbdb)

  lazy val nlp = Project(id = "nlp",
                base = file("nlp"),
                settings = Defaults.defaultSettings ++ assemblySettings) dependsOn(gbdb, braingenerators)
  
  lazy val email = Project(id = "email",
                base = file("email"),
                settings = Defaults.defaultSettings ++ assemblySettings) dependsOn(gbdb)

  lazy val tools = Project(id = "tools",
                base=file("tools"),
                settings = Defaults.defaultSettings ++ assemblySettings) dependsOn(gbdb, nlp)

  lazy val utils = Project(id = "utils", base=file("utils"))

  lazy val root = Project(id = "gb",
                base = file(".")) aggregate(gbdb, inference, webapp, braingenerators, tools, nlp, unfilteredScalate, utils)
}
