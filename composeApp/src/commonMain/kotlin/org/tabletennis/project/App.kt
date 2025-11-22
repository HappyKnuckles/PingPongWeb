package org.tabletennis.project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.tabletennis.project.game.GameFlow
import org.tabletennis.project.game.PingPongTable

private const val DEVELOPMENT_MODE = true

@Composable
@Preview
fun App() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF222222))
        ) {
//            if (DEVELOPMENT_MODE) {
//                PingPongTable()
//            } else {
                GameFlow()
//            }
        }
    }
}
