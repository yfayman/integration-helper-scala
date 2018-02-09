
lazy val akkaHttpVersion = "10.0.9"
lazy val akkaVersion    = "2.5.8"

// The actor tests are done in a single thread. At the end of the day,
// This part of the application is not "distributed"

name := MyBuild.NamePrefix + "root"

lazy val core = project.
	settings(Common.settings: _*).
	settings(libraryDependencies ++= Dependencies.coreDependencies)
	
lazy val akkaHttp = project.
	dependsOn(core).
	dependsOn(core % "test->test").
	settings(Common.settings: _*).
	settings(libraryDependencies ++= Dependencies.akkaHttpDependencies)
	
lazy val jdbc = project.
	dependsOn(core).
	settings(Common.settings: _*)
	
lazy val root = (project in file(".")).
    aggregate(core, akkaHttp, jdbc)

