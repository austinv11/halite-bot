import halite.Constants
import halite.DockMove
import halite.DockingStatus
import halite.Navigation
import wrapper.startMatch


fun main(args: Array<String>) {
    startMatch("Goblin", {}, {
        map.myPlayer.ships.values
                .filter { it.dockingStatus == DockingStatus.Undocked }
                .forEach { ship ->
                    val planet = map.allPlanets.values.firstOrNull { !it.isOwned }
                    if (planet != null) {
                        if (ship.canDock(planet)) {
                            moveQueue.add(DockMove(ship, planet))
                        } else {
                            val newThrustMove = Navigation(ship, planet).navigateToDock(map, Constants.MAX_SPEED / 2)

                            if (newThrustMove != null) {
                                moveQueue.add(newThrustMove)
                            }
                        }
                    }
                }
    })
}

