import sbt._
import Keys._

object GraphbrainBuild extends Build {

  lazy val hgdb = Project(id = "hgdb",
                           base = file("hgdb"))

  lazy val inference = Project(id = "inference",
                           base = file("inference"))

  lazy val webapp = Project(id = "webapp",
                           base = file("webapp"))

  lazy val brain_generators = Project(id="brain-generators",
  							base=file("brain-generators")) dependsOn(hgdb)

  lazy val root = Project(id = "gb",
                            base = file(".")) aggregate(hgdb, inference, webapp)
}