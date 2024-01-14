package com.hydra.server.ships

import com.hydra.server.galaxy.Position
import com.hydra.server.planet.Planet

sealed trait Ship {
  def name: String

  def attack: Int

  def defense: Int

  def velocity: Int
}

case class BattleCruiser(
    name: String = "Battle Cruiser",
    attack: Int = 100,
    defense: Int = 95,
    velocity: Int = 6
) extends Ship

case class ColonyShip(
    name: String = "Colony Ship",
    attack: Int = 1,
    defense: Int = 10,
    velocity: Int = 5
) extends Ship

case class Squad(ship: Ship, count: Int) {
  lazy val attack: Int = ship.attack * count
  lazy val defense: Int = ship.defense * count
  lazy val velocity: Int = ship.velocity
}

case class Fleet(
    squads: List[Squad],
    position: Position,
    destinationPlanet: Option[Planet]
) {
  lazy val attack: Int = squads.foldLeft(0)(_ + _.attack)
  lazy val defense: Int = squads.foldLeft(0)(_ + _.defense)
  lazy val velocity: Int =
    squads.foldRight(Int.MaxValue)((squad: Squad, minVelocity: Int) =>
      if (squad.velocity < minVelocity) squad.velocity
      else minVelocity
    )

  def update: Fleet = {
    System.out.println(position)
    destinationPlanet match {
      case Some(p) if position == p.position =>
        Fleet(squads, position, None)
      case Some(p) =>
        val direction = position.directionTo(p.position)
        val newPosition = position.add(
          Position(direction.x * velocity, direction.y * velocity)
        )
        System.out.println(p.name + " position:")
        System.out.println(p.position)
        System.out.println("Fleet position, direction, velocity, position:")
        System.out.println(position)
        System.out.println(direction)
        System.out.println(velocity)
        System.out.println(newPosition)
        System.out.println("")
        Fleet(squads, newPosition, destinationPlanet)
      case None => this
    }
  }

  def setDestinationPlanet(newDestinationPlanet: Planet): Fleet = {
    this.copy(destinationPlanet = Some(newDestinationPlanet))
  }
}
