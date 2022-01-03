 import Dependencies.{kindProjector, _}

lazy val commonSettings = Seq(
  name         := "fpa-ms-contact",
  version      := "0.1.0",
  scalaVersion := "2.13.7"
)

scalacOptions := Seq(
  "-unchecked",
  "-deprecation",
  "-feature",
  "-language:higherKinds",
  "-language:implicitConversions"
)

lazy val `fpa-ms-contact` = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    commonSettings,
    Defaults.itSettings,
    libraryDependencies ++= platformDependencies ++ testDependencies,
    addCompilerPlugin(kindProjector)
  )
