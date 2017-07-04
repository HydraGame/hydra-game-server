package com.hydra.server.planet

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

case class Position(x: Int, y: Int)

case class PlanetWithPlayer(planet: Planet, player: Option[Player]) {
  def update: PlanetWithPlayer = PlanetWithPlayer(planet.update, player)
}

