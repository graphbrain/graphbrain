import sbt._
import Keys._

object CSS {
  val css = TaskKey[Unit]("css", "Aggregates css files")

  val cssTask = css := {
  	val dir = new File("webapp/src/main/css")
  	val sources = dir.listFiles
  	val code = (for (s <- sources) yield {
  		println("merging CSS file: " + s.getAbsolutePath())
  		io.Source.fromFile(s).mkString
  	}).reduceLeft(_ + _)
	  val out = new java.io.FileWriter("webapp/src/main/resources/css/main.css")
	  out.write(code)
	  out.close
  }
}