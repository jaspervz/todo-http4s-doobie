import sbt._

object Dependencies {

  /** Core dependencies */
  val CatsVersion          = "1.4.0"
  val CatsEffectVersion    = "0.10.1"

  /** Platform dependencies */
  val Http4sVersion        = "0.18.19"
  val DoobieVersion        = "0.5.3"
  val CirceVersion         = "0.9.3"
  val PureConfigVersion    = "0.9.2"
  val LogbackVersion       = "1.2.3"

  /** Test dependencies */
  val ScalaTestVersion     = "3.0.5"
  val H2Version            = "1.4.197"
  val FlywayVersion        = "5.2.0"

  /** Build dependencies */
  val KindProjectorVersion = "0.9.7"

  val coreDependencies = Seq(
    "org.typelevel"         %% "cats-core"            % CatsVersion,
    "org.typelevel"         %% "cats-macros"          % CatsVersion,
    "org.typelevel"         %% "cats-effect"          % CatsEffectVersion,
  )

  val platformDependencies = Seq(
    "org.http4s"            %% "http4s-blaze-server"  % Http4sVersion,
    "org.http4s"            %% "http4s-circe"         % Http4sVersion,
    "org.http4s"            %% "http4s-dsl"           % Http4sVersion,
    "org.tpolecat"          %% "doobie-core"          % DoobieVersion,
    "org.tpolecat"          %% "doobie-h2"            % DoobieVersion,
    "org.tpolecat"          %% "doobie-hikari"        % DoobieVersion,
    "org.flywaydb"          %  "flyway-core"          % FlywayVersion,
    "io.circe"              %% "circe-generic"        % CirceVersion,
    "com.github.pureconfig" %% "pureconfig"           % PureConfigVersion,
    "ch.qos.logback"        %  "logback-classic"      % LogbackVersion,
  )

  val testDependencies = Seq(
    "org.scalatest"         %% "scalatest"            % ScalaTestVersion  % "it,test",
    "org.http4s"            %% "http4s-blaze-client"  % Http4sVersion     % "it,test",
    "com.h2database"        %  "h2"                   % H2Version         % "it,test",
    "io.circe"              %% "circe-literal"        % CirceVersion      % "it,test",
    "io.circe"              %% "circe-optics"         % CirceVersion      % "it,test",
  )

  /** Build dependencies */
  val kindProjector =
    "org.spire-math"        %% "kind-projector"       % KindProjectorVersion
}
