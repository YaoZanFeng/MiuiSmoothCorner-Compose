package miuix.path

import androidx.compose.ui.geometry.Rect
import kotlin.math.min

object SmoothPathProvider {

    const val SMOOTH_FACTOR = 0.46f
    const val SMOOTH_FACTOR_DOUBLE = 0.46000000834465027

    /**
     * 根据矩形区域和四角半径数组，计算并返回完整的平滑圆角数据。
     *
     * @param bounds     整体矩形区域
     * @param radii      8个半径值
     * @param smoothness 平滑度 (0.0 - 1.0)
     * @param offsetX    水平偏移量（内边距）
     * @param offsetY    垂直偏移量（内边距）
     * @return 完整的 [SmoothCornerData]，若 radii 为 null 则返回 null
     */
    fun computeSmoothCorners(
        bounds: Rect,
        radii: FloatArray?,
        smoothness: Double,
        offsetX: Float,
        offsetY: Float
    ): SmoothCornerData? {
        if (radii == null) {
            return null
        }

        val rectWidth = bounds.width
        val rectHeight = bounds.height
        val cornerData = SmoothCornerData(rectWidth, rectHeight, smoothness)

        val safeRadii = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
        for (i in 0 until min(8, radii.size)) {
            if (!radii[i].isNaN()) {
                safeRadii[i] = radii[i]
            }
        }

        var tlRadiusX = safeRadii[0]
        var tlRadiusY = safeRadii[1]
        var trRadiusX = safeRadii[2]
        var trRadiusY = safeRadii[3]
        var brRadiusX = safeRadii[4]
        var brRadiusY = safeRadii[5]
        var blRadiusX = safeRadii[6]
        var blRadiusY = safeRadii[7]

        // 水平方向约束
        val sumTopWidth = tlRadiusX + trRadiusX
        if (sumTopWidth > rectWidth) {
            tlRadiusX = (tlRadiusX * rectWidth) / sumTopWidth
            trRadiusX = (trRadiusX * rectWidth) / sumTopWidth
        }
        val clampedTrX = trRadiusX

        // 垂直方向约束
        val sumLeftHeight = trRadiusY + brRadiusY
        if (sumLeftHeight > rectHeight) {
            trRadiusY = (trRadiusY * rectHeight) / sumLeftHeight
            brRadiusY = (brRadiusY * rectHeight) / sumLeftHeight
        }
        val clampedTrY = trRadiusY
        val clampedBrY = brRadiusY

        // 水平方向约束
        val sumBottomWidth = brRadiusX + blRadiusX
        if (sumBottomWidth > rectWidth) {
            brRadiusX = (brRadiusX * rectWidth) / sumBottomWidth
            blRadiusX = (rectWidth * blRadiusX) / sumBottomWidth
        }
        val clampedBrX = brRadiusX

        // 垂直方向约束
        val sumRightHeight = blRadiusY + tlRadiusY
        if (sumRightHeight > rectHeight) {
            blRadiusY = (blRadiusY * rectHeight) / sumRightHeight
            tlRadiusY = (rectHeight * tlRadiusY) / sumRightHeight
        }
        val clampedBlY = blRadiusY

        // 初始化四角并计算几何数据
        if (cornerData.topLeftCorner == null) cornerData.topLeftCorner = CornerSegment()
        if (cornerData.topRightCorner == null) cornerData.topRightCorner = CornerSegment()
        if (cornerData.bottomRightCorner == null) cornerData.bottomRightCorner = CornerSegment()
        if (cornerData.bottomLeftCorner == null) cornerData.bottomLeftCorner = CornerSegment()

        // 每个角取 x/y 半径中的较小值，保证对称性
        cornerData.topLeftCorner?.compute(
            min(tlRadiusX, tlRadiusY),
            bounds,
            offsetX,
            offsetY,
            smoothness,
            CornerPosition.TOP_LEFT
        )
        cornerData.topRightCorner?.compute(
            min(clampedTrX, clampedTrY),
            bounds,
            offsetX,
            offsetY,
            smoothness,
            CornerPosition.TOP_RIGHT
        )
        cornerData.bottomRightCorner?.compute(
            min(clampedBrX, clampedBrY),
            bounds,
            offsetX,
            offsetY,
            smoothness,
            CornerPosition.BOTTOM_RIGHT
        )
        cornerData.bottomLeftCorner?.compute(
            min(blRadiusX, clampedBlY),
            bounds,
            offsetX,
            offsetY,
            smoothness,
            CornerPosition.BOTTOM_LEFT
        )

        return cornerData
    }

    /**
     * 判断垂直方向上半径是否受到矩形高度的约束（即两倍半径+平滑延伸超过了可用高度）。
     */
    fun isHeightConstrained(
        availableHeight: Float,
        radius1: Float,
        radius2: Float,
        smoothness: Double,
        smoothFactor: Float
    ): Boolean {
        return availableHeight <= ((smoothness * smoothFactor) + 1.0) * (radius1 + radius2)
    }

    /**
     * 判断水平方向上半径是否受到矩形宽度的约束（即两倍半径+平滑延伸超过了可用宽度）。
     */
    fun isWidthConstrained(
        availableWidth: Float,
        radius1: Float,
        radius2: Float,
        smoothness: Double,
        smoothFactor: Float
    ): Boolean {
        return availableWidth <= ((smoothness * smoothFactor) + 1.0) * (radius1 + radius2)
    }

    /**
     * 将弧度转换为角度。
     */
    fun radiansToDegrees(radians: Double): Double {
        return (radians * 180.0) / Math.PI
    }
}
