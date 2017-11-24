package wrapper

import halite.GameMap
import halite.Move
import halite.Networking
import kotlinx.coroutines.experimental.Unconfined
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
    val dispatcher: Dispatcher
    @Volatile var turnCount: Int = -1
    
    init {
        runBlocking {
            try {
                withTimeout(MAX_INIT_TIME) {
                    startCallback(this@Match)
                }
            } catch (e: Exception) {}
        }
        
        turnCount++
        map = network.initialize(name)
        dispatcher = Dispatcher(this)
        launch(Unconfined) { 
            while (true) {
                try {
                    withTimeout(MAX_TURN_TIME) {
                        map.updateMap(Networking.readLineIntoMetadata())
                        dispatcher.update()

                        try {
                            gameLoopCallback(this@Match, map)
                        } catch (e: Exception) {}
                        
                        dispatcher.dispatch()

                        Networking.sendMoves(moveQueue)
                        moveQueue.clear()
                    }
                } catch (e: Exception) {}
                turnCount++
            }
        }.start()
    }
}
