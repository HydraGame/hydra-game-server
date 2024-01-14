package com.hydra.server

import com.hydra.server.command.AttackCommand
import com.hydra.server.galaxy.GalaxyJsonProtocol._
import com.hydra.server.galaxy._
import com.hydra.server.planet._
import com.hydra.server.ships._
import com.hydra.server.player.{Player, PlayerProvider}
import com.redis.RedisClient
import spray.json._

import scala.collection.immutable.Nil

object Game {
  lazy val redisClient = new RedisClient("localhost", 6379)

  def run(): Unit = {
    // load planet names from file system
    val planetNames = PlanetNamesProvider.planetNames.getOrElse(
      throw new Exception("Cannot load planet names")
    )

    // load hard-coded galaxy config
    val galaxy: Galaxy =
      GalaxyGenerator.generate(GalaxyConfigProvider.galaxyConfig, planetNames)

    val startingSquds: List[Squad] = List(
      Squad(BattleCruiser(), 2),
      Squad(ColonyShip(), 1)
    )

    val players: List[Player] = PlayerProvider.players

    def planetsWithPlayers(
        planets: List[Planet],
        players: List[Player]
    ): List[PlanetWithPlayer] = {
      def combine(
          planets: List[Planet],
          players: List[Player]
      ): List[PlanetWithPlayer] = (planets, players) match {
        case (x :: xs, y :: ys) =>
          PlanetWithPlayer(x, Some(y)) :: combine(xs, ys)
        case (x :: xs, Nil) => PlanetWithPlayer(x, None) :: combine(xs, Nil)
        case _              => Nil
      }

      combine(planets, players)
    }

    def planetsExpanded(planets: List[PlanetWithPlayer]): List[PlanetExpanded] =
      planets map { p =>
        p.player match {
          case Some(_) =>
            PlanetExpanded(
              p.planet,
              p.player,
              Some(
                Fleet(startingSquds, p.planet.position, None)
              )
            )
          case None => PlanetExpanded(p.planet, p.player, None)
        }
      }

    def galaxyExpanded(g: Galaxy): GalaxyExpanded = {
      val planets = planetsExpanded(
        planetsWithPlayers(g.planets, players)
      )

      GalaxyExpanded(g.name, planets, g.timer, players, None)
    }

    def loop(g: GalaxyExpanded, iterations: Int): GalaxyExpanded = {
      val updatedGalaxy = handleCommands(g)
      val galaxiesString: String = updatedGalaxy.toJson.compactPrint

      redisClient.set("galaxy-simple-json-Andromeda", galaxiesString)

      // 1 update every 5 seconds

      Thread.sleep(1000 / 2)

      if (iterations == 0) updatedGalaxy
      else loop(updatedGalaxy.update, iterations - 1)
    }

    // open socket connection to client(s)
    // progress galaxies
    // send galaxies JSON via socket
    loop(galaxyExpanded(galaxy), 5000)
  }

  def handleCommands(galaxy: GalaxyExpanded): GalaxyExpanded = {
    val oac: Option[AttackCommand] = redisClient.lpop("commands").map { rc =>
      rc.parseJson.convertTo[AttackCommand]
    }

    oac.foreach { println }

    val updatedGalaxy: Option[GalaxyExpanded] = oac.map {
      handleAttackCommand(_, galaxy)
    }

    updatedGalaxy.getOrElse(galaxy)
  }

  def handleAttackCommand(
      attackCommand: AttackCommand,
      galaxy: GalaxyExpanded
  ): GalaxyExpanded = {
    val attackingPlanet: Option[PlanetExpanded] =
      findPlanetByName(attackCommand.attackingPlanetName, galaxy)
    val defendingPlanet: Option[PlanetExpanded] =
      findPlanetByName(attackCommand.attackedPlanetName, galaxy)

    val updatedFleet = attackingPlanet.flatMap(_.fleet).flatMap { fleet =>
      defendingPlanet.map(_.planet).map(fleet.setDestinationPlanet)
    }

    val updatedPlanet = attackingPlanet.map(_.copy(fleet = updatedFleet))

    val updatedPlanets = galaxy.planets
      .collect {
        case p if p.planet.equals(attackingPlanet.get.planet) =>
          updatedPlanet.get
        case p => p
      }
      .asInstanceOf[List[PlanetExpanded]]

    galaxy.copy(planets = updatedPlanets)
  }

  def findPlanetByName(
      planetName: String,
      galaxy: GalaxyExpanded
  ): Option[PlanetExpanded] =
    galaxy.planets.find(_.planet.name == planetName)

  // def attack(
  //     attackingFleet: Option[Fleet],
  //     defendingFleet: Option[Fleet]
  // ): Boolean = {
  //   val rnd = scala.util.Random
  //   val attackCoefficient = 0.5 + rnd.nextDouble
  //   val defenseCoefficient = 0.5 + rnd.nextDouble

  //   val attackPower =
  //     attackingFleet.map(_.attack).getOrElse(0) * attackCoefficient
  //   val defensePower =
  //     defendingFleet.map(_.defense).getOrElse(0) * defenseCoefficient

  //   println(s"Attack power: $attackPower, Defense power: $defensePower")

  //   attackPower > defensePower
  // }

  private def toJsonString(galaxy: GalaxyExpanded): String =
    galaxy.toJson.compactPrint
}
