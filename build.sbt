name := "todo http4s doobie"

version := "1.0-SNAPSHOT"

scalaVersion := "2.12.4"

val Http4sVersion = "0.18.0-M7"

val DoobieVersion = "0.5.0-M11"

val H2Version = "1.4.192"

val FlywayVersion = "4.2.0"

val CirceVersion = "0.9.0-M3"

val PureConfigVersion = "0.8.0"

val LogbackVersion = "1.2.3"

libraryDependencies ++= Seq(
  "org.http4s"            %% "http4s-blaze-server"  % Http4sVersion,
  "org.http4s"            %% "http4s-circe"         % Http4sVersion,
  "org.http4s"            %% "http4s-dsl"           % Http4sVersion,

  "org.tpolecat"          %% "doobie-core"          % DoobieVersion,
  "org.tpolecat"          %% "doobie-h2"            % DoobieVersion,
  "org.tpolecat"          %% "doobie-hikari"        % DoobieVersion,

  "com.h2database"        %  "h2"                   % H2Version,

  "org.flywaydb"          %  "flyway-core"          % FlywayVersion,

  "io.circe"              %% "circe-generic"        % CirceVersion,

  "com.github.pureconfig" %% "pureconfig"           % PureConfigVersion,

  "ch.qos.logback"        %  "logback-classic"      % LogbackVersion
)