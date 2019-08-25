name := "propagating-execution-context"
scalaVersion := "2.13.0"
libraryDependencies ++= Seq(
  "org.slf4j"     % "slf4j-api"     % "1.7.28",
  "org.slf4j"     % "slf4j-log4j12" % "1.7.28" % Test,
  "org.scalatest" %% "scalatest"    % "3.0.8" % Test
)
