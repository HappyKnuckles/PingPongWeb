package org.tabletennis.project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.tabletennis.project.game.LobbyScreen
import org.tabletennis.project.game.LobbyState
import org.tabletennis.project.game.PingPongTable
import org.tabletennis.project.network.WebSocketManager

@Composable
@Preview
fun App() {
    MaterialTheme {
        val webSocketManager = remember { WebSocketManager() }
        val lobbyState = remember { LobbyState() }
        var currentScreen by remember { mutableStateOf("lobby") }
        
        // Automatisch zum Lobby-Screen zurückkehren, wenn die Verbindung verloren geht
        val isConnected = webSocketManager.bothPlayersConnected.collectAsState()
        
        LaunchedEffect(isConnected.value) {
            if (!isConnected.value && currentScreen == "game") {
                // Zurück zur Lobby, wenn während des Spiels die Verbindung verloren geht
                currentScreen = "lobby"
                lobbyState.reset()
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF222222))
        ) {
            when (currentScreen) {
                "lobby" -> LobbyScreen(
                    onGameStart = { currentScreen = "game" },
                    webSocketManager = webSocketManager,
                    lobbyState = lobbyState
                )
                "game" -> {
                    // Die PingPongTable-Klasse mit den WebSocket-Verbindungsdaten initialisieren
                    PingPongTable(
                        webSocketManager = webSocketManager,
                        playerNumber = lobbyState.playerNumber.collectAsState().value
                    ).PingPongTable()
                }
            }
        }
    }
}