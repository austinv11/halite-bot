package halite

object Constants {

    ////////////////////////////////////////////////////////////////////////
    // Implementation-independent language-agnostic constants

    /** Games will not have more players than this  */
    const val MAX_PLAYERS = 4

    /** Max number of units of distance a ship can travel in a turn  */
    const val MAX_SPEED = 7

    /** Radius of a ship  */
    const val SHIP_RADIUS = 0.5

    /** Starting health of ship, also its max  */
    const val MAX_SHIP_HEALTH = 255

    /** Starting health of ship, also its max  */
    const val BASE_SHIP_HEALTH = 255

    /** Weapon cooldown period  */
    const val WEAPON_COOLDOWN = 1

    /** Weapon damage radius  */
    const val WEAPON_RADIUS = 5.0

    /** Weapon damage  */
    const val WEAPON_DAMAGE = 64

    /** Radius in which explosions affect other entities  */
    const val EXPLOSION_RADIUS = 10.0

    /** Distance from the edge of the planet at which ships can try to dock  */
    const val DOCK_RADIUS = 4.0

    /** Number of turns it takes to dock a ship  */
    const val DOCK_TURNS = 5

    /** Number of production units per turn contributed by each docked ship  */
    const val BASE_PRODUCTIVITY = 6

    /** Distance from the planets edge at which new ships are created  */
    const val SPAWN_RADIUS = 2.0

    ////////////////////////////////////////////////////////////////////////
    // Implementation-specific constants

    const val FORECAST_FUDGE_FACTOR = SHIP_RADIUS + 0.1
    const val MAX_NAVIGATION_CORRECTIONS = 90

    /**
     * Used in Position.getClosestPoint()
     * Minimum distance specified from the object's outer radius.
     */
    const val MIN_DISTANCE = 3
}
