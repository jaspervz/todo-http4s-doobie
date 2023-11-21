import sbt._

object Dependencies {

  /** Language dependencies */
  val ScalaLanguageVersion = "3.3.1"

  /** Platform dependencies */
  val Http4sVersion        = "0.23.23"
  val DoobieVersion        = "1.0.0-RC5"
  val CirceVersion         = "0.14.6"
  val PureConfigVersion    = "0.17.4"
  val LogbackVersion       = "1.4.11"

  /** Test dependencies */
  val ScalaTestVersion     = "3.2.17"
  val H2Version            = "2.2.224"
  val FlywayVersion        = "10.0.1"
  val CatsEffectTestKit    = "1.5.0"

  /** Build dependencies */
  val KindProjectorVersion = "0.13.2"

  val platformDependencies = Seq(
    "org.http4s"            %% "http4s-ember-server"  % Http4sVersion,
    "org.http4s"            %% "http4s-circe"         % Http4sVersion,
    "org.http4s"            %% "http4s-dsl"           % Http4sVersion,
    "org.tpolecat"          %% "doobie-core"          % DoobieVersion,
    "org.tpolecat"          %% "doobie-h2"            % DoobieVersion,
    "org.tpolecat"          %% "doobie-hikari"        % DoobieVersion,
    "org.flywaydb"          %  "flyway-core"          % FlywayVersion,
    "io.circe"              %% "circe-generic"        % CirceVersion,
    "com.github.pureconfig" %% "pureconfig-core"      % PureConfigVersion,
    "ch.qos.logback"        %  "logback-classic"      % LogbackVersion,
  )

  val testDependencies = Seq(
    "org.scalatest"         %% "scalatest"                      % ScalaTestVersion,
    "org.http4s"            %% "http4s-ember-client"            % Http4sVersion,
    "com.h2database"        %  "h2"                             % H2Version,
    "io.circe"              %% "circe-literal"                  % CirceVersion,
    "org.typelevel"         %% "cats-effect-testing-scalatest"  % CatsEffectTestKit,
  ).map(_ % "it,test")
}
