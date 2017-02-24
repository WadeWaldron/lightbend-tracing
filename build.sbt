name := """Tracing"""

scalaVersion := "2.12.1"

val akkaVersion = "2.4.16"

enablePlugins(Cinnamon)

cinnamon in run := true
cinnamon in test := true
cinnamonLogLevel := "INFO"

fork in run := true

scalacOptions ++= Seq("-unchecked", "-deprecation", "-Xfatal-warnings")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion withSources(),
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion withSources(),
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion withSources(),
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion withSources(),
  "com.typesafe.akka" %% "akka-cluster-sharding" % akkaVersion withSources(),
  "org.iq80.leveldb" % "leveldb" % "0.7",
  "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8",

  Cinnamon.library.cinnamonSlf4jMdc,
  Cinnamon.library.cinnamonSandbox,
  Cinnamon.library.cinnamonCHMetricsJvmMetrics,

  "ch.qos.logback" % "logback-classic" % "1.2.1",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)

addCommandAlias("consume", "runMain com.lightbend.tracing.Consumer -Dakka.remote.netty.tcp.port=2551")
addCommandAlias("produce", "runMain com.lightbend.tracing.Producer -Dakka.remote.netty.tcp.port=2552")
addCommandAlias("consume2", "runMain com.lightbend.tracing.Consumer -Dakka.remote.netty.tcp.port=2553")
addCommandAlias("produce2", "runMain com.lightbend.tracing.Producer -Dakka.remote.netty.tcp.port=2554")
addCommandAlias("blocking", "runMain com.lightbend.tracing.Blocking -Dakka.remote.netty.tcp.port=2555")
addCommandAlias("failing", "runMain com.lightbend.tracing.Failing -Dakka.remote.netty.tcp.port=2556")
