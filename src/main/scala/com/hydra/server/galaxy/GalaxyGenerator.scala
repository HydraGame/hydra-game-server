package com.hydra.server.galaxy

import com.hydra.server.planet.{Planet, PlanetWithPlayer, Position}
import com.hydra.server.player.Player

import scala.collection.immutable.{::, Nil}

case class GalaxyConfig(
  name: String,
  width: Int,
  height: Int,
  duration: Int,
  speed: Int,
  countPlanets: Int
)

object GalaxyGenerator {
  def generate(galaxyConfig: GalaxyConfig, planetNames: List[String]): Galaxy = {
    def planetFromNamePosition(namePos: (String, Position)): Planet = Planet(namePos._1, namePos._2)

    val planets: List[Planet] =
      planetNames take galaxyConfig.countPlanets zip PositionGenerator.generate(galaxyConfig) map planetFromNamePosition

    Galaxy(galaxyConfig.name, planets)
  }
}

object GalaxyPlayersPairing {
  def pair(galaxy: Galaxy, players: List[Player]): GalaxyWithPlayers = {

    def combine(planets: List[Planet], players: List[Player]): List[PlanetWithPlayer] = (planets, players) match {
      case (x::xs, y::ys) => PlanetWithPlayer(x, Some(y))::combine(xs, ys)
      case (x::xs, Nil) => PlanetWithPlayer(x, None)::combine(xs, Nil)
      case _ => Nil
    }

    def planetsWithPlayers = combine(galaxy.planets, players)

    GalaxyWithPlayers(galaxy.name, planetsWithPlayers, galaxy.timer, players, None)
  }
}

object PositionGenerator {
  def generate(galaxyConfig: GalaxyConfig): List[Position] = for {
    x <- (48 to(galaxyConfig.width - 48, 128)).toList
    y <- (48 to(galaxyConfig.height - 48, 128)).toList
  } yield Position(x, y)
}