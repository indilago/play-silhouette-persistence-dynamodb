import com.typesafe.sbt.SbtScalariform._
import xerial.sbt.Sonatype._

import scalariform.formatter.preferences._

//*******************************
// Play settings
//*******************************

name := "play-silhouette-persistence-dynamodb"

version := "0.1"

scalaVersion := "2.12.4"

crossScalaVersions := Seq("2.12.4", "2.11.12")

resolvers += Resolver.jcenterRepo

resolvers += "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

val SilhoetteVersion = "5.0.3"

libraryDependencies ++= Seq(
  "com.mohiva" %% "play-silhouette" % SilhoetteVersion,
  "com.mohiva" %% "play-silhouette-persistence" % SilhoetteVersion,
  "net.codingwell" %% "scala-guice" % "4.1.1" % "test",
  "com.amazonaws" % "aws-java-sdk-dynamodb" % "1.11.297",
  specs2 % Test
)

lazy val root = (project in file(".")).enablePlugins(PlayScala).disablePlugins(PlayLayoutPlugin)

//*******************************
// Compiler settings
//*******************************

scalacOptions ++= Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Xfatal-warnings", // Fail the compilation if there are any warnings.
  "-Xlint", // Enable recommended additional warnings.
  "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver.
  "-Ywarn-dead-code", // Warn when dead code is identified.
  "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
  "-Ywarn-nullary-override", // Warn when non-nullary overrides nullary, e.g. def foo() over def foo.
  "-Ywarn-numeric-widen" // Warn when numerics are widened.
)

//*******************************
// Test settings
//*******************************

parallelExecution in Test := false

fork in Test := true

// Needed to avoid https://github.com/travis-ci/travis-ci/issues/3775 in forked tests
// in Travis with `sudo: false`.
// See https://github.com/sbt/sbt/issues/653
// and https://github.com/travis-ci/travis-ci/issues/3775
javaOptions += "-Xmx1G"

//*******************************
// Maven settings
//*******************************

sonatypeSettings

organization := "com.indilago"

description := "DynamoDB persistence module for Silhouette"

homepage := Some(url("https://indilago.com/"))

licenses := Seq("Apache License" -> url("https://github.com/indilago/play-silhouette-persistence-dynamodb/blob/master/LICENSE"))

val pom = <scm>
    <url>git@github.com:indilago/play-silhouette-persistence-dynamodb.git</url>
    <connection>scm:git:git@github.com:indilago/play-silhouette-persistence-dynamodb.git</connection>
  </scm>
    <developers>
      <developer>
        <id>kbanman</id>
        <name>Kelly Banman</name>
        <url>http://indilago.com</url>
      </developer>
    </developers>;

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

sources in (Compile,doc) := Seq.empty

pomExtra := pom

//********************************************************
// Scalariform settings
//********************************************************

defaultScalariformSettings

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(FormatXml, false)
  .setPreference(DoubleIndentClassDeclaration, false)
  .setPreference(DanglingCloseParenthesis, Preserve)
