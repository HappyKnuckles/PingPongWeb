package org.tabletennis.project.game

import androidx.compose.animation.core.*
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
import org.tabletennis.project.network.WebSocketManager
import kotlin.math.sqrt
import kotlin.random.Random

@Composable
fun PingPongTable(
    webSocketManager: WebSocketManager,
    playerNumber: Int
) {
    var score1 by remember { mutableStateOf(0) }
    var score2 by remember { mutableStateOf(0) }

    var paddleY by remember { mutableStateOf(0f) }

    val (initialTableX, initialTableZ) = remember {
        GameCoordinates.mapGameToTable(0f, -99f)
    }

    var ballX by remember { mutableStateOf(initialTableX) }
    var ballY by remember { mutableStateOf(initialTableZ) }

    var targetBallX by remember { mutableStateOf(initialTableX) }
    var targetBallY by remember { mutableStateOf(initialTableZ) }

    var previousBallX by remember { mutableStateOf(initialTableX) }
    var previousBallY by remember { mutableStateOf(initialTableZ) }

    var ballVelocity by remember { mutableStateOf(0f) }

    val animationProgress = remember { Animatable(initialValue = 1f) }

    var controlPointX by remember { mutableStateOf(0f) }
    var controlPointY by remember { mutableStateOf(0f) }
    var useCurvedPath by remember { mutableStateOf(false) }

    val collisionEvent by webSocketManager.collisionEvent.collectAsState()

    LaunchedEffect(collisionEvent) {
        collisionEvent?.let { event ->
            if (event.v > 0f) {
                previousBallX = ballX
                previousBallY = ballY

                val (mappedTargetX, _) = GameCoordinates.mapGameToTable(event.x, 0f)
                targetBallX = mappedTargetX

                val targetGameY = if (event.y > 0) -99f else 99f

                val (_, mappedTargetZ) = GameCoordinates.mapGameToTable(0f, targetGameY)
                targetBallY = mappedTargetZ

                ballVelocity = event.v

                useCurvedPath = Random.nextFloat() < 0.3f

                if (useCurvedPath) {
                    val dx = targetBallX - previousBallX
                    val dy = targetBallY - previousBallY

                    val perpX = -dy
                    val perpY = dx

                    val length = sqrt(perpX * perpX + perpY * perpY)
                    val scale = if (length > 0) Random.nextFloat() * 100f + 50f else 0f

                    controlPointX = (previousBallX + targetBallX) / 2f + (perpX / length) * scale
                    controlPointY = (previousBallY + targetBallY) / 2f + (perpY / length) * scale
                }

                animationProgress.snapTo(0f)
                animationProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = (3000 / ballVelocity).toInt().coerceIn(300, 1500),
                        easing = EaseInOutQuad
                    )
                )
            }
        }
    }

    LaunchedEffect(animationProgress.value) {
        if (useCurvedPath) {
            val t = animationProgress.value
            val oneMinusT = 1 - t

            ballX = oneMinusT * oneMinusT * previousBallX +
                    2 * oneMinusT * t * controlPointX +
                    t * t * targetBallX

            ballY = oneMinusT * oneMinusT * previousBallY +
                    2 * oneMinusT * t * controlPointY +
                    t * t * targetBallY
        } else {
            ballX = previousBallX + (targetBallX - previousBallX) * animationProgress.value
            ballY = previousBallY + (targetBallY - previousBallY) * animationProgress.value
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

            fun project(x: Float, y: Float, z: Float): Offset {
                val effectiveX = if (playerNumber == 2) -x else x
                val effectiveZ = if (playerNumber == 2) -z else z
                return GameCoordinates.project3DToScreen(effectiveX, y, effectiveZ, w, h)
            }

            val p1 = project(-halfW, 0f, -halfL)
            val p2 = project(halfW, 0f, -halfL)
            val p3 = project(halfW, 0f, halfL)
            val p4 = project(-halfW, 0f, halfL)

            val tablePath = Path().apply {
                moveTo(p1.x, p1.y)
                lineTo(p2.x, p2.y)
                lineTo(p3.x, p3.y)
                lineTo(p4.x, p4.y)
                close()
            }
            drawPath(tablePath, tableColor)
            drawPath(tablePath, Color.White, style = Stroke(width = 2f))

            val centerLineStart = project(0f, 0f, -halfL)
            val centerLineEnd = project(0f, 0f, halfL)

            drawLine(
                color = centerLineColor,
                start = centerLineStart,
                end = centerLineEnd,
                strokeWidth = 2f
            )

            val netZ = 0f
            val netLeftX = -halfW - netOverhang
            val netRightX = halfW + netOverhang

            val verticalStruts = 20
            val horizontalStruts = 5

            for (i in 1 until verticalStruts) {
                val fraction = i.toFloat() / verticalStruts
                val xPos = netLeftX + (netRightX - netLeftX) * fraction

                val top = project(xPos, netHeight, netZ)
                val bot = project(xPos, 0f, netZ)

                drawLine(netMeshColor, top, bot, strokeWidth = 1f)
            }

            for (i in 1 until horizontalStruts) {
                val fraction = i.toFloat() / horizontalStruts
                val yPos = netHeight * fraction

                val left = project(netLeftX, yPos, netZ)
                val right = project(netRightX, yPos, netZ)

                drawLine(netMeshColor, left, right, strokeWidth = 1f)
            }

            val pNetLeftBot = project(netLeftX, 0f, netZ)
            val pNetRightBot = project(netRightX, 0f, netZ)
            val pNetLeftTop = project(netLeftX, netHeight, netZ)
            val pNetRightTop = project(netRightX, netHeight, netZ)

            drawLine(netPostColor, pNetLeftBot, pNetLeftTop, strokeWidth = 6f)
            drawLine(netPostColor, pNetRightBot, pNetRightTop, strokeWidth = 6f)
            drawLine(netTapeColor, pNetLeftTop, pNetRightTop, strokeWidth = 3f)
            drawLine(netTapeColor, pNetLeftBot, pNetRightBot, strokeWidth = 2f)

            if (ballX != 0f || ballY != 0f) {
                val ballPos = project(
                    ballX,
                    30f,
                    ballY
                )
                drawCircle(Color(0xFFFFFFFF), radius = 15f, center = ballPos)
            }
        }
    }
}

