val scala3Version = "3.2.2"

val catsEffectVersion = "3.4.8"
val log4CatsVersion = "2.5.0"
val munitVersion = "0.7.29"

lazy val root = project
  .in(file("."))
  .settings(
    name := "dining-philosophers",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % catsEffectVersion,
      "org.typelevel" %% "cats-effect" % catsEffectVersion,
      "org.typelevel" %% "cats-effect-kernel" % catsEffectVersion,
      "org.typelevel" %% "cats-effect-std" % catsEffectVersion,
      "org.typelevel" %% "log4cats-slf4j" % log4CatsVersion,
      "org.typelevel" %% "log4cats-core" % log4CatsVersion,
      "ch.qos.logback" % "logback-classic" % "1.2.11",
      "net.logstash.logback" % "logstash-logback-encoder" % "7.3",
      "org.typelevel" %% "munit-cats-effect-3" % "1.0.7" % Test,
      "org.scalameta" %% "munit" % munitVersion % Test,
      "org.scalameta" %% "munit-scalacheck" % "0.7.29" % Test,
      "org.scalacheck" %% "scalacheck" % "1.17.0" % Test,
      "io.chrisdavenport" %% "cats-scalacheck" % "0.3.2"
    )
  )

ThisBuild / scalacOptions -= "-explain"

lazy val enableFatalWarnings: String =
  """set ThisBuild / scalacOptions += "-Xfatal-warnings""""

lazy val disableFatalWarnings: String =
  """set ThisBuild / scalacOptions -= "-Xfatal-warnings""""

addCommandAlias("disableFatalWarnings", disableFatalWarnings)
addCommandAlias("enableFatalWarnings", enableFatalWarnings)
addCommandAlias(
  "lint",
  List(
    enableFatalWarnings,
    "scalafmtAll",
    "scalafmtSbt",
    disableFatalWarnings
  ).mkString(" ;")
)
