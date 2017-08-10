package com.hydra.server.galaxy

import com.hydra.server.planet.Planet

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
  def generate(galaxyConfig: GalaxyConfig): List[Position] = {
    val rnd = new scala.util.Random

    val x = (128 to(galaxyConfig.width - 256, 256)).toList
    val y = (128 to(galaxyConfig.width - 256, 256)).toList

    val z = x.flatMap { i => {
      y.map { j => Position(j + rnd.nextInt(128), i + rnd.nextInt(128)) }
    }
    }

    for {
      x <- (128 to(galaxyConfig.height - 256, 256)).toList
      y <- (128 to(galaxyConfig.width - 256, 256)).toList
    } yield Position(y + rnd.nextInt(128), x + rnd.nextInt(128))
  }
}