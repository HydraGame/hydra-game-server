package com.hydra.server.galaxy

import com.hydra.server.planet.{Planet, PlanetWithPlayer}
import com.hydra.server.player.Player
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

object GalaxyJsonProtocol extends DefaultJsonProtocol {
  implicit val position: RootJsonFormat[Position] = jsonFormat2(Position)
  implicit val player: RootJsonFormat[Player] = jsonFormat2(Player)
  implicit val planet: RootJsonFormat[Planet] = jsonFormat(Planet, "name", "position", "population", "gold")
  implicit val planetWithPlayer: RootJsonFormat[PlanetWithPlayer] = jsonFormat(PlanetWithPlayer, "planet", "player")
  implicit val gFormat: RootJsonFormat[Galaxy] = jsonFormat(Galaxy, "name", "planets", "timer")
  implicit val gpFormat: RootJsonFormat[GalaxyWithPlayers] = jsonFormat(GalaxyWithPlayers, "name", "planetsWithPlayers", "timer", "players", "winner")
}
