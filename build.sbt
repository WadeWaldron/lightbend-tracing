name := """Tracing"""

//scalaVersion := "2.11.7"
scalaVersion := "2.12.1"

val akkaVersion = "2.4.16"

enablePlugins(Cinnamon)

cinnamon in run := true
cinnamon in test := true

cinnamonLogLevel := "INFO"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion withSources(),
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion withSources(),
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion withSources(),
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion withSources(),
  "com.typesafe.akka" %% "akka-cluster-sharding" % akkaVersion withSources(),
  "org.iq80.leveldb" % "leveldb" % "0.7",
  "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8",

  Cinnamon.library.cinnamonSlf4jMdc,

  "ch.qos.logback" % "logback-classic" % "1.2.1",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)
