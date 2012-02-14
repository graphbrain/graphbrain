import sbt._
import Keys._


object GraphbrainBuild extends Build {
	lazy val hgdb = Project(id = "hgdb",
                           base = file("hgdb"))

  lazy val inference = Project(id = "inference",
                           base = file("inference"))

  lazy val root = Project(id = "gb",
                            base = file(".")) aggregate(hgdb, inference)
}

