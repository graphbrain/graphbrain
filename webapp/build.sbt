organization := "com.graphbrain"

name := "webapp"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.9.1"

libraryDependencies ++= Seq(
   "net.databinder" %% "unfiltered-netty-server" % "0.5.3",
   "net.databinder" %% "dispatch-nio" % "0.8.5",
   "org.clapper" %% "avsl" % "0.3.6",
   "net.databinder" %% "unfiltered-spec" % "0.5.3" % "test"
)

resolvers ++= Seq(
  "jboss repo" at "http://repository.jboss.org/nexus/content/groups/public-jboss/"
)