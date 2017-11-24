import halite.*
import wrapper.*


fun main(args: Array<String>) {
    log("Starting...")
    startMatch("Mastermind", {
        
    }, {
        if (turnCount == 0) { //First turn
            log("First turn")
            val directedPlanets = mutableListOf<Int>()
            map.myPlayer.ships.values.forEach { 
                val planet = closestPlanets(it).find { !directedPlanets.contains(it.id) }!!
                dispatcher.applyDirective(dispatcher.opener(planet), target = planet)
                directedPlanets.add(planet.id)
            }
        } else {
            log("Turn $turnCount")
            dispatcher.applyDirective(dispatcher.generateDirective(Priority.LOWEST, { match ->
                val planet = map.allPlanets.values.firstOrNull { !it.isOwned }
                if (planet != null) {
                    if (this.canDock(planet)) {
                        moveQueue.add(DockMove(this, planet))
                    } else {
                        val newThrustMove = Navigation(this, planet).navigateToDock(map, Constants.MAX_SPEED)

                        if (newThrustMove != null) {
                            moveQueue.add(newThrustMove)
                        }
                    }
                }
            }), dispatcher.unallocatedShips.size)
        }
    })
}

fun Dispatcher.opener(planet: Planet): Directive {
    return generateDirective(Priority.HIGH) { match -> 
        if (this.canDock(planet))
            match.moveQueue.add(DockMove(this, planet))
        else if (this.dockingStatus == DockingStatus.Docked) {
            this.deallocate()
        } else {
            val newThrustMove = Navigation(this, planet).navigateToDock(match.map, Constants.MAX_SPEED)
            
            if (newThrustMove != null)
                match.moveQueue.add(newThrustMove)
        }
    }
}
