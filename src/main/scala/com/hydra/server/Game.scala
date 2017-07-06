package com.hydra.server

import java.time.LocalDateTime

import com.hydra.server.fleet._
import com.hydra.server.galaxy.GalaxyJsonProtocol._
import com.hydra.server.galaxy._
import com.hydra.server.planet.PlanetNamesProvider
import com.hydra.server.player.{Player, PlayerProvider, PlayerWithFleet}
import com.redis.RedisClient
import spray.json._

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
        Squad(BattleCruiser, 2),
        Squad(ColonyShip, 1)
      )
    )
    val players: List[Player] = PlayerProvider.players
    val galaxiesWithPlayers: List[GalaxyWithPlayers] = galaxies.map { g => GalaxyPlayersPairing.pair(g, players) }

    val playersWithFleet = players.map { PlayerWithFleet(_, startingFleet) }
    val planetsWithFleet = galaxiesWithPlayers.map { _.planetsWithPlayers }

    val x: List[GalaxyWithPlayers] = galaxiesWithPlayers.map { galaxy => GalaxyWithPlayers(galaxy.name, galaxy.planetsWithPlayers.map { _.withFleet(startingFleet) }, galaxy.timer, galaxy.players, galaxy.winner) }

    println(x)

    def loop(galaxies: List[GalaxyWithPlayers], iterations: Int): List[GalaxyWithPlayers] = {
      System.out.println(s"${LocalDateTime.now()}")

      val galaxiesString = galaxies.head.toJson.compactPrint
      redis.set("galaxy-simple-json-Andromeda", galaxiesString)

      // 60 updates per second
      Thread.sleep(1000 / 1)

      if (iterations == 0) galaxies
      else loop(galaxies.map(_.update), iterations - 1)
    }

    // open socket connection to client(s)
    // progress galaxies
    // send galaxies JSON via socket
    loop(galaxiesWithPlayers, 1000)
  }

  private def toJsonString(galaxy: Galaxy): String = galaxy.toJson.compactPrint
}
