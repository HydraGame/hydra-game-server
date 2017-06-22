package com.hydra.server.galaxy

import com.hydra.server.planet.{Planet, Position}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

object GalaxyJsonProtocol extends DefaultJsonProtocol {
  implicit val position: RootJsonFormat[Position] = jsonFormat2(Position)
  implicit val planet: RootJsonFormat[Planet] = jsonFormat(Planet, "name", "position", "population", "gold")
  implicit val gFormat: RootJsonFormat[Galaxy] = jsonFormat(Galaxy, "name", "planets", "timer")
}
