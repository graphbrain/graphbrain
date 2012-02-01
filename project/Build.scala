import sbt._
import Keys._


object GraphbrainBuild extends Build {
	lazy val root = Project(id = "gb",
                            base = file(".")) aggregate(inference, web)

    lazy val inference = Project(id = "inference",
                           base = file("inference"))

    lazy val web = Project(id = "web",
                           base = file("web")) dependsOn(RootProject(uri("git://github.com/unfiltered/unfiltered-scalate.git")))
}

