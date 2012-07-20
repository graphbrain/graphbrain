resolvers += Resolver.url(
  "sbt-plugin-releases", 
  new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/")
)(Resolver.ivyStylePatterns)

resolvers += "spray repo" at "http://repo.spray.cc"

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.8.1")

addSbtPlugin("cc.spray" % "sbt-revolver" % "0.6.1")

addSbtPlugin("com.github.retronym" % "sbt-onejar" % "0.6")