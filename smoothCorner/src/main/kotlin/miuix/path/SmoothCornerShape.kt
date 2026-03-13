package miuix.path

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

/**
 * Compose 风格的平滑圆角形状
 *
 * @param radius 统一圆角半径 (Px)
 * @param smoothness 平滑度 (0.0 - 1.0)
 */
class SmoothCornerShape(
    private val radius: Float,
    private val smoothness: Double = 0.9
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val radii = floatArrayOf(
            radius, radius, // TL
            radius, radius, // TR
            radius, radius, // BR
            radius, radius  // BL
        )
        val rect = Rect(0f, 0f, size.width, size.height)
        val path = SmoothPathFactory.createSmoothPath(rect, radii, smoothness)
        return Outline.Generic(path)
    }
}
