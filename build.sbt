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
  "com.freelance-stats" %% "commons" % "0.0.11",
  "org.apache.tika" % "tika-core" % "2.2.1"
)

resolvers ++= Seq(
  Resolver.githubPackages("bijelic99")
)

