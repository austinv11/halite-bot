package wrapper

import halite.GameMap
import halite.Move
import halite.Networking
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import kotlinx.coroutines.experimental.withTimeout

fun startMatch(name: String, 
               startCallback: Match.() -> Unit,
               gameLoopCallback: Match.(data: GameMap) -> Unit): Match
                    = Match(Networking(), name, startCallback, gameLoopCallback)

private const val MAX_INIT_TIME = 60000L //Time for the bot to init
private const val MAX_TURN_TIME = 2000L //Time for a turn

data class Match(val network: Networking, 
            val name: String,
            val startCallback: Match.() -> Unit,
            val gameLoopCallback: Match.(data: GameMap) -> Unit) {
    
    
    val map: GameMap
    val moveQueue: MutableList<Move> = mutableListOf()
    
    init {
        runBlocking {
            try {
                withTimeout(MAX_INIT_TIME) {
                    startCallback(this@Match)
                }
            } catch (e: Exception) {}
        }
        
        map = network.initialize(name)
        
        launch { 
            while (true) {
                withTimeout(MAX_TURN_TIME) {
                    map.updateMap(Networking.readLineIntoMetadata())

                    gameLoopCallback(this@Match, map)
                    
                    Networking.sendMoves(moveQueue)
                    moveQueue.clear()
                }
            }
        }
    }
}
