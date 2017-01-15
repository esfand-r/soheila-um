import sbt.Keys.scalacOptions
import org.scoverage.coveralls.Imports.CoverallsKeys._
import com.typesafe.sbt.SbtScalariform._
import xerial.sbt.Sonatype._
import org.scoverage.coveralls.Imports.CoverallsKeys._
import scalariform.formatter.preferences._

name := "soheila-um"

organization := "io.soheila"

description := "User management"

homepage := Some(url("http://www.soheila.io/"))

licenses := Seq("Apache2 License" -> url("https://www.apache.org/licenses/LICENSE-2.0"))

version := "0.1.0-alpha1"

scalaVersion := "2.11.8"

aggregate in update := false

lazy val commonSettings = Seq(
  organization := "org.soheila",
  version := "0.1.0",
  scalaVersion := "2.11.8",
  aggregate in update := false
)

version := "0.1.0-alpha1"

scalaVersion := "2.11.8"

aggregate in update := true

resolvers += Resolver.jcenterRepo

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= Seq(
  "net.codingwell" % "scala-guice_2.11" % "4.1.0",
  "com.enragedginger" % "akka-quartz-scheduler_2.11" % "1.6.0-akka-2.4.x",
  "com.typesafe.akka" % "akka-actor_2.11" % "2.4.17",
  "com.typesafe.akka" %% "akka-persistence" % "2.4.17",
  "com.typesafe.akka" % "akka-stream_2.11" % "2.4.17",
  "com.typesafe.akka" %% "akka-testkit" % "2.4.17",
  "com.typesafe.akka" %% "akka-slf4j" % "2.4.17",
  "org.sangria-graphql" % "sangria_2.11" % "1.0.0", // SimpleFetcherCache does not cache Relation and was fixed in snapshot
  "org.sangria-graphql" % "sangria-play-json_2.11" % "1.0.0",
  // Security
  "com.mohiva" % "play-silhouette_2.11" % "4.0.0",
  "com.mohiva" % "play-silhouette-password-bcrypt_2.11" % "4.0.0",
  "com.mohiva" % "play-silhouette-persistence_2.11" % "4.0.0",
  "com.mohiva" % "play-silhouette-crypto-jca_2.11" % "4.0.0",
  "com.mohiva" % "play-silhouette-persistence-reactivemongo_2.11" % "4.0.0",
  "com.iheart" % "ficus_2.11" % "1.4.0",
  "io.soheila" % "play-reactivemongo-commons_2.11" % "0.1.0-alpha1",
  "com.github.romix.akka" % "akka-kryo-serialization_2.11" % "0.5.0",

  "de.flapdoodle.embed" % "de.flapdoodle.embed.mongo" % "2.0.0" % Test,
  "com.mohiva" %% "play-silhouette-testkit" % "4.0.0" % Test,
  "net.codingwell" % "scala-guice_2.11" % "4.1.0" % Test,
  specs2 % Test,
  "org.specs2" %% "specs2-core" % "3.8.6" % "test",
  "com.typesafe.play" % "play-specs2_2.11" % "2.5.12"

)

libraryDependencies ~= { _.map(_.exclude("org.slf4j", "slf4j-simple")) }
libraryDependencies ~= { _.map(_.exclude("org.specs2", "specs2-core")) }

lazy val `soheila-um` = Project(id = "soheila-um", base = file("."))
  .enablePlugins(PlayScala, JavaAppPackaging).disablePlugins(PlayLayoutPlugin)
  .settings(commonSettings: _*)

javaOptions in Test += "-Dconfig.file=src/test/resources/application.test.conf"

parallelExecution in Test := false

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

scalacOptions in(Compile, doc) ++= Seq(
  "-no-link-warnings" // Suppresses problems with Scaladoc @throws links
)

sonatypeSettings

val pom = <scm>
  <url>git@github.com:esfand-r/soheila-um.git</url>
  <connection>scm:git@github.com:esfand-r/soheila-um.git</connection>
</scm>
  <developers>
    <developer>
      <id>esfand-r</id>
      <name>Esfandiar Amirrahimi</name>
      <url>http://soheila.io</url>
    </developer>
  </developers>;

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

sources in (Compile,doc) := Seq.empty

pomExtra := pom

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

coverallsToken := sys.env.get("COVERALLS_REPO_TOKEN")

addCommandAlias("build",       ";clean;coverage;test;format;coverageReport")
addCommandAlias("deployBuild", ";clean;coverage;test;format;coverageReport;coverageAggregate;coveralls")
