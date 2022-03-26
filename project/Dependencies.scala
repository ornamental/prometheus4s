import sbt._

object Dependencies {

  val catsCore = "org.typelevel" %% "cats-core" % "2.6.1"
  val catsEffect = "org.typelevel" %% "cats-effect" % "3.3.0"

  val shapeless = "com.chuusai" %% "shapeless" % "2.3.7"

  val supertagged = "org.rudogma" %% "supertagged" % "2.0-RC1"

  val prometheusClientVersion = "0.12.0"
  val prometheusClient = "io.prometheus" % "simpleclient" % prometheusClientVersion
  val prometheusClientHttp =
    "io.prometheus" % "simpleclient_httpserver" % prometheusClientVersion
}