@Composable
fun PingPongTable() {
    val webSocketManager = remember { WebSocketManager() }
    val playerNumber = 1

    PingPongTable(
        webSocketManager = webSocketManager,
        playerNumber = playerNumber
    )
}

public class GameCoordinates {
    object TableDims {
        const val WIDTH = 1525f
        const val LENGTH = 1700f
        const val NET_HEIGHT = 135f

        const val GAME_TOP = 100f
        const val GAME_LEFT = -100f
        const val GAME_RIGHT = 100f

    }

    companion object {
        fun mapGameToTable(gameX: Float, gameY: Float): Pair<Float, Float> {
            val tableX = gameX * (TableDims.WIDTH / 2) / TableDims.GAME_RIGHT

            val effectiveLength = TableDims.LENGTH - 2
            val tableZ = gameY * (effectiveLength / 2) / TableDims.GAME_TOP

            return Pair(tableX, tableZ)
        }

        fun mapTableToGame(tableX: Float, tableZ: Float): Pair<Float, Float> {
            val gameX = tableX * TableDims.GAME_RIGHT / (TableDims.WIDTH / 2)

            val effectiveLength = TableDims.LENGTH - 2
            val gameY = tableZ * TableDims.GAME_TOP / (effectiveLength / 2)

            return Pair(gameX, gameY)
        }

        fun project3DToScreen(
            x: Float, y: Float, z: Float,
            screenWidth: Float, screenHeight: Float
        ): Offset {
            val cx = screenWidth / 2
            val cy = screenHeight / 2

            val cameraDist = 4500f
            val focalLength = 2500f
            val tilt = 0.8f

            val currentZ = z + cameraDist
            val safeZ = if (currentZ < 1f) 1f else currentZ
            val scale = focalLength / safeZ

            val screenX = cx + (x * scale)

            val verticalOffset = -(cy * 0.1f)

            val screenY = cy + verticalOffset - (y * scale) - (z * tilt * scale)

            return Offset(screenX, screenY)
        }
    }
}