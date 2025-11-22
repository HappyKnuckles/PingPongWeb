package org.tabletennis.project.network

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Simuliert einen WebSocket-Manager ohne echte Verbindung.
 * Diese Klasse wird später mit einer echten WebSocket-Implementierung ersetzt.
 */
class WebSocketManager {
    private val messageChannel = Channel<String>()
    val messages: Flow<String> = messageChannel.receiveAsFlow()
    
    private var connected = false
    private var playerNumber = 0
    
    // Status, ob beide Spieler verbunden sind
    private val _bothPlayersConnected = MutableStateFlow(false)
    val bothPlayersConnected: StateFlow<Boolean> = _bothPlayersConnected
    
    // Simulierte Server-Spieleranzahl (in einer echten Implementierung wäre das serverseitig)
    private var connectedPlayersCount = 0
    
    // Simulierte Verbindung mit automatischer Spielernummerzuweisung
    suspend fun connect() {
        if (!connected) {
            // Spieler zählen und Nummer zuweisen (1 für ersten Spieler, 2 für zweiten)
            connectedPlayersCount++
            
            // Simuliere, dass maximal 2 Spieler erlaubt sind
            if (connectedPlayersCount > 2) {
                messageChannel.send("ERROR:SERVER_FULL")
                return
            }
            
            playerNumber = connectedPlayersCount
            connected = true
            
            // Sende dem Spieler seine zugewiesene Nummer
            delay(500)
            messageChannel.send("PLAYER_ASSIGNED:$playerNumber")
            
            // Wenn beide Spieler verbunden sind, informiere beide
            if (connectedPlayersCount == 2) {
                _bothPlayersConnected.value = true
                delay(1000)
                messageChannel.send("BOTH_PLAYERS_CONNECTED")
                
                // Automatisch das Spiel starten
                delay(2000)
                simulateGameStart()
            } else {
                messageChannel.send("WAITING_FOR_OPPONENT")
            }
        }
    }
    
    suspend fun send(message: String) {
        if (!connected) return
        
        // Simuliere Nachrichtenverarbeitung
        when {
            message == "LEAVE_GAME" -> {
                connected = false
                // Bei einem echten Server würde hier die Spieleranzahl reduziert
                connectedPlayersCount--
                if (connectedPlayersCount < 2) {
                    _bothPlayersConnected.value = false
                }
            }
            message.startsWith("PADDLE_MOVE:") -> {
                // Paddleposition an alle Clients senden (in einer echten Implementierung)
                val parts = message.split(":")
                if (parts.size >= 3) {
                    val paddle = parts[1].toInt()
                    val position = parts[2].toFloat()
                    messageChannel.send("PADDLE_UPDATE:$paddle:$position")
                }
            }
        }
    }
    
    suspend fun updatePaddlePosition(position: Float) {
        if (connected) {
            send("PADDLE_MOVE:$playerNumber:$position")
        }
    }
    
    fun disconnect() {
        if (connected) {
            connected = false
            connectedPlayersCount--
            if (connectedPlayersCount < 2) {
                _bothPlayersConnected.value = false
            }
        }
    }
    
    // Hilfsmethode für die Simulation eines Spielstarts
    suspend fun simulateGameStart() {
        if (connected && connectedPlayersCount == 2) {
            // Spiel für beide Spieler starten
            messageChannel.send("GAME_START")
            
            // Initialposition und Geschwindigkeit des Balls senden
            delay(500)
            val initialBallData = "BALL_INIT:0:30:0:20:0:40"
            messageChannel.send(initialBallData)
            
            // Ball-Trajektorie senden
            sendBallTrajectory(0f, 0f, 500f, 1000f, 20f, 40f, 3f)
        }
    }
    
    // Methode zum Senden von Ball-Trajektorien
    suspend fun sendBallTrajectory(
        startX: Float, startZ: Float, 
        endX: Float, endZ: Float, 
        velocityX: Float, velocityZ: Float, 
        duration: Float
    ) {
        if (connected) {
            val msg = "BALL_TRAJECTORY:$startX:$startZ:$endX:$endZ:$velocityX:$velocityZ:$duration"
            messageChannel.send(msg)
        }
    }
    
    // Methode zum Senden von Punktupdates
    suspend fun sendScoreUpdate(score1: Int, score2: Int) {
        if (connected) {
            messageChannel.send("SCORE_UPDATE:$score1:$score2")
        }
    }
    
    // Hilfsmethode, um den aktuellen Spieler zu bekommen
    fun getPlayerNumber(): Int {
        return playerNumber
    }
}
