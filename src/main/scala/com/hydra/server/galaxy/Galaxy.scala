package com.hydra.server.galaxy

import com.hydra.server.planet.{Planet, PlanetExpanded}
import com.hydra.server.player.Player

case class Galaxy(name: String, planets: List[Planet], timer: Int = 0) {
  private val TimerIncreaseRate = 1

  def update: Galaxy = Galaxy(name, planets.map(_.update), timer + TimerIncreaseRate)
}

case class GalaxyExpanded(name: String, planets: List[PlanetExpanded], timer: Int, players: List[Player], winner: Option[Player]) {
  private val TimerIncreaseRate = 1

  def update: GalaxyExpanded = GalaxyExpanded(name, planets.map(_.update), timer + TimerIncreaseRate, players, winner)
}