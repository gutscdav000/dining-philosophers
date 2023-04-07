val scala3Version = "3.2.2"

val catsEffectVersion = "3.4.8"

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
      "org.typelevel" %% "munit-cats-effect-3" % "1.0.7" % Test,
      "org.scalameta" %% "munit" % "0.7.29" % Test
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
