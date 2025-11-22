package org.tabletennis.project.game

// Datenklasse für den Spielzustand
data class GameState(
    val ballX: Float = 0f,
    val ballY: Float = 0f,
    val player1Y: Float = 0f,
    val player2Y: Float = 0f,
    val score1: Int = 0,
    val score2: Int = 0
)
