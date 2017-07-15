package com.hydra.server.galaxy

import com.hydra.server.ships._
import com.hydra.server.planet.{Planet, PlanetExpanded, PlanetWithPlayer}
import com.hydra.server.player.Player
import spray.json._

object GalaxyJsonProtocol extends DefaultJsonProtocol {
  implicit val battleCruiserFormat: RootJsonFormat[BattleCruiser] = jsonFormat4(BattleCruiser)
  implicit val colonyShipFormat: RootJsonFormat[ColonyShip] = jsonFormat4(ColonyShip)
  implicit object ShipFormat extends RootJsonFormat[Ship] {
    def write(s: Ship): JsValue = s match {
      case b: BattleCruiser => b.toJson
      case c: ColonyShip => c.toJson
    }

    override def read(json: JsValue): Ship = BattleCruiser()
  }

  implicit val position: RootJsonFormat[Position] = jsonFormat2(Position)
  implicit val player: RootJsonFormat[Player] = jsonFormat2(Player)
  implicit val planet: RootJsonFormat[Planet] = jsonFormat(Planet, "name", "position", "population", "gold")
  implicit val planetWithPlayer: RootJsonFormat[PlanetWithPlayer] = jsonFormat(PlanetWithPlayer, "planet", "player")
  implicit val squad: RootJsonFormat[Squad] = jsonFormat(Squad, "ship", "count")
  implicit val fleet: RootJsonFormat[Fleet] = jsonFormat(Fleet, "squads")
  implicit val planetExpanded: RootJsonFormat[PlanetExpanded] = jsonFormat(PlanetExpanded, "planet", "player", "fleet")
  implicit val galaxyFormat: RootJsonFormat[Galaxy] = jsonFormat(Galaxy, "name", "planets", "timer")
  implicit val galaxyExpandedFormat: RootJsonFormat[GalaxyExpanded] = jsonFormat(GalaxyExpanded, "name", "planets", "timer", "players", "winner")
}