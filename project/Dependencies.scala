import sbt._

object Dependencies {

  /** Platform dependencies */
  val Http4sVersion        = "0.23.7"
  val DoobieVersion        = "1.0.0-RC1"
  val CirceVersion         = "0.14.1"
  val PureConfigVersion    = "0.17.1"
  val LogbackVersion       = "1.2.10"

  /** Test dependencies */
  val ScalaTestVersion     = "3.2.10"
  val H2Version            = "2.0.202"
  val FlywayVersion        = "8.3.0"
  val ScalaMockVersion     = "5.2.0"
  val CatsEffectTestKit    = "1.4.0"

  /** Build dependencies */
  val KindProjectorVersion = "0.13.2"

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
    "org.scalatest"         %% "scalatest"                      % ScalaTestVersion,
    "org.http4s"            %% "http4s-blaze-client"            % Http4sVersion,
    "com.h2database"        %  "h2"                             % H2Version,
    "io.circe"              %% "circe-literal"                  % CirceVersion,
    "io.circe"              %% "circe-optics"                   % CirceVersion,
    "org.typelevel"         %% "cats-effect-testing-scalatest"  % CatsEffectTestKit,
    "org.scalamock"         %% "scalamock"                      % ScalaMockVersion,
  ).map(_ % "it,test")

  val kindProjector =
    "org.typelevel"         %% "kind-projector"  % KindProjectorVersion cross CrossVersion.full
}
