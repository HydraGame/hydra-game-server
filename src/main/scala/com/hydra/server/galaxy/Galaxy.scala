package com.hydra.server.galaxy

import com.hydra.server.planet.{Planet, PlanetExpanded}
import com.hydra.server.player.Player

case class Galaxy(name: String, planets: List[Planet], timer: Int = 0) {
  private val TimerIncreaseRate = 1

  def update: Galaxy =
    Galaxy(name, planets.map(_.update), timer + TimerIncreaseRate)
}

case class GalaxyExpanded(
    name: String,
    planets: List[PlanetExpanded],
    timer: Int,
    players: List[Player],
    winner: Option[Player]
) {
  private val TimerIncreaseRate = 1

  def update: GalaxyExpanded = {
    val updatedPlanets = planets.map(_.update).map { planet =>
      planet.fleet match {
        case Some(fleet) if fleet.destinationPlanet.isDefined =>
          val player = planet.player.get
          val updatedDestinationPlanet =
            fleet.destinationPlanet.map(_.fleet = Some(fleet))
          planet.copy(fleet = None)
        case _ => planet
      }
    }

    GalaxyExpanded(
      name,
      updatedPlanets,
      timer + TimerIncreaseRate,
      players,
      winner
    )
  }
}
