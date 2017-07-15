package com.hydra.server

import java.time.LocalDateTime

import com.hydra.server.galaxy.GalaxyJsonProtocol._
import com.hydra.server.galaxy._
import com.hydra.server.planet.{Planet, PlanetExpanded, PlanetNamesProvider, PlanetWithPlayer}
import com.hydra.server.player.{Player, PlayerProvider}
import com.hydra.server.ships._
import com.redis.RedisClient
import spray.json._

import scala.collection.immutable.Nil

object Game {

  def run(): Unit = {
    val redis = new RedisClient("localhost", 6379)

    // load planet names from file system
    val planetNames = PlanetNamesProvider.planetNames.getOrElse(
      throw new Exception("Cannot load planet names")
    )

    // load hard-coded galaxies config
    val galaxies: List[Galaxy] = GalaxyConfigProvider.galaxiesConfig.map {
      GalaxyGenerator.generate(_, planetNames)
    }

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

    def galaxyExpanded(g: Galaxy) = {
      val planets = planetsExpanded(
        planetsWithPlayers(g.planets, players)
      )

      GalaxyExpanded(g.name, planets, g.timer, players, None)
    }

    val galaxiesExpanded = galaxies map galaxyExpanded

    def loop(galaxies: List[GalaxyExpanded], iterations: Int): List[GalaxyExpanded] = {
      System.out.println(s"${LocalDateTime.now()}")

      val galaxiesString = galaxies.head.toJson.compactPrint

      redis.set("galaxy-simple-json-Andromeda", galaxiesString)
      println(redis.get("galaxy-simple-json-Andromeda"))

      // 60 updates per second
      Thread.sleep(1000 / 3)

      if (iterations == 0) galaxies
      else loop(galaxies.map(_.update), iterations - 1)
    }

    // open socket connection to client(s)
    // progress galaxies
    // send galaxies JSON via socket
    loop(galaxiesExpanded, 5000)
  }

  private def toJsonString(galaxy: GalaxyExpanded): String = galaxy.toJson.compactPrint
}
