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

            val cameraDist = 4500f

            val focalLength = 2500f

            val tilt = 0.6f

            val currentZ = z + cameraDist
            val safeZ = if (currentZ < 1f) 1f else currentZ
            val scale = focalLength / safeZ

            val screenX = cx + (x * scale)

            val verticalOffset = -(cy * 0.2f)

            val screenY = cy + verticalOffset - (y * scale) - (z * tilt * scale)

            return Offset(screenX, screenY)
        }
    }
}