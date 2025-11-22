package org.tabletennis.project.game


enum class GameState {
    PLAYER_SELECTION,
    WAITING,
    PLAYING
}

data class GameElements(
    val ballX: Float = 0f,
    val ballY: Float = 0f,
    val player1Y: Float = 0f,
    val player2Y: Float = 0f,
    val score1: Int = 0,
    val score2: Int = 0
)
