import sbt._
import Keys._
import PlayProject._


object GraphbrainBuild extends Build {
	lazy val root = Project(id = "gb",
                            base = file(".")) aggregate(inference, webapp)

    lazy val inference = Project(id = "inference",
                           base = file("inference"))

    // Play web app project
    val appName         = "GraphBrain Web App"
    val appVersion      = "1.0"

    val appDependencies = Seq(
      // Add your project dependencies here,
    )

    val webapp = PlayProject(appName, appVersion, appDependencies).settings(defaultScalaSettings:_*).settings(
      // Add your own project settings here      
    )
}

