import sbt._
import Keys._


object GraphbrainBuild extends Build {
	lazy val core = Project(id = "core",
                           base = file("core"))

  lazy val inference = Project(id = "inference",
                           base = file("inference"))

  lazy val root = Project(id = "gb",
                            base = file(".")) aggregate(core, inference)

  
}

