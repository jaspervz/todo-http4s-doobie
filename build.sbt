lazy val commonSettings = Seq(
  name := "todo http4s doobie",
  version := "1.0-SNAPSHOT",
  scalaVersion := "2.13.15",
  scalacOptions ++= Seq(
    "-deprecation",
    "-Xfatal-warnings",
    "-Ywarn-value-discard",
    "-Xlint:missing-interpolator"
  ),
)

lazy val Http4sVersion = "0.23.30"

lazy val DoobieVersion = "1.0.0-RC6"

lazy val H2Version = "2.3.232"

lazy val FlywayVersion = "11.1.0"

lazy val CirceVersion = "0.14.10"

lazy val CirceOpticsVersion = "0.15.0"

lazy val PureConfigVersion = "0.17.8"

lazy val LogbackVersion = "1.5.15"

lazy val JansiVersion = "2.4.1"

lazy val ScalaTestVersion = "3.2.19"

lazy val ScalaMockVersion = "6.0.0"

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    commonSettings,
    Defaults.itSettings,
    libraryDependencies ++= Seq(
      "org.http4s"            %% "http4s-ember-server"  % Http4sVersion,
      "org.http4s"            %% "http4s-circe"         % Http4sVersion,
      "org.http4s"            %% "http4s-dsl"           % Http4sVersion,
      "org.http4s"            %% "http4s-ember-client"  % Http4sVersion     % "it,test",

      "org.tpolecat"          %% "doobie-core"          % DoobieVersion,
      "org.tpolecat"          %% "doobie-h2"            % DoobieVersion,
      "org.tpolecat"          %% "doobie-hikari"        % DoobieVersion,

      "com.h2database"        %  "h2"                   % H2Version,

      "org.flywaydb"          %  "flyway-core"          % FlywayVersion,

      "io.circe"              %% "circe-generic"        % CirceVersion,
      "io.circe"              %% "circe-literal"        % CirceVersion        % "it,test",
      "io.circe"              %% "circe-optics"         % CirceOpticsVersion  % "it",

      "com.github.pureconfig" %% "pureconfig"             % PureConfigVersion,
      "com.github.pureconfig" %% "pureconfig-cats-effect" % PureConfigVersion,

      "ch.qos.logback"        %  "logback-classic"      % LogbackVersion,
      "org.fusesource.jansi"  % "jansi"                 % JansiVersion,

      "org.scalatest"         %% "scalatest"            % ScalaTestVersion  % "it,test",
      "org.scalamock"         %% "scalamock"            % ScalaMockVersion  % "test"
    )
  )
