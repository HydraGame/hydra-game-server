package com.hydra.server.galaxy

import com.hydra.server.planet.Planet

case class Galaxy(name: String, planets: List[Planet], timer: Int = 0) {
  private val TimerIncreaseRate = 1

  def update: Galaxy = Galaxy(name, planets.map(_.update), timer + TimerIncreaseRate)
}