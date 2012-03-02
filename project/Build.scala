import sbt._
import Keys._

object GraphbrainBuild extends Build {
  def standardSettings = Seq(
    exportJars := true
  ) ++ Defaults.defaultSettings

  lazy val hgdb = Project(id = "hgdb",
                           base = file("hgdb"))

  lazy val inference = Project(id = "inference",
                           base = file("inference"))

  lazy val webapp = Project(id = "webapp",
                           base = file("webapp")) dependsOn(hgdb)

  lazy val brain_generators = Project(id="brain-generators",
  							base=file("brain-generators"),
                settings = standardSettings) dependsOn(hgdb)

  lazy val root = Project(id = "gb",
                            base = file(".")) aggregate(hgdb, inference, webapp, brain_generators)
}