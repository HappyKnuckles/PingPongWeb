package org.tabletennis.project.game

import androidx.compose.ui.geometry.Offset

public class GameCoordinates {
    object TableDims {
        const val WIDTH = 1525f
        const val LENGTH = 2740f
        const val NET_HEIGHT = 152.5f
    }

    companion object {
        fun project3DToScreen(
            x: Float, y: Float, z: Float,
            screenWidth: Float, screenHeight: Float
        ): Offset {
            val cx = screenWidth / 2
            val cy = screenHeight / 2

            // --- CAMERA ADJUSTMENTS ---
            // 1. Zoom out by moving camera further back (was 3000f)
            val cameraDist = 4500f

            // 2. Adjust lens to keep perspective nice
            val focalLength = 2500f

            val tilt = 0.6f

            val currentZ = z + cameraDist
            val safeZ = if (currentZ < 1f) 1f else currentZ
            val scale = focalLength / safeZ

            val screenX = cx + (x * scale)

            // --- POSITION ADJUSTMENT ---
            // Changed (cy * 0.3f) to -(cy * 0.2f).
            // This moves the table UPWARDS by 20% of screen height,
            // creating empty space at the bottom for the legs.
            val verticalOffset = -(cy * 0.2f)

            val screenY = cy + verticalOffset - (y * scale) - (z * tilt * scale)

            return Offset(screenX, screenY)
        }
    }
}