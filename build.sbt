name := """time-spot"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"
herokuJdkVersion in Compile := "1.8"
herokuAppName in Compile := "time-spot"

libraryDependencies ++= Seq(
  jdbc,
  "org.postgresql" % "postgresql" % "9.4-1205-jdbc42",
  "org.sorm-framework" % "sorm" % "0.3.19",
  "org.webjars" % "jquery" % "2.1.4",
  "org.webjars" % "bootstrap" % "3.3.5",
  "com.google.code.gson" % "gson" % "2.4",
  "com.github.nscala-time" %% "nscala-time" % "2.12.0",
  "com.cloudinary" %% "cloudinary-scala-play" % "1.2.1",
  "org.eclipse.paho" % "org.eclipse.paho.client.mqttv3" % "1.1.1",
  "com.tzavellas" %% "sse-guice" % "0.7.2",
  "com.google.inject" % "guice"  % "4.1.0",
  "com.typesafe.play" %% "play-json" % "2.7.3",
  "com.typesafe.play" %% "play-json-joda" % "2.7.3",
  "org.joda" % "joda-convert" % "1.8.1",
  "com.h2database" % "h2" % "1.4.193",
  jodaForms,
  guice,
  cacheApi,
  ws,
  specs2 % Test
)

dependencyOverrides += "org.scala-lang" % "scala-compiler" % scalaVersion.value
resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.

routesGenerator := InjectedRoutesGenerator
routesGenerator := StaticRoutesGenerator

