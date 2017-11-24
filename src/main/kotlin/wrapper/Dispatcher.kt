package wrapper

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
    
    fun applyDirective(directive: Directive, ships: Int = 1, target: Position? = null) {
        val allocated = mutableListOf<Ship>()
        
        val toCheck = mutableListOf<Ship>()

        toCheck.addAll(unallocatedShips.map { this@Dispatcher[it]!! })
        
        if (target != null)
            toCheck.sortBy { it.getDistanceTo(target) }
        
        for (i in 0 until ships) {
            if (toCheck.size > i)
                allocated.add(toCheck[i])
        }
        
        if (allocated.size < ships) {
            toCheck.clear()

            toCheck.addAll(allocatedShips.filter { it.second.priority < directive.priority }.map { this@Dispatcher[it.first]!! })

            if (target != null)
                toCheck.sortBy { it.getDistanceTo(target) }

            for (i in 0 until ships) {
                if (toCheck.size > i)
                    allocated.add(toCheck[i])
            }
        }
        
        allocated.forEach { 
            allocate(it, directive)
        }
    }
}
