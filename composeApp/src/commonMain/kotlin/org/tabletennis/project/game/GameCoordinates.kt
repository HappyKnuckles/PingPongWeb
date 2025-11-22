import androidx.compose.ui.geometry.Offset

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
