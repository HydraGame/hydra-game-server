package com.hydra.server

import com.hydra.server.command.AttackCommand
import com.hydra.server.galaxy.GalaxyJsonProtocol._
import com.hydra.server.galaxy._
import com.hydra.server.planet.{Planet, PlanetExpanded, PlanetNamesProvider, PlanetWithPlayer}
import com.hydra.server.player.{Player, PlayerProvider}
import com.hydra.server.ships._
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
    val galaxy: Galaxy = GalaxyGenerator.generate(GalaxyConfigProvider.galaxyConfig, planetNames)

    val startingFleet: Fleet = Fleet(
      List(
        Squad(BattleCruiser(), 2),
        Squad(ColonyShip(), 1)
      )
    )
    val players: List[Player] = PlayerProvider.players

    def planetsWithPlayers(planets: List[Planet], players: List[Player]): List[PlanetWithPlayer] = {
      def combine(planets: List[Planet], players: List[Player]): List[PlanetWithPlayer] = (planets, players) match {
        case (x :: xs, y :: ys) => PlanetWithPlayer(x, Some(y)) :: combine(xs, ys)
        case (x :: xs, Nil) => PlanetWithPlayer(x, None) :: combine(xs, Nil)
        case _ => Nil
      }

      combine(planets, players)
    }

    def planetsExpanded(planets: List[PlanetWithPlayer]): List[PlanetExpanded] =
      planets map { p =>
        p.player match {
          case Some(_) => PlanetExpanded(p.planet, p.player, Some(startingFleet))
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

      // 60 updates per second
      Thread.sleep(1000 / 3)

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

    val updatedGalaxy: Option[GalaxyExpanded] = oac.map {
      handleAttackCommand(_, galaxy)
    }

    updatedGalaxy.getOrElse(galaxy)
  }

  def handleAttackCommand(attackCommand: AttackCommand, galaxy: GalaxyExpanded): GalaxyExpanded = {
    val attackingPlanet: Option[PlanetExpanded] = galaxy.planets.find(_.planet.name == attackCommand.attackingPlanetName)
    val attackingPlayer: Option[Player] = attackingPlanet.flatMap {
      _.player
    }
    val attackingFleet: Option[Fleet] = attackingPlanet.flatMap {
      _.fleet
    }

    val updatedPlanets = galaxy.planets.map { pe =>
      pe.planet.name match {
        case attackCommand.attackingPlanetName =>
          PlanetExpanded(
            Planet(pe.planet.name, pe.planet.position, pe.planet.population + 100, pe.planet.gold + 50000),
            pe.player,
            None
          )
        case attackCommand.attackedPlanetName =>
          PlanetExpanded(Planet(pe.planet.name, pe.planet.position, 1), attackingPlayer, attackingFleet)
      }
    }

    val players: List[Player] = updatedPlanets.flatMap(p => p.player)
    val firstPlayer = players.headOption
    val winner: Option[Player] =
      if (players.forall(_ == players.head)) firstPlayer
      else None

    GalaxyExpanded(galaxy.name, updatedPlanets, galaxy.timer, galaxy.players, winner)
  }

  private def toJsonString(galaxy: GalaxyExpanded): String = galaxy.toJson.compactPrint
}
