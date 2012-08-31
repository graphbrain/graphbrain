import sbt._
import Keys._
import sbtassembly.Plugin._
import AssemblyKeys._
import cc.spray.revolver.RevolverPlugin._

object GraphbrainBuild extends Build {
  lazy val hgdb = Project(id = "hgdb",
                           base = file("hgdb"),
                           settings = Defaults.defaultSettings ++ assemblySettings)

  lazy val inference = Project(id = "inference",
                           base = file("inference"))

  lazy val unfilteredScalate = Project(id = "unfiltered-scalate",
                base=file("unfiltered-scalate"))

  lazy val webapp = Project(id = "webapp",
                           base = file("webapp"),
                           settings = Defaults.defaultSettings ++ assemblySettings ++ Revolver.settings) dependsOn(hgdb, nlp, unfilteredScalate)

  lazy val braingenerators = Project(id = "braingenerators",
  							base = file("braingenerators"),
                settings = Defaults.defaultSettings ++ assemblySettings) dependsOn(hgdb)

  lazy val nlp = Project(id = "nlp",
                base = file("nlp"),
                settings = Defaults.defaultSettings ++ assemblySettings) dependsOn(hgdb, braingenerators)
  
  lazy val email = Project(id = "email",
                base = file("email"),
                settings = Defaults.defaultSettings ++ assemblySettings) dependsOn(hgdb)

  lazy val tools = Project(id = "tools",
                base=file("tools"),
                settings = Defaults.defaultSettings ++ assemblySettings) dependsOn(hgdb, nlp)

  lazy val root = Project(id = "gb",
                base = file(".")) aggregate(hgdb, inference, webapp, braingenerators, tools, nlp, unfilteredScalate)
}
