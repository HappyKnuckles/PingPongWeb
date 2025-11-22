package org.tabletennis.project.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.tabletennis.project.network.WebSocketManager

class PingPongTable(
    private val webSocketManager: WebSocketManager,
    private val playerNumber: Int
) {
    @Composable
    fun PingPongTable() {
        // Status-Text für die Anzeige der Spielernummer
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            // Spielernummer-Anzeige am oberen Bildschirmrand
            Text(
                text = "Spieler $playerNumber", 
                color = Color.White,
                modifier = Modifier.padding(top = 16.dp)
            )
            
            // Tischtennis-Tisch zeichnen
            Canvas(modifier = Modifier.fillMaxSize().background(Color(0xFF222222))) {
                val w = size.width
                val h = size.height
    
                // Colors
                val tableSurfaceColor = Color(0xFF1565C0)
                val tableSideColor = Color(0xFF0D47A1)
                val legColor = Color(0xFF424242)
                val lineColor = Color.White.copy(alpha = 0.9f)
                val netMeshColor = Color.White.copy(alpha = 0.3f) // More transparent
                val netPostColor = Color(0xFF212121)
    
                val halfW = GameCoordinates.TableDims.WIDTH / 2
                val halfL = GameCoordinates.TableDims.LENGTH / 2
                val tableThickness = 40f
                val legHeight = 760f
                val legInsetL = 400f
                val legInsetW = 200f
    
                // Anpassen der Koordinaten je nach Spielernummer
                // Für Spieler 2 spiegeln wir die Z-Koordinate
                val adjustZ = { z: Float -> if (playerNumber == 2) -z else z }
    
                // 1. DRAW LEGS
                val legPositions = listOf(
                    Pair(-halfW + legInsetW, adjustZ(-halfL + legInsetL)),
                    Pair(halfW - legInsetW, adjustZ(-halfL + legInsetL)),
                    Pair(-halfW + legInsetW, adjustZ(halfL - legInsetL)),
                    Pair(halfW - legInsetW, adjustZ(halfL - legInsetL))
                )
    
                legPositions.forEach { (lx, lz) ->
                    val legTop = GameCoordinates.project3DToScreen(lx, -tableThickness, lz, w, h)
                    val legBot = GameCoordinates.project3DToScreen(lx, -legHeight, lz, w, h)
    
                    drawLine(
                        color = legColor,
                        start = legTop,
                        end = legBot,
                        strokeWidth = 8f * (2500f / (kotlin.math.abs(lz) + 4500f))
                    )
                }
    
                // 2. DRAW TABLE THICKNESS (Skirt)
                val c1Top = GameCoordinates.project3DToScreen(-halfW, 0f, adjustZ(-halfL), w, h)
                val c2Top = GameCoordinates.project3DToScreen(halfW, 0f, adjustZ(-halfL), w, h)
                val c3Top = GameCoordinates.project3DToScreen(halfW, 0f, adjustZ(halfL), w, h)
                val c4Top = GameCoordinates.project3DToScreen(-halfW, 0f, adjustZ(halfL), w, h)
    
                val c1Bot = GameCoordinates.project3DToScreen(-halfW, -tableThickness, adjustZ(-halfL), w, h)
                val c2Bot = GameCoordinates.project3DToScreen(halfW, -tableThickness, adjustZ(-halfL), w, h)
                val c3Bot = GameCoordinates.project3DToScreen(halfW, -tableThickness, adjustZ(halfL), w, h)
                val c4Bot = GameCoordinates.project3DToScreen(-halfW, -tableThickness, adjustZ(halfL), w, h)
    
                val faces = listOf(
                    listOf(c1Top, c2Top, c2Bot, c1Bot), // Front
                    listOf(c2Top, c3Top, c3Bot, c2Bot), // Right
                    listOf(c1Top, c4Top, c4Bot, c1Bot)  // Left
                )
    
                faces.forEach { pts ->
                    drawPath(Path().apply {
                        moveTo(pts[0].x, pts[0].y)
                        lineTo(pts[1].x, pts[1].y)
                        lineTo(pts[2].x, pts[2].y)
                        lineTo(pts[3].x, pts[3].y)
                        close()
                    }, tableSideColor)
                }
    
                // 3. DRAW SURFACE
                val surfacePath = Path().apply {
                    moveTo(c1Top.x, c1Top.y)
                    lineTo(c2Top.x, c2Top.y)
                    lineTo(c3Top.x, c3Top.y)
                    lineTo(c4Top.x, c4Top.y)
                    close()
                }
                drawPath(surfacePath, tableSurfaceColor)
                drawPath(surfacePath, lineColor, style = Stroke(width = 3f))
    
                // Mittellinie
                val centerNear = GameCoordinates.project3DToScreen(0f, 0f, adjustZ(-halfL), w, h)
                val centerFar = GameCoordinates.project3DToScreen(0f, 0f, adjustZ(halfL), w, h)
                drawLine(lineColor, centerNear, centerFar, strokeWidth = 1.5f)
    
                // 4. DRAW NET
                val netOverhang = 80f
                val netZ = 0f
                val netLeftX = -halfW - netOverhang
                val netRightX = halfW + netOverhang
                val netTopY = GameCoordinates.TableDims.NET_HEIGHT
                val netBotY = 0f
    
                val pLeftBot = GameCoordinates.project3DToScreen(netLeftX, netBotY, adjustZ(netZ), w, h)
                val pRightBot = GameCoordinates.project3DToScreen(netRightX, netBotY, adjustZ(netZ), w, h)
                val pLeftTop = GameCoordinates.project3DToScreen(netLeftX, netTopY, adjustZ(netZ), w, h)
                val pRightTop = GameCoordinates.project3DToScreen(netRightX, netTopY, adjustZ(netZ), w, h)
    
                // Draw Posts
                drawLine(netPostColor, pLeftBot, pLeftTop, strokeWidth = 4f)
                drawLine(netPostColor, pRightBot, pRightTop, strokeWidth = 4f)
    
                // Draw Tape
                drawLine(Color.White, pLeftTop, pRightTop, strokeWidth = 2f)
    
                // Mesh
                val vertLines = 25
                for (i in 1 until vertLines) {
                    val fraction = i / vertLines.toFloat()
                    val xWorld = netLeftX + (netRightX - netLeftX) * fraction
                    val start = GameCoordinates.project3DToScreen(xWorld, netBotY, adjustZ(netZ), w, h)
                    val end = GameCoordinates.project3DToScreen(xWorld, netTopY, adjustZ(netZ), w, h)
                    drawLine(netMeshColor, start, end, strokeWidth = 0.5f)
                }
            }
        }
        
        // Hier könnten wir später WebSocket-Logik implementieren
        DisposableEffect(Unit) {
            onDispose {
                // Beim Verlassen aufräumen
            }
        }
    }
}