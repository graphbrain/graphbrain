resolvers ++= Seq(
  "coda" at "http://repo.codahale.com")

addSbtPlugin("me.lessis" % "ls-sbt" % "0.1.1")

resolvers <+= "less is" at "http://repo.lessis.me"