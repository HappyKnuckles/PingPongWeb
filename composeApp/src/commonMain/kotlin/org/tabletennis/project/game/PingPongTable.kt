package org.tabletennis.project.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.tabletennis.project.network.WebSocketManager

@Composable
fun PingPongTable(
    webSocketManager: WebSocketManager,
    playerNumber: Int
) {
    var score1 by remember { mutableStateOf(0) }
    var score2 by remember { mutableStateOf(0) }

    var paddleY by remember { mutableStateOf(0f) }
    var opponentPaddleY by remember { mutableStateOf(0f) }

    var ballX by remember { mutableStateOf(0f) }
    var ballY by remember { mutableStateOf(0f) }
    var ballVelocity by remember { mutableStateOf(0f) }

    val collisionEvent by webSocketManager.collisionEvent.collectAsState()

    LaunchedEffect(collisionEvent) {
        collisionEvent?.let { event ->
            val (mappedX, mappedZ) = GameCoordinates.mapGameToTable(event.x, event.y)
            ballX = mappedX
            ballY = mappedZ
            ballVelocity = event.v
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF222222))
            .pointerInput(Unit) {
                detectDragGestures { _, dragAmount ->
                    val dragScale = GameCoordinates.TableDims.WIDTH / size.width
                    val scaledDragAmount = dragAmount.y * dragScale

                    val newY = paddleY + scaledDragAmount

                    val (leftBoundary, _) = GameCoordinates.mapGameToTable(GameCoordinates.TableDims.GAME_LEFT + 20f, 0f)
                    val (rightBoundary, _) = GameCoordinates.mapGameToTable(GameCoordinates.TableDims.GAME_RIGHT - 20f, 0f)

                    paddleY = newY.coerceIn(leftBoundary, rightBoundary)
                }
            },
        contentAlignment = Alignment.TopCenter
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "$score1 : $score2",
                color = Color.White,
                fontSize = 32.sp
            )
            Text(
                text = "You are Player $playerNumber",
                color = Color.Gray,
                fontSize = 16.sp
            )
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val tableColor = Color(0xFF1565C0)
            val centerLineColor = Color.White.copy(alpha = 0.8f)
            val netPostColor = Color.Gray
            val netMeshColor = Color.White.copy(alpha = 0.3f)
            val netTapeColor = Color.White

            val halfW = GameCoordinates.TableDims.WIDTH / 2
            val halfL = GameCoordinates.TableDims.LENGTH / 2
            val netHeight = GameCoordinates.TableDims.NET_HEIGHT
            val netOverhang = 20f

            fun adjustZ(z: Float): Float = if (playerNumber == 2) -z else z

            val p1 = GameCoordinates.project3DToScreen(-halfW, 0f, adjustZ(-halfL), w, h)
            val p2 = GameCoordinates.project3DToScreen(halfW, 0f, adjustZ(-halfL), w, h)
            val p3 = GameCoordinates.project3DToScreen(halfW, 0f, adjustZ(halfL), w, h)
            val p4 = GameCoordinates.project3DToScreen(-halfW, 0f, adjustZ(halfL), w, h)

            val pTop1 = GameCoordinates.project3DToScreen(-halfW, 0f, adjustZ(-halfL), w, h)
            val pTop2 = GameCoordinates.project3DToScreen(halfW, 0f, adjustZ(-halfL), w, h)
            val pTop3 = GameCoordinates.project3DToScreen(halfW, 0f, adjustZ(-halfL), w, h)
            val pTop4 = GameCoordinates.project3DToScreen(-halfW, 0f, adjustZ(-halfL), w, h)

            val pBottom1 = GameCoordinates.project3DToScreen(-halfW, 0f, adjustZ(halfL), w, h)
            val pBottom2 = GameCoordinates.project3DToScreen(halfW, 0f, adjustZ(halfL), w, h)
            val pBottom3 = GameCoordinates.project3DToScreen(halfW, 0f, adjustZ(halfL), w, h)
            val pBottom4 = GameCoordinates.project3DToScreen(-halfW, 0f, adjustZ(halfL), w, h)

            val tablePath = Path().apply {
                moveTo(p1.x, p1.y)
                lineTo(p2.x, p2.y)
                lineTo(p3.x, p3.y)
                lineTo(p4.x, p4.y)
                close()
            }
            drawPath(tablePath, tableColor)
            drawPath(tablePath, Color.White, style = Stroke(width = 2f))

            val topBufferPath = Path().apply {
                moveTo(pTop1.x, pTop1.y)
                lineTo(pTop2.x, pTop2.y)
                lineTo(pTop3.x, pTop3.y)
                lineTo(pTop4.x, pTop4.y)
                close()
            }
            drawPath(topBufferPath, tableColor.copy(alpha = 0.7f))
            drawPath(topBufferPath, Color.White.copy(alpha = 0.5f), style = Stroke(width = 1f))
            val bottomBufferPath = Path().apply {
                moveTo(pBottom1.x, pBottom1.y)
                lineTo(pBottom2.x, pBottom2.y)
                lineTo(pBottom3.x, pBottom3.y)
                lineTo(pBottom4.x, pBottom4.y)
                close()
            }
            drawPath(bottomBufferPath, tableColor.copy(alpha = 0.7f))
            drawPath(bottomBufferPath, Color.White.copy(alpha = 0.5f), style = Stroke(width = 1f))

            val centerLineStart = GameCoordinates.project3DToScreen(0f, 0f, adjustZ(-halfL), w, h)
            val centerLineEnd = GameCoordinates.project3DToScreen(0f, 0f, adjustZ(halfL), w, h)

            val centerLineMainStart = GameCoordinates.project3DToScreen(0f, 0f, adjustZ(-halfL), w, h)
            val centerLineMainEnd = GameCoordinates.project3DToScreen(0f, 0f, adjustZ(halfL), w, h)

            drawLine(
                color = centerLineColor,
                start = centerLineMainStart,
                end = centerLineMainEnd,
                strokeWidth = 2f
            )

            drawLine(
                color = centerLineColor.copy(alpha = 0.5f),
                start = centerLineStart,
                end = centerLineMainStart,
                strokeWidth = 1.5f
            )

            drawLine(
                color = centerLineColor.copy(alpha = 0.5f),
                start = centerLineMainEnd,
                end = centerLineEnd,
                strokeWidth = 1.5f
            )

            val netZ = 0f
            val netLeftX = -halfW - netOverhang
            val netRightX = halfW + netOverhang

            val verticalStruts = 20
            val horizontalStruts = 5

            for (i in 1 until verticalStruts) {
                val fraction = i.toFloat() / verticalStruts
                val xPos = netLeftX + (netRightX - netLeftX) * fraction

                val top = GameCoordinates.project3DToScreen(xPos, netHeight, adjustZ(netZ), w, h)
                val bot = GameCoordinates.project3DToScreen(xPos, 0f, adjustZ(netZ), w, h)

                drawLine(netMeshColor, top, bot, strokeWidth = 1f)
            }

            for (i in 1 until horizontalStruts) {
                val fraction = i.toFloat() / horizontalStruts
                val yPos = netHeight * fraction

                val left = GameCoordinates.project3DToScreen(netLeftX, yPos, adjustZ(netZ), w, h)
                val right = GameCoordinates.project3DToScreen(netRightX, yPos, adjustZ(netZ), w, h)

                drawLine(netMeshColor, left, right, strokeWidth = 1f)
            }

            val pNetLeftBot = GameCoordinates.project3DToScreen(netLeftX, 0f, adjustZ(netZ), w, h)
            val pNetRightBot = GameCoordinates.project3DToScreen(netRightX, 0f, adjustZ(netZ), w, h)
            val pNetLeftTop = GameCoordinates.project3DToScreen(netLeftX, netHeight, adjustZ(netZ), w, h)
            val pNetRightTop = GameCoordinates.project3DToScreen(netRightX, netHeight, adjustZ(netZ), w, h)

            drawLine(netPostColor, pNetLeftBot, pNetLeftTop, strokeWidth = 6f)
            drawLine(netPostColor, pNetRightBot, pNetRightTop, strokeWidth = 6f)
            drawLine(netTapeColor, pNetLeftTop, pNetRightTop, strokeWidth = 3f)
            drawLine(netTapeColor, pNetLeftBot, pNetRightBot, strokeWidth = 2f)

            if (ballX != 0f || ballY != 0f) {
                val ballPos = GameCoordinates.project3DToScreen(
                    ballX,
                    30f,
                    adjustZ(ballY),
                    w, h
                )
                drawCircle(Color(0xFFFFFFFF), radius = 15f, center = ballPos)
            }
        }
    }
}

/**
 * Overloaded version of PingPongTable for development mode.
 * This version doesn't require parameters and uses default values.
 */
@Composable
fun PingPongTable() {
    // Create a dummy WebSocketManager for development
    val webSocketManager = remember { WebSocketManager() }

    // Use player 1 as default for development
    val playerNumber = 1

    // Call the main PingPongTable with the development parameters
    PingPongTable(
        webSocketManager = webSocketManager,
        playerNumber = playerNumber
    )
}
