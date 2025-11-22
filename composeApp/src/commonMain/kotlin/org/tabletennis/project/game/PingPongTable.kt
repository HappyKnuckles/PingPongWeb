package org.tabletennis.project.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
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

    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF222222))
            .pointerInput(Unit) {
                detectDragGestures { _, dragAmount ->
                    val newY = paddleY + dragAmount.y

                    val halfTableWidth = GameCoordinates.TableDims.WIDTH / 2
                    paddleY = newY.coerceIn(-halfTableWidth + 50f, halfTableWidth - 50f)
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
            val legColor = Color(0xFF424242)
            val netColor = Color.White.copy(alpha = 0.8f)

            val halfW = GameCoordinates.TableDims.WIDTH / 2
            val halfL = GameCoordinates.TableDims.LENGTH / 2

            fun adjustZ(z: Float): Float = if (playerNumber == 2) -z else z

            val p1 = GameCoordinates.project3DToScreen(-halfW, 0f, adjustZ(-halfL), w, h)
            val p2 = GameCoordinates.project3DToScreen(halfW, 0f, adjustZ(-halfL), w, h)
            val p3 = GameCoordinates.project3DToScreen(halfW, 0f, adjustZ(halfL), w, h)
            val p4 = GameCoordinates.project3DToScreen(-halfW, 0f, adjustZ(halfL), w, h)

            val tablePath = Path().apply {
                moveTo(p1.x, p1.y)
                lineTo(p2.x, p2.y)
                lineTo(p3.x, p3.y)
                lineTo(p4.x, p4.y)
                close()
            }
            drawPath(tablePath, tableColor)
            drawPath(tablePath, Color.White, style = Stroke(width = 2f))

            val netLeft = GameCoordinates.project3DToScreen(-halfW - 20f, 0f, adjustZ(0f), w, h)
            val netRight = GameCoordinates.project3DToScreen(halfW + 20f, 0f, adjustZ(0f), w, h)
            val netTopLeft = GameCoordinates.project3DToScreen(-halfW - 20f, 150f, adjustZ(0f), w, h)
            val netTopRight = GameCoordinates.project3DToScreen(halfW + 20f, 150f, adjustZ(0f), w, h)

            drawLine(netColor, netLeft, netTopLeft, strokeWidth = 3f) // Post
            drawLine(netColor, netRight, netTopRight, strokeWidth = 3f) // Post
            drawLine(netColor, netTopLeft, netTopRight, strokeWidth = 2f) // Top tape

            val myPaddlePos = GameCoordinates.project3DToScreen(
                paddleY,
                30f,
                adjustZ(-halfL + 60f),
                w, h
            )
            drawCircle(Color(0xFFF44336), radius = 30f, center = myPaddlePos)

            val oppPaddlePos = GameCoordinates.project3DToScreen(
                opponentPaddleY,
                30f,
                adjustZ(halfL - 60f),
                w, h
            )
            drawCircle(Color(0xFF4CAF50), radius = 30f, center = oppPaddlePos)
        }
    }
}