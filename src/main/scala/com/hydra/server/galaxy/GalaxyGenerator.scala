package com.hydra.server.galaxy

import com.hydra.server.planet.{Planet, Position}

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

object PositionGenerator {
  def generate(galaxyConfig: GalaxyConfig): List[Position] = for {
    x <- (48 to(galaxyConfig.width - 48, 128)).toList
    y <- (48 to(galaxyConfig.height - 48, 128)).toList
  } yield Position(x, y)
}