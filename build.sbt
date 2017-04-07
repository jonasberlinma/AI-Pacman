
name := "AI-Pacman"

version := "1.0"

scalaVersion := "2.12.1"
libraryDependencies += "org.scalatest" % "scalatest_2.12" % "3.0.1" % "test"

javacOptions ++= Seq("-source", "1.8")

compileOrder := CompileOrder.JavaThenScala

unmanagedSourceDirectories in Compile <<= Seq(javaSource in Compile).join

mainClass in (Compile, run) := Some("edu.ucsb.cs56.projects.games.pacman.PacMan")

resourceDirectory in Compile := baseDirectory.value / "assets"
