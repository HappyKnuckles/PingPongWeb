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

    // Ball position state
    var ballX by remember { mutableStateOf(0f) }
    var ballY by remember { mutableStateOf(0f) }
    var ballVelocity by remember { mutableStateOf(0f) }

    // Collect collision events
    val collisionEvent by webSocketManager.collisionEvent.collectAsState()

    // Update ball position when collision event is received
    LaunchedEffect(collisionEvent) {
        collisionEvent?.let { event ->
            ballX = event.x
            ballY = event.y
            ballVelocity = event.v
        }
    }

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
        // --- Score HUD ---
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

        // --- 3D Game Canvas ---
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // --- Colors ---
            val tableColor = Color(0xFF1565C0) // Standard ITTF Blue
            val centerLineColor = Color.White.copy(alpha = 0.8f) // Regulation White Line
            val netPostColor = Color(0xFF212121) // Dark Grey Posts
            val netMeshColor = Color.White.copy(alpha = 0.3f) // Semi-transparent mesh
            val netTapeColor = Color.White // Solid white top tape

            // --- Dimensions ---
            val halfW = GameCoordinates.TableDims.WIDTH / 2
            val halfL = GameCoordinates.TableDims.LENGTH / 2
            val netHeight = 150f
            val netOverhang = 20f

            // Helper to flip the board for Player 2
            fun adjustZ(z: Float): Float = if (playerNumber == 2) -z else z

            // 1. Draw Table Surface
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
            drawPath(tablePath, Color.White, style = Stroke(width = 2f)) // White border

            // 2. Draw Center Line (The "Middle Line")
            val centerLineStart = GameCoordinates.project3DToScreen(0f, 0f, adjustZ(-halfL), w, h)
            val centerLineEnd = GameCoordinates.project3DToScreen(0f, 0f, adjustZ(halfL), w, h)

            drawLine(
                color = centerLineColor,
                start = centerLineStart,
                end = centerLineEnd,
                strokeWidth = 2f
            )

            // 3. Draw Net Structure
            val netZ = 0f // Net is always at center Z
            val netLeftX = -halfW - netOverhang
            val netRightX = halfW + netOverhang

            // 3a. Draw the Mesh (Grid)
            val verticalStruts = 20
            val horizontalStruts = 5

            // Vertical Mesh Lines
            for (i in 1 until verticalStruts) {
                val fraction = i.toFloat() / verticalStruts
                val xPos = netLeftX + (netRightX - netLeftX) * fraction

                val top = GameCoordinates.project3DToScreen(xPos, netHeight, adjustZ(netZ), w, h)
                val bot = GameCoordinates.project3DToScreen(xPos, 0f, adjustZ(netZ), w, h)

                drawLine(netMeshColor, top, bot, strokeWidth = 1f)
            }

            // Horizontal Mesh Lines
            for (i in 1 until horizontalStruts) {
                val fraction = i.toFloat() / horizontalStruts
                val yPos = netHeight * fraction

                val left = GameCoordinates.project3DToScreen(netLeftX, yPos, adjustZ(netZ), w, h)
                val right = GameCoordinates.project3DToScreen(netRightX, yPos, adjustZ(netZ), w, h)

                drawLine(netMeshColor, left, right, strokeWidth = 1f)
            }

            // 3b. Draw Posts, Top Tape, and Bottom Line
            val pNetLeftBot = GameCoordinates.project3DToScreen(netLeftX, 0f, adjustZ(netZ), w, h)
            val pNetRightBot = GameCoordinates.project3DToScreen(netRightX, 0f, adjustZ(netZ), w, h)
            val pNetLeftTop = GameCoordinates.project3DToScreen(netLeftX, netHeight, adjustZ(netZ), w, h)
            val pNetRightTop = GameCoordinates.project3DToScreen(netRightX, netHeight, adjustZ(netZ), w, h)

            // Left Post
            drawLine(netPostColor, pNetLeftBot, pNetLeftTop, strokeWidth = 4f)
            // Right Post
            drawLine(netPostColor, pNetRightBot, pNetRightTop, strokeWidth = 4f)
            // Top White Tape
            drawLine(netTapeColor, pNetLeftTop, pNetRightTop, strokeWidth = 3f)
            // Bottom White Line
            drawLine(netTapeColor, pNetLeftBot, pNetRightBot, strokeWidth = 2f)

            // 4. Draw Paddles
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

            // 5. Draw Ball
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