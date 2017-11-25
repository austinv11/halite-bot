import halite.*
import wrapper.*


const val DEFENSE_DISTANCE = Constants.DOCK_RADIUS * 3
const val DEFAULT_ANGULAR_STEP = Math.PI / 180.0

fun main(args: Array<String>) {
    log("Starting...")
    startMatch("Mastermind", {
        
    }, {
        if (turnCount == 0) { //First turn
            log("First turn")
            val directedPlanets = mutableListOf<Int>()
            map.myPlayer.ships.values.forEach { 
                val planet = closestPlanets(it).find { !directedPlanets.contains(it.id) }!!
                dispatcher.applyDirective(dispatcher.claimNeutralPlanet(planet), target = planet)
                directedPlanets.add(planet.id)
            }
        } else {
            log("Turn $turnCount")
            
            dispatcher.unallocatedShips.forEach { //Claim neutral planets ASAP
                val planet = closestPlanets(dispatcher[it]!!).first { !it.isOwned }
                dispatcher.applyDirective(dispatcher.claimNeutralPlanet(planet), target = planet, ships = dispatcher[it]!!)
            }
            
            if (map.myPlayer.ships.isNotEmpty()) { //Defend planets from attack
                val dangerousShips = map.allShips
                        .filter { !map.myPlayer.ships.containsKey(it.id) }
                        .map { Pair(it, closestPlanets(it).first()) }
                        .filter { it.first.getDistanceTo(it.second) <= DEFENSE_DISTANCE }
                
                val priority = Priority.values()[((1 - mapControlPercentage()) * (Priority.values().size - 1)).toInt()]
                
                dangerousShips.forEach { 
                    dispatcher.applyDirective(dispatcher.attackShip(priority, it.first), target = it.second)
                }
            }
            
            dispatcher.applyDirective(dispatcher.reinforcePlanet(), shipCount = dispatcher.unallocatedShips.size)
            
            dispatcher.applyDirective(dispatcher.centerShips(), shipCount = dispatcher.unallocatedShips.size)
            
            dispatcher.applyDirective(dispatcher.generateDirective(Priority.LOWEST, { match -> //Starter kit logic
                val planet = map.allPlanets.values.firstOrNull { !it.isOwned }
                if (planet != null) {
                    if (this.canDock(planet)) {
                        dispatcher.queue(DockMove(this, planet))
                    } else {
                        val newThrustMove = Navigation(this, planet).navigateToDock(map, Constants.MAX_SPEED)

                        if (newThrustMove != null) {
                            dispatcher.queue(newThrustMove)
                        }
                    }
                }
            }), dispatcher.unallocatedShips.size)
        }
    })
}

fun Match.mapControlPercentage(): Double
        = this.map.allPlanets.
        filter { it.value.isOwned && it.value.owner == map.myPlayerId }.size.toDouble() /
            this.map.allPlanets.size.toDouble()

fun Match.centerOfControl(): Position { //TODO: Investigate K means clustering, currently estimates zone of control as rectangle
    var minX: Double = Double.MAX_VALUE
    var maxX: Double = Double.MIN_VALUE
    var minY: Double = Double.MAX_VALUE
    var maxY: Double = Double.MIN_VALUE
    
    map.allPlanets.filter { !it.value.isOwned || it.value.owner != map.myPlayerId }.map { it.value }.forEach { 
        minX = Math.min(it.xPos, minX)
        maxX = Math.max(it.xPos, maxX)
        minY = Math.min(it.yPos, minY)
        maxY = Math.max(it.yPos, maxY)
    }
    
    return Position((minX + maxX) / 2, (minY + maxY) / 2)
}

fun Dispatcher.claimNeutralPlanet(planet: Planet): Directive {
    return generateDirective(Priority.HIGHEST) { match -> 
        if (this.canDock(planet))
            queue(DockMove(this, planet))
        else if (this.dockingStatus == DockingStatus.Docked || planet.isOwned) {
            //pass
        } else {
            val newThrustMove = Navigation(this, planet).navigateToDock(match.map, Constants.MAX_SPEED)
            
            if (newThrustMove != null)
                queue(newThrustMove)
        }
    }
}

fun Dispatcher.attackShip(priority: Priority, ship: Ship): Directive {
    return generateDirective(priority) {
        if (match.map.allShips.firstOrNull { it.id == ship.id } == null) {
            this.deallocate()
        } else if (this.dockingStatus == DockingStatus.Docked || this.dockingStatus == DockingStatus.Undocking) {
            queue(UndockMove(this))
        } else if (this.dockingStatus == DockingStatus.Undocked) {
            val move = Navigation(this, ship).navigateTowards(match.map, ship.getClosestPoint(this), Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, DEFAULT_ANGULAR_STEP)
            
            if (this.weaponCooldown > 0 && move != null) { // Run away if we can't fire
                move.angle = 360 - move.angle
                move.thrust /= 2
            }
            
            queue(move)
        }
    }
}

fun Dispatcher.reinforcePlanet(): Directive {
    return generateDirective(Priority.LOWEST) {
        if (this.dockingStatus == DockingStatus.Docked)
            this.deallocate()
        
        val toReinforce = it.closestPlanets(this).firstOrNull { planet ->
            planet.isOwned && planet.owner == it.map.myPlayerId && !planet.isFull
        }
        
        if (toReinforce == null) {
            this.deallocate()
        } else {
            if (this.canDock(toReinforce) && this.dockingStatus != DockingStatus.Docked) {
                queue(DockMove(this, toReinforce))
            } else {
                queue(Navigation(this, toReinforce).navigateToDock(it.map, Constants.MAX_SPEED))
            }
        }
    }
}

fun Dispatcher.centerShips(): Directive {
    return generateDirective(Priority.LOWEST) {
        val center = it.centerOfControl()
        if (center != Position(this.xPos, this.yPos)) {
            queue(Navigation(this, null).navigateTowards(it.map, center, Constants.MAX_SPEED / 2, true, Constants.MAX_NAVIGATION_CORRECTIONS, DEFAULT_ANGULAR_STEP))
        } else {
            this.deallocate()
        }
    }
}
