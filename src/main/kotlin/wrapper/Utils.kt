package wrapper

import halite.Log
import halite.Planet
import halite.Position

fun Match.closestPlanets(position: Position): List<Planet> =
        map.allPlanets.values.sortedBy { position.getDistanceTo(it) }

fun log(obj: Any?) {
    Log.log(obj?.toString() ?: "null")
}
