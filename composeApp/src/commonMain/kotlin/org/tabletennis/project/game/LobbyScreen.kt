package org.tabletennis.project.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.tabletennis.project.network.WebSocketManager

@Composable
fun LobbyScreen(
    onGameStart: () -> Unit,
    webSocketManager: WebSocketManager,
    lobbyState: LobbyState
) {
    val scope = rememberCoroutineScope()
    val playerNumber = lobbyState.playerNumber.collectAsState()
    val isConnected = lobbyState.isConnected.collectAsState()
    val waitingForOpponent = lobbyState.waitingForOpponent.collectAsState()
    val gameStarting = lobbyState.gameStarting.collectAsState()
    
    // Status für den verbindenden Button
    var connecting by remember { mutableStateOf(false) }
    
    // WebSocket-Nachrichten abhören
    LaunchedEffect(Unit) {
        webSocketManager.messages.collect { message ->
            when {
                message == "GAME_START" -> {
                    onGameStart()
                }
                message.startsWith("PLAYER_ASSIGNED") -> {
                    val parts = message.split(":")
                    if (parts.size > 1) {
                        val number = parts[1].toIntOrNull() ?: 0
                        lobbyState.setPlayerNumber(number)
                        lobbyState.setConnected(true)
                    }
                    connecting = false
                }
                message == "WAITING_FOR_OPPONENT" -> {
                    lobbyState.setWaitingForOpponent(true)
                }
                message == "BOTH_PLAYERS_CONNECTED" -> {
                    lobbyState.setWaitingForOpponent(false)
                    lobbyState.setGameStarting(true)
                }
                message == "ERROR:SERVER_FULL" -> {
                    // Server ist voll, zurück zum Anfang
                    connecting = false
                    lobbyState.reset()
                }
            }
        }
    }
    
    // Beobachte auch den WebSocketManager Status für beide Spieler
    val bothConnected = webSocketManager.bothPlayersConnected.collectAsState()
    
    LaunchedEffect(bothConnected.value) {
        if (bothConnected.value) {
            lobbyState.setWaitingForOpponent(false)
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF222222)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .widthIn(max = 480.dp)
                .padding(16.dp)
        ) {
            Text(
                text = "Tischtennis Online",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            // Hauptkarte mit Status und Verbindungsbutton
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    if (isConnected.value) {
                        // Verbundener Zustand
                        Text(
                            "Du bist Spieler ${playerNumber.value}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(Modifier.height(16.dp))
                        
                        if (waitingForOpponent.value) {
                            // Warten auf den zweiten Spieler
                            CircularProgressIndicator(
                                color = Color(0xFF1565C0),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Warte auf Gegner...",
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                        } else if (gameStarting.value) {
                            // Beide Spieler sind verbunden, Spiel startet gleich
                            Text(
                                "Beide Spieler verbunden!",
                                fontSize = 16.sp,
                                color = Color.Green
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Spiel startet in Kürze...",
                                fontSize = 16.sp
                            )
                            Spacer(Modifier.height(16.dp))
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else {
                        // Nicht verbundener Zustand
                        Text(
                            "Tischtennis-Server",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(Modifier.height(8.dp))
                        
                        Text(
                            "Verbinde dich mit dem Spiel.\nDu wirst automatisch als Spieler 1 oder 2 zugewiesen.",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        
                        Spacer(Modifier.height(24.dp))
                        
                        Button(
                            onClick = {
                                connecting = true
                                scope.launch {
                                    webSocketManager.connect()
                                }
                            },
                            enabled = !connecting,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1565C0),
                                disabledContainerColor = Color(0xFF78909C)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (connecting) "Verbinde..." else "Mit Spiel verbinden"
                            )
                        }
                    }
                }
            }
            
            // Informationstext basierend auf Status
            if (isConnected.value) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF333333)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            when (playerNumber.value) {
                                1 -> "Du spielst unten (rot)"
                                2 -> "Du spielst oben (grün)"
                                else -> "Spieler-Zuweisung läuft..."
                            },
                            color = when (playerNumber.value) {
                                1 -> Color(0xFFF44336)
                                2 -> Color(0xFF4CAF50)
                                else -> Color.White
                            },
                            fontWeight = FontWeight.Medium
                        )
                        
                        if (!waitingForOpponent.value) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Bewege den Schläger mit der Maus/Touch",
                                color = Color.LightGray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
