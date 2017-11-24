package wrapper

import halite.DockingStatus
import halite.Move
import halite.Position
import halite.Ship

class Dispatcher(val match: Match) { //Swarm mastermind, handles delegation of tasks
    
    val unallocatedShips = mutableListOf<Int>()
    
    val allocatedShips = mutableListOf<Pair<Int, Directive>>()
    
    fun update() {
        unallocatedShips.removeIf { !match.map.myPlayer.ships.containsKey(it) }

        allocatedShips.removeIf { !match.map.myPlayer.ships.containsKey(it.first) }
        
        match.map.myPlayer.ships.values.forEach { 
            if (!it.isAllocated() && !unallocatedShips.contains(it.id)) {
                unallocatedShips.add(it.id)
            }
        }
    }
    
    fun dispatch() {
        allocatedShips.map { Pair(this@Dispatcher[it.first]!!, it.second) }.forEach { it.second.fire(it.first, match) }
    }
    
    fun Ship.isAllocated() = allocatedShips.singleOrNull { it.first == this.id } != null
    
    fun Ship.deallocate() {
        if (isAllocated()) {
            allocatedShips.removeIf { it.first == this.id }
            unallocatedShips.add(this.id)
        }
    }
    
    operator fun get(id: Int): Ship? = match.map.myPlayer.ships.values.find { it.id == id }
    
    fun generateDirective(_priority: Priority, callback: Ship.(Match) -> Unit) = object: Directive {
        override val priority: Priority = _priority

        override fun fire(ship: Ship, match: Match) {
            callback(ship, match)
        }
    }
    
    fun allocate(ship: Ship, directive: Directive) {
        if (ship.isAllocated()) {
            allocatedShips.removeIf { it.first == ship.id }
        } else {
            unallocatedShips.removeIf { it == ship.id }
        }
        
        allocatedShips.add(Pair(ship.id, directive))
    }
    
    fun applyDirective(directive: Directive, shipCount: Int = 1, target: Position? = null, vararg ships: Ship) {
        val allocated = mutableListOf<Ship>()
        
        if (ships.isEmpty()) {
            val toCheck = mutableListOf<Ship>()

            toCheck.addAll(unallocatedShips.map { this@Dispatcher[it]!! })

            if (target != null)
                toCheck.sortBy { it.getDistanceTo(target) }

            for (i in 0 until shipCount) {
                if (toCheck.size > i)
                    allocated.add(toCheck[i])
            }

            if (allocated.size < shipCount) {
                toCheck.clear()

                toCheck.addAll(allocatedShips.filter { it.second.priority < directive.priority }.map { this@Dispatcher[it.first]!! })

                if (target != null)
                    toCheck.sortBy { it.getDistanceTo(target) }

                for (i in 0 until shipCount) {
                    if (toCheck.size > i)
                        allocated.add(toCheck[i])
                }
            }
        } else {
            allocated.addAll(ships)
        }
        
        allocated.forEach { 
            allocate(it, directive)
        }
    }
    
    fun queue(move: Move?) {
        if (move != null) {
            if (move.ship.dockingStatus != DockingStatus.Docking && move.ship.dockingStatus != DockingStatus.Undocking) {
                if (match.moveQueue.find { it.ship.id == move.ship.id } == null)
                    match.moveQueue.add(move)
                else
                    log("Attempted to queue move $move when ship ${move.ship.id} already had a move scheduled!")
            }
        }
    }
}
