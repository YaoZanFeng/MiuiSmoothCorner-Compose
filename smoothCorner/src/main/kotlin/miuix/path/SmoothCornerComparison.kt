package miuix.path

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SmoothCornerComparison(modifier: Modifier = Modifier.fillMaxSize()) {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    
    val sizePx = with(density) { 150.dp.toPx() }
    val radiusPx = with(density) { 35.dp.toPx() }
    val strokeWidthPx = with(density) { 2.dp.toPx() }
    val labelTextSize = with(density) { 13.sp }

    Canvas(modifier = modifier) {
        val canvasHeight = size.height
        val leftMargin = with(density) { 40.dp.toPx() }
        val topOffset = (canvasHeight - sizePx) / 2f
        
        val rectLeft = Rect(leftMargin, topOffset, leftMargin + sizePx, topOffset + sizePx)

        val systemPath = Path().apply {
            addRoundRect(RoundRect(rectLeft, radiusPx, radiusPx))
        }

        val radii = floatArrayOf(
            radiusPx, radiusPx, radiusPx, radiusPx,
            radiusPx, radiusPx, radiusPx, radiusPx
        )
        val smoothPath = SmoothPathFactory.createSmoothPath(rectLeft, radii, 0.9)

        drawText(
            textMeasurer = textMeasurer,
            text = "原始对比 (180dp, R=35dp)",
            topLeft = Offset(rectLeft.left, rectLeft.top - with(density) { 25.dp.toPx() }),
            style = TextStyle(color = Color.Black, fontSize = labelTextSize)
        )
        
        drawPath(path = systemPath, color = Color.Red, style = Stroke(width = strokeWidthPx))
        drawPath(path = smoothPath, color = Color.Blue, style = Stroke(width = strokeWidthPx))

        drawMagnifiedComparison(
            originalRect = rectLeft, 
            sysPath = systemPath, 
            smPath = smoothPath, 
            strokeWidth = strokeWidthPx, 
            fontSize = labelTextSize, 
            textMeasurer = textMeasurer, 
            sizePx = sizePx
        )
        
        drawLegend(canvasHeight, density, textMeasurer, labelTextSize)
    }
}

private fun DrawScope.drawMagnifiedComparison(
    originalRect: Rect,
    sysPath: Path,
    smPath: Path,
    strokeWidth: Float,
    fontSize: TextUnit,
    textMeasurer: TextMeasurer,
    sizePx: Float
) {
    val magnification = 6f
    val rightAreaCenterX = size.width * 0.75f
    val rightAreaCenterY = size.height / 2f

    drawText(
        textMeasurer = textMeasurer,
        text = "放大 6 倍细节 (左上角)",
        topLeft = Offset(rightAreaCenterX - 100f, rightAreaCenterY - sizePx / 2 - 60f),
        style = TextStyle(color = Color.Black, fontSize = fontSize)
    )

    withTransform({
        translate(rightAreaCenterX, rightAreaCenterY)
        scale(magnification, magnification, pivot = Offset(0f, 0f))
        translate(-originalRect.left, -originalRect.top)
    }) {
        drawPath(path = sysPath, color = Color.Red, style = Stroke(width = strokeWidth / magnification))
        drawPath(path = smPath, color = Color.Blue, style = Stroke(width = strokeWidth / magnification))
    }
}

private fun DrawScope.drawLegend(
    canvasHeight: Float,
    density: Density,
    textMeasurer: TextMeasurer,
    fontSize: TextUnit
) {
    val legendY = canvasHeight - with(density) { 120.dp.toPx() }
    val legendX = with(density) { 40.dp.toPx() }

    drawRect(color = Color.Red, topLeft = Offset(legendX, legendY), size = Size(40f, 5f))
    drawText(
        textMeasurer = textMeasurer,
        text = "红色：Android系统圆角 (Compose)",
        topLeft = Offset(legendX + 50f, legendY - 10f),
        style = TextStyle(color = Color.Black, fontSize = fontSize)
    )

    drawRect(color = Color.Blue, topLeft = Offset(legendX, legendY + with(density) { 40.dp.toPx() }), size = Size(40f, 5f))
    drawText(
        textMeasurer = textMeasurer,
        text = "蓝色：MIUI系统平滑圆角 (Compose)",
        topLeft = Offset(legendX + 50f, legendY + with(density) { 40.dp.toPx() } - 10f),
        style = TextStyle(color = Color.Black, fontSize = fontSize)
    )
}
