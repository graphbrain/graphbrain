import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "gb"
    val appVersion      = "0.1"

    val appDependencies = Seq(
      // Add your project dependencies here,
    )

    val webapp = PlayProject(
    	appName + "-common", appVersion, path = file("webapp")
  	)
  
  	val main = PlayProject(appName, appVersion, appDependencies).settings(
      // Add your own project settings here      
    ).dependsOn(
    	webapp
    )
}
