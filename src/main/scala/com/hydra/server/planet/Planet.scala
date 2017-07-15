package com.hydra.server.planet

import com.hydra.server.ships.Fleet
import com.hydra.server.galaxy.Position
import com.hydra.server.player.Player

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

case class PlanetWithPlayer(planet: Planet, player: Option[Player]) {
  def update: PlanetWithPlayer = PlanetWithPlayer(planet.update, player)
}

case class PlanetExpanded(planet: Planet, player: Option[Player], fleet: Option[Fleet]) {
  def update: PlanetExpanded = PlanetExpanded(planet.update, player, fleet)
}


