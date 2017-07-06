package com.hydra.server.fleet

import com.hydra.server.galaxy.Position
import com.hydra.server.planet.Planet

sealed trait Ship {
  def name: String

  def attack: Int

  def defense: Int

  def velocity: Int
}

object BattleCruiser extends Ship {
  def name: String = "Battle Cruiser"

  def attack: Int = 100

  def defense: Int = 50

  def velocity: Int = 6
}

object ColonyShip extends Ship {
  def name: String = "Colony Ship"

  def attack: Int = 1

  def defense: Int = 10

  def velocity: Int = 5
}

case class Squad(ship: Ship, count: Int) {
  lazy val attack: Int = ship.attack * count
  lazy val defense: Int = ship.defense * count
  lazy val velocity: Int = ship.velocity
}

case class Fleet(squads: List[Squad]) {
  lazy val attack: Int = squads.foldLeft(0)(_ + _.attack)
  lazy val defense: Int = squads.foldLeft(0)(_ + _.defense)
  lazy val velocity: Int = squads.foldRight(Int.MaxValue)(
    (squad: Squad, minVelocity: Int) =>
      if (squad.velocity < minVelocity) squad.velocity
      else minVelocity
  )
}

case class FleetWithPosition(fleet: Fleet, position: Position, inTransit: Boolean, sourcePlanet: Option[Planet], targetPlanet: Option[Planet])

object Fleet {

}