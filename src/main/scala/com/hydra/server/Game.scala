package com.hydra.server

import java.time.LocalDateTime

import com.hydra.server.galaxy._
import com.hydra.server.planet.PlanetNamesProvider
import com.redis.RedisClient
import spray.json._
import com.hydra.server.galaxy.GalaxyJsonProtocol._
import com.hydra.server.player.PlayerProvider

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

    val players = PlayerProvider.players
    val galaxiesWithPlyers: List[GalaxyWithPlayers] = galaxies.map { g =>  GalaxyPlayersPairing.pair(g, players) }

    def loop(galaxies: List[GalaxyWithPlayers], iterations: Int): List[GalaxyWithPlayers] = {
      System.out.println(s"${LocalDateTime.now()}")

      val galaxiesString = galaxies.head.toJson.compactPrint
      println(galaxiesString)
      redis.set("galaxy-simple-json-Andromeda", galaxiesString)

      // 60 updates per second
      Thread.sleep(1000 / 60)

      if (iterations == 0) galaxies
      else loop(galaxies.map(_.update), iterations - 1)
    }

    // open socket connection to client(s)
    // progress galaxies
    // send galaxies JSON via socket
    loop(galaxiesWithPlyers, 1000)
  }

  private def toJsonString(galaxy: Galaxy): String = galaxy.toJson.compactPrint
}
