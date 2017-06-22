package com.hydra.server.galaxy

object GalaxyConfigProvider {
  lazy val galaxiesConfig = List(
    GalaxyConfig(
      "Andromeda",
      2560,
      2560,
      864000,
      30,
      250
    ),
    GalaxyConfig(
      "Large Magelanic Cloud",
      320,
      320,
      864000,
      30,
      1
    )
  )
}
