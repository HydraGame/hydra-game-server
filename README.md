# hydra-game-server

Player
-> id
-> name
-> fleets: List[FleetId]

Fleet
-> playerId
-> squads: List[SquadId]
-> planetId: Option[planetId]

Planet
-> position
-> option[player]
-> option[fleet]