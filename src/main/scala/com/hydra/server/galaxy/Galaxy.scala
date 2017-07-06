package com.hydra.server.galaxy

import com.hydra.server.planet.{Planet, PlanetWithPlayer}
import com.hydra.server.player.Player

case class Galaxy(name: String, planets: List[Planet], timer: Int = 0) {
  private val TimerIncreaseRate = 1

  def update: Galaxy = Galaxy(name, planets.map(_.update), timer + TimerIncreaseRate)
}

case class GalaxyWithPlayers(name: String, planetsWithPlayers: List[PlanetWithPlayer], timer: Int, players: List[Player], winner: Option[Player]) {
  private val TimerIncreaseRate = 1

  def update: GalaxyWithPlayers = GalaxyWithPlayers(name, planetsWithPlayers.map(_.update), timer + TimerIncreaseRate, players, winner)
}