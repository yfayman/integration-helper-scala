import sbt._
import Keys._

object Dependencies {

  lazy val akkaHttpVersion = "10.0.9"
  lazy val akkaVersion = "2.5.8"

  val commonDependencies: Seq[ModuleID] = Seq(
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
    "org.scalatest" %% "scalatest" % "3.0.1" % Test,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
    "com.typesafe" % "config" % "1.3.1",
    "com.typesafe.akka" %% "akka-stream"       % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "ch.qos.logback" % "logback-classic" % "1.1.7"
  )

  val coreDependencies: Seq[ModuleID] = commonDependencies

  val akkaHttpDependencies: Seq[ModuleID] = commonDependencies ++ Seq(
      "com.typesafe.akka" %% "akka-http"         % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-xml"     % akkaHttpVersion,
      "com.typesafe.akka" %%  "akka-http-spray-json"    % akkaHttpVersion
  )
 

}
