package miuix.path

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import kotlin.math.*

class CornerSegment {

    var bounds: Rect = Rect.Zero
    var radius: Float = 0f
    var hSmoothness: Double = 0.0
    var vSmoothness: Double = 0.0
    var hAngle: Double = 0.0
    var vAngle: Double = 0.0
    var straightSegmentAngle: Float = 0f
    
    var hControlPoints: Array<Offset> = Array(4) { Offset.Zero }
    var vControlPoints: Array<Offset> = Array(4) { Offset.Zero }

    /**
     * 根据给定参数计算该圆角的所有几何数据。
     *
     * @param radius     圆角半径
     * @param bounds     整体矩形区域
     * @param offsetX    水平偏移量（内边距）
     * @param offsetY    垂直偏移量（内边距）
     * @param smoothness 平滑度 (0.0 - 1.0)
     * @param position   圆角位置枚举
     */
    fun compute(
        radius: Float,
        bounds: Rect,
        offsetX: Float,
        offsetY: Float,
        smoothness: Double,
        position: CornerPosition
    ) {
        val clampedVSmoothness: Float
        val vBezierScale: Double

        this.radius = radius

        val rectWidth = bounds.width
        val rectHeight = bounds.height
        val left = bounds.left
        val top = bounds.top
        val right = bounds.right
        val bottom = bounds.bottom

        val r = this.radius
        this.hSmoothness = if (SmoothPathProvider.isWidthConstrained(
                rectWidth,
                r,
                r,
                smoothness,
                SmoothPathProvider.SMOOTH_FACTOR
            )
        ) {
            ((rectWidth / (r * 2.0f)) - 1.0f).toDouble()
                .div(SmoothPathProvider.SMOOTH_FACTOR.toDouble())
                .coerceIn(0.0, 1.0)
        } else {
            smoothness
        }

        val r2 = this.radius
        if (SmoothPathProvider.isHeightConstrained(
                rectHeight,
                r2,
                r2,
                smoothness,
                SmoothPathProvider.SMOOTH_FACTOR
            )
        ) {
            clampedVSmoothness = SmoothPathProvider.SMOOTH_FACTOR
            vBezierScale = ((rectHeight / (r2 * 2.0f)) - 1.0f).toDouble()
                .div(SmoothPathProvider.SMOOTH_FACTOR.toDouble())
                .coerceIn(0.0, 1.0)
        } else {
            clampedVSmoothness = SmoothPathProvider.SMOOTH_FACTOR
            vBezierScale = smoothness
        }
        this.vSmoothness = vBezierScale

        val hAngle = (this.hSmoothness * PI) / 4.0
        this.hAngle = hAngle
        val vAngle = (vBezierScale * PI) / 4.0
        this.vAngle = vAngle

        this.straightSegmentAngle = SmoothPathProvider.radiansToDegrees(
            (PI / 2.0 - vAngle) - hAngle
        ).toFloat()

        val smoothFactor = clampedVSmoothness.toDouble()
        val hSmoothnessScaled = this.hSmoothness * smoothFactor
        val hBezierScale: Double
        if (hAngle == 0.0) {
            hBezierScale = 0.0
        } else {
            val halfH = hAngle / 2.0
            hBezierScale = (((cos(hAngle) + 1.0)
                    * ((tan(halfH) + (hSmoothnessScaled * SmoothPathProvider.SMOOTH_FACTOR_DOUBLE)) * 2.0))
                    / (tan(halfH) * 3.0)) - 1.0
        }

        val hSin = (1.0 - sin(this.hAngle)) * this.radius
        val hCos = (1.0 - cos(this.hAngle)) * this.radius
        val hTan = (1.0 - tan(this.hAngle / 2.0)) * this.radius
        val hTan2 = (tan(this.hAngle / 2.0) * (this.radius * 1.5)) / (cos(this.hAngle) + 1.0)
        val hBezierOffset = hBezierScale * hTan2

        val vSmoothnessScaled = this.vSmoothness * smoothFactor
        var vBezierScaleV = 0.0
        if (vAngle != 0.0) {
            val halfV = vAngle / 2.0
            vBezierScaleV = (((cos(vAngle) + 1.0)
                    * ((tan(halfV) + (vSmoothnessScaled * SmoothPathProvider.SMOOTH_FACTOR_DOUBLE)) * 2.0))
                    / (tan(halfV) * 3.0)) - 1.0
        }

        val vCos = (1.0 - cos(this.vAngle)) * this.radius
        val vSin = (1.0 - sin(this.vAngle)) * this.radius
        val vTan = (1.0 - tan(this.vAngle / 2.0)) * this.radius
        val vTan2 = (tan(this.vAngle / 2.0) * (this.radius * 1.5)) / (cos(this.vAngle) + 1.0)
        val vBezierOffset = vBezierScaleV * vTan2

        val vCtrl = this.vControlPoints
        val hCtrl = this.hControlPoints

        when (position) {
            CornerPosition.TOP_LEFT -> {
                // 左上角
                val cornerLeft = left + offsetX
                val cornerTop = top + offsetY
                val diameter = this.radius * 2.0f
                this.bounds = Rect(cornerLeft, cornerTop, diameter + cornerLeft, diameter + cornerTop)

                val cx = cornerLeft.toDouble()
                val cy = cornerTop.toDouble()
                hCtrl[0] = Offset((hSin + cx).toFloat(), (hCos + cy).toFloat())
                hCtrl[1] = Offset((hTan + cx).toFloat(), cornerTop)
                val hBase = hTan + hTan2
                hCtrl[2] = Offset((hBase + cx).toFloat(), cornerTop)
                hCtrl[3] = Offset((hBase + hBezierOffset + cx).toFloat(), cornerTop)

                val vBase = vTan + vTan2
                vCtrl[0] = Offset(cornerLeft, (vBezierOffset + vBase + cy).toFloat())
                vCtrl[1] = Offset(cornerLeft, (vBase + cy).toFloat())
                vCtrl[2] = Offset(cornerLeft, (vTan + cy).toFloat())
                vCtrl[3] = Offset((vCos + cx).toFloat(), (vSin + cy).toFloat())
            }

            CornerPosition.TOP_RIGHT -> {
                // 右上角
                val cornerTopTR = top + offsetY
                val diameterTR = this.radius * 2.0f
                val cornerRightTR = right - offsetX
                this.bounds = Rect((right - diameterTR) - offsetX, cornerTopTR, cornerRightTR, diameterTR + cornerTopTR)

                val rx = right.toDouble()
                val hBaseTR = rx - hTan - hTan2
                val ox = offsetX.toDouble()
                hCtrl[0] = Offset((hBaseTR - hBezierOffset - ox).toFloat(), cornerTopTR)
                hCtrl[1] = Offset((hBaseTR - ox).toFloat(), cornerTopTR)
                hCtrl[2] = Offset((rx - hTan - ox).toFloat(), cornerTopTR)
                val cyTR = cornerTopTR.toDouble()
                hCtrl[3] = Offset(((rx - hSin) - ox).toFloat(), (hCos + cyTR).toFloat())

                val vBaseTR = vTan + vTan2
                vCtrl[0] = Offset(((rx - vCos) - ox).toFloat(), (vSin + cyTR).toFloat())
                vCtrl[1] = Offset(cornerRightTR, (vTan + cyTR).toFloat())
                vCtrl[2] = Offset(cornerRightTR, (vBaseTR + cyTR).toFloat())
                vCtrl[3] = Offset(cornerRightTR, (vBaseTR + vBezierOffset + cyTR).toFloat())
            }

            CornerPosition.BOTTOM_RIGHT -> {
                // 右下角
                val diameterBR = this.radius * 2.0f
                val cornerRightBR = right - offsetX
                val cornerBottomBR = bottom - offsetY
                this.bounds = Rect(
                    (right - diameterBR) - offsetX, (bottom - diameterBR) - offsetY,
                    cornerRightBR, cornerBottomBR
                )

                val rxBR = right.toDouble()
                val oxBR = offsetX.toDouble()
                val hBaseBR = rxBR - hTan - hTan2
                val hSinX = ((rxBR - hSin) - oxBR).toFloat()
                val by = bottom.toDouble()
                val bCos = by - hCos
                val oy = offsetY.toDouble()
                hCtrl[0] = Offset(hSinX, (bCos - oy).toFloat())
                hCtrl[1] = Offset((rxBR - hTan - oxBR).toFloat(), cornerBottomBR)
                hCtrl[2] = Offset((hBaseBR - oxBR).toFloat(), cornerBottomBR)
                hCtrl[3] = Offset((hBaseBR - hBezierOffset - oxBR).toFloat(), cornerBottomBR)

                val vBase1 = by - vTan
                val vBase2 = vBase1 - vTan2
                vCtrl[0] = Offset(cornerRightBR, (vBase2 - vBezierOffset - oy).toFloat())
                vCtrl[1] = Offset(cornerRightBR, (vBase2 - oy).toFloat())
                vCtrl[2] = Offset(cornerRightBR, (vBase1 - oy).toFloat())
                vCtrl[3] = Offset(((rxBR - vCos) - oxBR).toFloat(), ((by - vSin) - oy).toFloat())
            }

            CornerPosition.BOTTOM_LEFT -> {
                // 左下角
                val cornerLeftBL = left + offsetX
                val diameterBL = this.radius * 2.0f
                val cornerBottomBL = bottom - offsetY
                this.bounds = Rect(
                    cornerLeftBL, (bottom - diameterBL) - offsetY,
                    diameterBL + cornerLeftBL, cornerBottomBL
                )

                val hBaseBL = hTan + hTan2
                val cxBL = cornerLeftBL.toDouble()
                hCtrl[0] = Offset((hBezierOffset + hBaseBL + cxBL).toFloat(), cornerBottomBL)
                hCtrl[1] = Offset((hBaseBL + cxBL).toFloat(), cornerBottomBL)
                hCtrl[2] = Offset((hTan + cxBL).toFloat(), cornerBottomBL)
                val byBL = bottom.toDouble()
                val oyBL = offsetY.toDouble()
                hCtrl[3] = Offset((hSin + cxBL).toFloat(), ((byBL - hCos) - oyBL).toFloat())

                vCtrl[0] = Offset((vCos + cxBL).toFloat(), ((byBL - vSin) - oyBL).toFloat())
                val vBase1BL = byBL - vTan
                vCtrl[1] = Offset(cornerLeftBL, (vBase1BL - oyBL).toFloat())
                val vBase2BL = vBase1BL - vTan2
                vCtrl[2] = Offset(cornerLeftBL, (vBase2BL - oyBL).toFloat())
                vCtrl[3] = Offset(cornerLeftBL, ((vBase2BL - vBezierOffset) - oyBL).toFloat())
            }
        }
    }
}
