package com.hydra.server.planet

import scala.io.Source
import scala.util.Try

object PlanetNamesProvider {
  lazy val planetNames: Try[List[String]] = Try {
    Source.fromFile("src/main/resources/com.hydra.server/planet-names.txt").getLines.toList
  }
}
