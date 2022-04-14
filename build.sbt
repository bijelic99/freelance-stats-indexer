import sbtrelease.ReleaseStateTransformations._

name := """freelance-stats-indexer"""
organization := "com.freelance-stats"

scalaVersion := "2.13.6"

scalafmtOnCompile := true

githubTokenSource := TokenSource.GitConfig("github.token")

enablePlugins(BuildInfoPlugin)

buildInfoKeys := Seq[BuildInfoKey](name, version)
buildInfoPackage := "buildInfo"

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,              // : ReleaseStep
  inquireVersions,                         // : ReleaseStep
  runClean,                               // : ReleaseStep
  runTest,                                // : ReleaseStep
  setReleaseVersion,                      // : ReleaseStep
  commitReleaseVersion,                   // : ReleaseStep, performs the initial git checks
  tagRelease,                             // : ReleaseStep
  //publishArtifacts,                       // : ReleaseStep, checks whether `publishTo` is properly set up
  setNextVersion,                         // : ReleaseStep
  commitNextVersion,                      // : ReleaseStep
  pushChanges
)

lazy val AkkaVersion = "2.6.14"

libraryDependencies ++= Seq(
  "com.google.inject" % "guice" % "5.0.1",
  "ch.qos.logback" % "logback-classic" % "1.2.10",
  "com.typesafe" % "config" % "1.4.1",
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % "10.2.7",
  "com.freelance-stats" %% "alpakka-rabbitmq-client" % "0.0.3",
  "com.freelance-stats" %% "amazon-async-s3-client" % "0.0.5",
  "com.freelance-stats" %% "commons" % "0.0.19",
  "com.typesafe.play" %% "play-cache" % "2.8.13",
  "com.typesafe.play" %% "play-caffeine-cache" % "2.8.13",
  "com.github.ben-manes.caffeine" % "caffeine" % "2.8.8",
  "org.neo4j.driver" % "neo4j-java-driver" % "4.4.3",
  "io.github.neotypes" %% "neotypes-core" % "0.18.3",
  "com.sksamuel.elastic4s" %% "elastic4s-client-esjava" % "7.17.1",
  "org.typelevel" %% "cats-core" % "2.7.0"
)

resolvers ++= Seq(
  Resolver.githubPackages("bijelic99")
)

