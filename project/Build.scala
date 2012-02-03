import sbt._
import Keys._


object GraphbrainBuild extends Build {
	lazy val root = Project(id = "gb",
                            base = file(".")) aggregate(inference, web)

    lazy val inference = Project(id = "inference",
                           base = file("inference"))
}

