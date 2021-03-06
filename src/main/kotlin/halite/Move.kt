package halite

enum class MoveType { Noop, Thrust, Dock, Undock }

open class Move(val type: MoveType, val ship: Ship) {
    override fun toString(): String = "Move(type=$type, ship=${ship.id})"
}

class ThrustMove(ship: Ship, @Volatile var angle: Int, @Volatile var thrust: Int) : Move(MoveType.Thrust, ship)
class UndockMove(ship: Ship) : Move(MoveType.Undock, ship)
class DockMove(ship: Ship, planet: Planet) : Move(MoveType.Dock, ship) {
    val destinationId: Long = planet.id.toLong()
}
