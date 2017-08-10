name := "hydra-game-server"

version := "0.1"

scalaVersion := "2.12.2"

libraryDependencies ++= Seq(
  "io.spray" %%  "spray-json" % "1.3.3",
  "net.debasishg" %% "redisclient" % "3.4"
)
