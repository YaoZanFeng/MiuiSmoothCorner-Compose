package miuix.path

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import kotlin.math.PI

object SmoothPathFactory {

    /**
     * 根据 8 个半径值生成平滑圆角路径
     */
    fun createSmoothPath(bounds: Rect, radii: FloatArray, smoothness: Double): Path {
        val data = SmoothPathProvider.computeSmoothCorners(bounds, radii, smoothness, 0f, 0f)

        if (data == null) return Path()

        val path = Path()

        val tl = data.topLeftCorner!!
        val tr = data.topRightCorner!!
        val br = data.bottomRightCorner!!
        val bl = data.bottomLeftCorner!!

        fun radToDeg(rad: Double): Float = (rad * 180.0 / PI).toFloat()

        val tlSweep = 90f - radToDeg(tl.hAngle) - radToDeg(tl.vAngle)
        val trSweep = 90f - radToDeg(tr.hAngle) - radToDeg(tr.vAngle)
        val brSweep = 90f - radToDeg(br.hAngle) - radToDeg(br.vAngle)
        val blSweep = 90f - radToDeg(bl.hAngle) - radToDeg(bl.vAngle)

        path.moveTo(tl.hControlPoints[3].x, tl.hControlPoints[3].y)

        path.lineTo(tr.hControlPoints[0].x, tr.hControlPoints[0].y)
        cubicTo(path, tr.hControlPoints[1], tr.hControlPoints[2], tr.hControlPoints[3])
        path.arcTo(tr.bounds, 270f + radToDeg(tr.hAngle), trSweep, false)
        cubicTo(path, tr.vControlPoints[1], tr.vControlPoints[2], tr.vControlPoints[3])

        path.lineTo(br.vControlPoints[0].x, br.vControlPoints[0].y)
        cubicTo(path, br.vControlPoints[1], br.vControlPoints[2], br.vControlPoints[3])
        path.arcTo(br.bounds, radToDeg(br.vAngle), brSweep, false)
        cubicTo(path, br.hControlPoints[1], br.hControlPoints[2], br.hControlPoints[3])

        path.lineTo(bl.hControlPoints[0].x, bl.hControlPoints[0].y)
        cubicTo(path, bl.hControlPoints[1], bl.hControlPoints[2], bl.hControlPoints[3])
        path.arcTo(bl.bounds, 90f + radToDeg(bl.hAngle), blSweep, false)
        cubicTo(path, bl.vControlPoints[1], bl.vControlPoints[2], bl.vControlPoints[3])

        path.lineTo(tl.vControlPoints[0].x, tl.vControlPoints[0].y)
        cubicTo(path, tl.vControlPoints[1], tl.vControlPoints[2], tl.vControlPoints[3])
        path.arcTo(tl.bounds, 180f + radToDeg(tl.vAngle), tlSweep, false)
        cubicTo(path, tl.hControlPoints[1], tl.hControlPoints[2], tl.hControlPoints[3])

        path.close()
        return path
    }

    private fun cubicTo(path: Path, p1: Offset, p2: Offset, p3: Offset) {
        path.cubicTo(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y)
    }
}
