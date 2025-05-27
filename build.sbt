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
    socksCore.jvm,
    socksCore.js,
    socks5,
  )
  .settings(commonSettings)
  .settings(
    name := "socks",
  )

lazy val socksCore = (crossProject(JSPlatform, JVMPlatform) in file("socks-core"))
  .settings(commonSettings)
  .settings(
    name := "socks-core",
    libraryDependencies ++= Seq(
      "com.peknight" %%% "error-core" % pekErrorVersion,
    ),
  )

lazy val socks5 = (project in file("socks5"))
  .aggregate(
    socks5Core.jvm,
    socks5Core.js,
    socks5Api.jvm,
    socks5Api.js,
    socks5Server,
    socks5Client,
  )
  .settings(commonSettings)
  .settings(
    name := "socks5",
  )

lazy val socks5Core = (crossProject(JSPlatform, JVMPlatform) in file("socks5/core"))
  .dependsOn(socksCore)
  .settings(commonSettings)
  .settings(
    name := "socks5-core",
    libraryDependencies ++= Seq(
      "co.fs2" %%% "fs2-core" % fs2Version,
    ),
  )

lazy val socks5Api = (crossProject(JSPlatform, JVMPlatform) in file("socks5/api"))
  .dependsOn(socks5Core)
  .settings(commonSettings)
  .settings(
    name := "socks5-api",
    libraryDependencies ++= Seq(
    ),
  )

lazy val socks5Server = (project in file("socks5/server"))
  .aggregate(
    socks5ServerCore.jvm,
    socks5ServerCore.js,
    socks5ServerFs2IO.jvm,
    socks5ServerFs2IO.js,
  )
  .settings(commonSettings)
  .settings(
    name := "socks5-server",
  )

lazy val socks5ServerCore = (crossProject(JSPlatform, JVMPlatform) in file("socks5/server/core"))
  .dependsOn(socks5Api)
  .settings(commonSettings)
  .settings(
    name := "socks5-server-core",
    libraryDependencies ++= Seq(
    ),
  )

lazy val socks5ServerFs2IO = (crossProject(JSPlatform, JVMPlatform) in file("socks5/server/fs2-io"))
  .dependsOn(socks5ServerCore)
  .settings(commonSettings)
  .settings(
    name := "socks5-server-fs2-io",
    libraryDependencies ++= Seq(
      "co.fs2" %%% "fs2-io" % fs2Version,
    ),
  )

lazy val socks5Client = (project in file("socks5/client"))
  .aggregate(
    socks5ClientCore.jvm,
    socks5ClientCore.js,
  )
  .settings(commonSettings)
  .settings(
    name := "socks5-client",
  )

lazy val socks5ClientCore = (crossProject(JSPlatform, JVMPlatform) in file("socks5/client/core"))
  .dependsOn(socks5Api)
  .settings(commonSettings)
  .settings(
    name := "socks5-client-core",
    libraryDependencies ++= Seq(
    ),
  )

val fs2Version = "3.12.0"
val pekVersion = "0.1.0-SNAPSHOT"
val pekExtVersion = pekVersion
val pekErrorVersion = pekVersion
