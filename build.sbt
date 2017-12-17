lazy val akkaHttpVersion = "10.0.9"
lazy val akkaVersion    = "2.5.8"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "com.example",
      scalaVersion    := "2.12.4"
    )),
    name := "AkkaIntegrationHelper",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"         % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-xml"     % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream"       % akkaVersion,
      "com.typesafe.akka" %%  "akka-http-spray-json"    % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
      "org.scalatest"     %% "scalatest"         % "3.0.1"         % Test,
      "com.typesafe.akka" %% "akka-testkit"    % akkaVersion   % "test",
      "com.typesafe" 		% "config" 			% "1.3.1",
      "com.typesafe.akka" %% "akka-slf4j" % akkaVersion, 
   	  "ch.qos.logback" % "logback-classic" % "1.1.7"
    )
  )
  
// The actor tests are done in a single thread. At the end of the day,
// This part of the application is not "distributed"
parallelExecution in Test := false
