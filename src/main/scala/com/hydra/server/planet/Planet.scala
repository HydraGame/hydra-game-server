package com.hydra.server.planet

import com.hydra.server.fleet.{Fleet, FleetWithPosition}
import com.hydra.server.galaxy.Position
import com.hydra.server.player.{Player, PlayerWithFleet}

case class Planet(
  name: String,
  position: Position,
  population: Int = 0,
  gold: Int = 0
) {
  private val PopulationGrowthRate = 1
  private val GoldGrowthRate = 137

  def update: Planet = Planet(name, position, population + PopulationGrowthRate, gold + GoldGrowthRate)
}

case class PlanetWithPlayer(planet: Planet, player: Option[Player], fleet: Option[FleetWithPosition] = None) {
  def update: PlanetWithPlayer = PlanetWithPlayer(planet.update, player)
  def withFleet(fleet: Fleet): PlanetWithPlayer =
    PlanetWithPlayer(planet, player, Some(FleetWithPosition(fleet, planet.position, inTransit = false, Some(planet), None)))
}

