ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.7.0"

ThisBuild / organization := "com.peknight"

lazy val commonSettings = Seq(
  scalacOptions ++= Seq(
    "-feature",
    "-deprecation",
    "-unchecked",
    "-Xfatal-warnings",
    "-language:strictEquality",
    "-Xmax-inlines:64"
  ),
)

lazy val socks = (project in file("."))
  .aggregate(
    socks5
  )
  .settings(commonSettings)
  .settings(
    name := "socks",
  )

lazy val socks5 = (project in file("socks5"))
  .aggregate(
    socks5Core.jvm,
    socks5Core.js,
  )
  .settings(commonSettings)
  .settings(
    name := "socks5",
  )

lazy val socks5Core = (crossProject(JSPlatform, JVMPlatform) in file("socks5/core"))
  .settings(commonSettings)
  .settings(
    name := "socks5-core",
    libraryDependencies ++= Seq(
      "co.fs2" %%% "fs2-io" % fs2Version,
    ),
  )

val fs2Version = "3.12.0"
