package org.tabletennis.project.game

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.tabletennis.project.network.WebSocketManager

private object GameColors {
    val FloorGradient = listOf(Color(0xFF333333), Color(0xFF111111))
    val TableBase = Color(0xFF1565C0)
    val TableDark = Color(0xFF0D47A1)
    val TableSide = Color(0xFF0A3880)
    val TableBottom = Color(0xFF072A5C)
    val CenterLine = Color.White.copy(alpha = 0.8f)
    val NetPost = Color(0xFF444444)
    val NetMesh = Color.White.copy(alpha = 0.3f)
    val NetTape = Color.White
    val Leg = Color(0xFF222222)
    val LegHighlight = Color(0xFF444444)
    val Shadow = Color.Black.copy(alpha = 0.5f)
    val BallShadow = Color.Black.copy(alpha = 0.3f)
    val TextWhite = Color.White
    val TextGray = Color.Gray
    val MessageGold = Color(0xFFFFD700)
}

@Composable
fun PingPongTable(
    webSocketManager: WebSocketManager,
    playerNumber: Int
) {
    var score1 by remember { mutableIntStateOf(0) }
    var score2 by remember { mutableIntStateOf(0) }
    var scoreMessage by remember { mutableStateOf("") }

    var ballX by remember { mutableFloatStateOf(0f) }
    var ballY by remember { mutableFloatStateOf(0f) }

    val coordinatesEvent by webSocketManager.coordinatesEvent.collectAsState()
    val scoreEvent by webSocketManager.scoreEvent.collectAsState()

    LaunchedEffect(coordinatesEvent) {
        coordinatesEvent?.let { event ->
            val (mappedX, mappedZ) = GameCoordinates.mapGameToTable(event.x, event.y)
            ballX = mappedX
            ballY = mappedZ
            scoreMessage = ""
        }
    }

    LaunchedEffect(scoreEvent) {
        scoreEvent?.let { event ->
            if (event.score.size >= 2) {
                if (playerNumber == 1) {
                    score1 = event.score[0]
                    score2 = event.score[1]
                } else {
                    score1 = event.score[1]
                    score2 = event.score[0]
                }
                scoreMessage = event.message
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(GameColors.FloorGradient)),
        contentAlignment = Alignment.TopCenter
    ) {

        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    val w = size.width
                    val h = size.height

                    val halfW = GameCoordinates.TableDims.WIDTH / 2
                    val halfL = GameCoordinates.TableDims.LENGTH / 2
                    val netHeight = GameCoordinates.TableDims.NET_HEIGHT
                    val tableHeightFromGround = 760f
                    val tableThickness = 25f
                      val legInset = 50f
                    val legWidth = 12f
                    val floorY = -tableHeightFromGround

                    fun project(x: Float, y: Float, z: Float): Offset {
                        val effectiveX = if (playerNumber == 2) -x else x
                        val effectiveZ = if (playerNumber == 2) -z else z
                        return GameCoordinates.project3DToScreen(effectiveX, y, effectiveZ, w, h)
                    }

                    val s1 = project(-halfW, floorY, -halfL)
                    val s2 = project(halfW, floorY, -halfL)
                    val s3 = project(halfW, floorY, halfL)
                    val s4 = project(-halfW, floorY, halfL)
                    val shadowPath = Path().apply {
                        moveTo(s1.x, s1.y); lineTo(s2.x, s2.y); lineTo(s3.x, s3.y); lineTo(s4.x, s4.y); close()
                    }

                    val t1 = project(-halfW, 0f, -halfL)
                    val t2 = project(halfW, 0f, -halfL)
                    val t3 = project(halfW, 0f, halfL)
                    val t4 = project(-halfW, 0f, halfL)
                    val tablePath = Path().apply {
                        moveTo(t1.x, t1.y); lineTo(t2.x, t2.y); lineTo(t3.x, t3.y); lineTo(t4.x, t4.y); close()
                    }

                    val b1 = project(-halfW, -tableThickness, -halfL)
                    val b2 = project(halfW, -tableThickness, -halfL)
                    val b3 = project(halfW, -tableThickness, halfL)
                    val b4 = project(-halfW, -tableThickness, halfL)
                    val tableBottomPath = Path().apply {
                        moveTo(b1.x, b1.y); lineTo(b2.x, b2.y); lineTo(b3.x, b3.y); lineTo(b4.x, b4.y); close()
                    }

                    val frontSidePath = Path().apply {
                        moveTo(t1.x, t1.y); lineTo(t2.x, t2.y); lineTo(b2.x, b2.y); lineTo(b1.x, b1.y); close()
                    }

                    val backSidePath = Path().apply {
                        moveTo(t3.x, t3.y); lineTo(t4.x, t4.y); lineTo(b4.x, b4.y); lineTo(b3.x, b3.y); close()
                    }

                    val leftSidePath = Path().apply {
                        moveTo(t4.x, t4.y); lineTo(t1.x, t1.y); lineTo(b1.x, b1.y); lineTo(b4.x, b4.y); close()
                    }

                    val rightSidePath = Path().apply {
                        moveTo(t2.x, t2.y); lineTo(t3.x, t3.y); lineTo(b3.x, b3.y); lineTo(b2.x, b2.y); close()
                    }

                    val tableGradient = Brush.linearGradient(
                        colors = listOf(GameColors.TableDark, GameColors.TableBase),
                        start = t1,
                        end = t4
                    )

                    val legsCoords = listOf(
                        Triple(-halfW + legInset, floorY, -halfL + legInset),
                        Triple(halfW - legInset, floorY, -halfL + legInset),
                        Triple(-halfW + legInset, floorY, halfL - legInset),
                        Triple(halfW - legInset, floorY, halfL - legInset)
                    )
                    val drawOrder = if (playerNumber == 1) listOf(0, 1, 2, 3) else listOf(2, 3, 0, 1)
                    val legLines = drawOrder.map { idx ->
                        val (lx, ly, lz) = legsCoords[idx]
                        project(lx, 0f, lz) to project(lx, ly, lz)
                    }

                    val netZ = 0f
                    val netOverhang = 20f
                    val netLeftX = -halfW - netOverhang
                    val netRightX = halfW + netOverhang

                    val netVLines = (1 until 20).map { i ->
                        val fraction = i.toFloat() / 20
                        val xPos = netLeftX + (netRightX - netLeftX) * fraction
                        project(xPos, netHeight, netZ) to project(xPos, 0f, netZ)
                    }
                    val netHLines = (1 until 5).map { i ->
                        val fraction = i.toFloat() / 5
                        val yPos = netHeight * fraction
                        project(netLeftX, yPos, netZ) to project(netRightX, yPos, netZ)
                    }

                    val pNetLeftBot = project(netLeftX, 0f, netZ)
                    val pNetRightBot = project(netRightX, 0f, netZ)
                    val pNetLeftTop = project(netLeftX, netHeight, netZ)
                    val pNetRightTop = project(netRightX, netHeight, netZ)

                    val cStart = project(0f, 0f, -halfL)
                    val cEnd = project(0f, 0f, halfL)

                    onDrawBehind {
                        drawPath(shadowPath, GameColors.Shadow)

                        legLines.forEach { (start, end) ->
                            drawLine(GameColors.Leg, start, end, strokeWidth = legWidth)

                            val highlightOffset = Offset(2f, 2f)
                            drawLine(
                                GameColors.LegHighlight, 
                                start.plus(highlightOffset), 
                                end.plus(highlightOffset), 
                                strokeWidth = legWidth / 3
                            )
                        }

                        drawPath(tableBottomPath, GameColors.TableBottom)

                        drawPath(frontSidePath, GameColors.TableSide)
                        drawPath(backSidePath, GameColors.TableSide)
                        drawPath(leftSidePath, GameColors.TableSide)
                        drawPath(rightSidePath, GameColors.TableSide)

                        drawPath(tablePath, tableGradient)

                        drawPath(tablePath, Color.White, style = Stroke(width = 2f))

                        drawLine(GameColors.CenterLine, cStart, cEnd, strokeWidth = 2f)

                        netVLines.forEach { (s, e) -> drawLine(GameColors.NetMesh, s, e, strokeWidth = 1f) }
                        netHLines.forEach { (s, e) -> drawLine(GameColors.NetMesh, s, e, strokeWidth = 1f) }

                        drawLine(GameColors.NetPost, pNetLeftBot, pNetLeftTop, strokeWidth = 6f)
                        drawLine(GameColors.NetPost, pNetRightBot, pNetRightTop, strokeWidth = 6f)
                        drawLine(GameColors.NetTape, pNetLeftTop, pNetRightTop, strokeWidth = 3f)
                        drawLine(GameColors.NetTape, pNetLeftBot, pNetRightBot, strokeWidth = 2f)

                        if (ballX != 0f || ballY != 0f) {
                            val ballRadius = 10f

                            val ballShadowPos = project(ballX + 5f, 0f, ballY + 5f)
                            drawCircle(
                                color = GameColors.BallShadow,
                                radius = ballRadius * 0.8f,
                                center = ballShadowPos
                            )

                            val ballVisualPos = project(ballX, 30f, ballY)

                            val ballGradient = Brush.radialGradient(
                                colors = listOf(Color.White, Color(0xFFDDDDDD), Color(0xFFAAAAAA)),
                                center = Offset(ballVisualPos.x - ballRadius/3, ballVisualPos.y - ballRadius/3),
                                radius = ballRadius * 1.2f
                            )

                            drawCircle(
                                brush = ballGradient,
                                radius = ballRadius,
                                center = ballVisualPos
                            )
                        }
                    }
                }
        )

        ScoreOverlay(score1, score2, playerNumber)

        BigMessageOverlay(scoreMessage)
    }
}

@Composable
private fun ScoreOverlay(s1: Int, s2: Int, pNum: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "$s1 : $s2",
            color = GameColors.TextWhite,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "You are Player $pNum",
            color = GameColors.TextGray,
            fontSize = 16.sp
        )
    }
}

@Composable
private fun BigMessageOverlay(message: String) {
    if (message.isNotEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (-50).dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                color = GameColors.MessageGold,
                fontSize = 72.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                lineHeight = 72.sp,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.8f),
                        offset = Offset(4f, 4f),
                        blurRadius = 8f
                    )
                )
            )
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
