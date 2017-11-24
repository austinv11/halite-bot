package wrapper

import halite.Ship

interface Directive {
    
    val priority: Priority
    
    fun fire(ship: Ship, match: Match)
}

enum class Priority {
    LOWEST, LOW, MEDIUM, HIGH, HIGHEST
}
