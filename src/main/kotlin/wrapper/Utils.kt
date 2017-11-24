package wrapper

import halite.Log
import halite.Planet
import halite.Position
import halite.Ship

fun Match.closestPlanets(position: Position): List<Planet> =
        map.allPlanets.values.sortedBy { position.getDistanceTo(it) }

fun Match.closestFriendlyShips(position: Position): List<Ship> =
        map.myPlayer.ships.values.sortedBy { position.getDistanceTo(it) }

fun log(obj: Any?) {
    Log.log(obj?.toString() ?: "null")
}
