package org.tabletennis.project.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.tabletennis.project.network.WebSocketManager

@Composable
fun PlayerSelectionScreen(
    webSocketManager: WebSocketManager,
    onPlayerSelected: (Int) -> Unit
) {
    var isConnecting by remember { mutableStateOf(false) }
    var selectedPlayerLabel by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        webSocketManager.disconnect()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF222222)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Tischtennis Online",
            fontSize = 36.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Wähle deinen Spieler",
            fontSize = 24.sp,
            color = Color.White,
            fontWeight = FontWeight.Normal
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                isConnecting = true
                selectedPlayerLabel = "Spieler 1"
                webSocketManager.connect("host1")
                onPlayerSelected(1)
            },
            enabled = !isConnecting,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF44336),
                disabledContainerColor = Color(0x77F44336)
            ),
            modifier = Modifier
                .width(220.dp)
                .height(60.dp)
        ) {
            Text(
                if (isConnecting && selectedPlayerLabel == "Spieler 1") "Verbinde..." else "Spieler 1 (Unten)",
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                isConnecting = true
                selectedPlayerLabel = "Spieler 2"
                webSocketManager.connect("host2")
                onPlayerSelected(2)
            },
            enabled = !isConnecting,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50),
                disabledContainerColor = Color(0x774CAF50)
            ),
            modifier = Modifier
                .width(220.dp)
                .height(60.dp)
        ) {
            Text(
                if (isConnecting && selectedPlayerLabel == "Spieler 2") "Verbinde..." else "Spieler 2 (Oben)",
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            "Hinweis: Das Spiel startet automatisch, sobald beide Spieler verbunden sind.",
            fontSize = 14.sp,
            color = Color.Yellow.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}