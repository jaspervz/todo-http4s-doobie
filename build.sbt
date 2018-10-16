import Dependencies._

lazy val commonSettings = Seq(
  name         := "ms-contact",
  version      := "0.1.0-SNAPSHOT",
  scalaVersion := "2.12.7"
)

scalacOptions := Seq(
  "-unchecked",
  "-deprecation",
  "-feature",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-Ypartial-unification",
  "-Ywarn-unused-import"
)

lazy val `ms-contact` = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    commonSettings,
    Defaults.itSettings,
    dependencyOverrides ++= coreDependencies,
    libraryDependencies ++= platformDependencies ++ testDependencies
  )
