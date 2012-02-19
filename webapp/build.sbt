organization := "com.graphbrain"

name := "webapp"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.9.1"

scalacOptions += "-deprecation"

libraryDependencies ++= Seq(
   "net.databinder" %% "unfiltered-netty-server" % "0.5.3",
   "net.databinder" %% "dispatch-nio" % "0.8.5",
   "org.clapper" %% "avsl" % "0.3.6",
   "net.databinder" %% "unfiltered-spec" % "0.5.3" % "test"
)

resolvers ++= Seq(
  "jboss repo" at "http://repository.jboss.org/nexus/content/groups/public-jboss/"
)

seq(jsSettings : _*)

(sourceDirectory in (Compile, JsKeys.js)) <<= (sourceDirectory in Compile)(_ / "js")

(resourceManaged in (Compile, JsKeys.js)) <<= (sourceDirectory in Compile)(_  / "resources" / "js")

(compile in Compile) <<= compile in Compile dependsOn (JsKeys.js in Compile)

(compile in Compile) <<= compile in Compile dependsOn (CSS.css in Compile)