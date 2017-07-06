package com.hydra.server.player

import com.hydra.server.fleet.Fleet
import com.hydra.server.galaxy.GalaxyWithPlayers
import com.hydra.server.planet.PlanetWithPlayer

case class Player(id: Int, name: String)

case class PlayerWithFleet(player: Player, fleet: Fleet)

object Player {
  def startingPlanet(galaxyWithPlayers: GalaxyWithPlayers, player: Player): Option[PlanetWithPlayer] = {
    galaxyWithPlayers.planetsWithPlayers.find(_.player.get.id == player.id)
  }
}