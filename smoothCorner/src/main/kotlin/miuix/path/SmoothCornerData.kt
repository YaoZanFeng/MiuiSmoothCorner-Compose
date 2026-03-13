package miuix.path

/**
 * 平滑圆角数据
 *
 * @param width      宽度
 * @param height     高度
 * @param smoothness 平滑度 (0.0 - 1.0)
 */
class SmoothCornerData(
    val width: Float,
    val height: Float,
    val smoothness: Double
) {
    var topLeftCorner: CornerSegment? = null
    var topRightCorner: CornerSegment? = null
    var bottomRightCorner: CornerSegment? = null
    var bottomLeftCorner: CornerSegment? = null
}
