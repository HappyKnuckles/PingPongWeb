package org.tabletennis.project.game

import androidx.compose.runtime.*
import org.tabletennis.project.network.WebSocketManager

@Composable
fun GameFlow() {
    var gameState by remember { mutableStateOf(GameState.PLAYER_SELECTION) }

    var playerNumber by remember { mutableStateOf(0) }

    val webSocketManager = remember { WebSocketManager() }

    val bothPlayersConnected by webSocketManager.bothPlayersConnected.collectAsState()

    LaunchedEffect(bothPlayersConnected) {
        if (bothPlayersConnected) {
            gameState = GameState.PLAYING
        } else {
            if (gameState == GameState.PLAYING) {
                gameState = GameState.PLAYER_SELECTION
                playerNumber = 0
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webSocketManager.disconnect()
        }
    }

    when (gameState) {
        GameState.PLAYER_SELECTION -> {
            PlayerSelectionScreen(
                webSocketManager = webSocketManager,
                onPlayerSelected = { number ->
                    playerNumber = number
                    gameState = GameState.WAITING
                }
            )
        }

        GameState.WAITING -> {
            WaitingScreen(playerNumber = playerNumber)
        }

        GameState.PLAYING -> {
            if (playerNumber > 0) {
                // Direct Composable call (updated from previous step)
                PingPongTable(
                    webSocketManager = webSocketManager,
                    playerNumber = playerNumber
                )
            }
        }
    }
}