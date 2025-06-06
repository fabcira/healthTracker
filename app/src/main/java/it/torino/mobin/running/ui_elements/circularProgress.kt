package it.torino.mobin.running.ui_elements

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Color.LightGray
 */
@Composable
fun StepsProgressDial(circleColour: Color, excessArcColour: Color, arcColour: Color, progress: Float,
                      writing: String) {

    Canvas(modifier = Modifier.size(250.dp)) {
        val strokeWidth = 80f
        val radius = (size.minDimension / 2.2).toFloat() - (strokeWidth / 2.2).toFloat()
        val center = Offset(x = size.width / 2, y = size.height / 2)

        // Draw base circle
        drawCircle(
            color = circleColour,
            center = center,
            radius = radius,
            style = Stroke(width = strokeWidth)
        )

        val sweepAngle = 360 * progress

        // Draw progress arc
        drawArc(
            color = arcColour,
            startAngle = -90f,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(width = strokeWidth),
            topLeft = Offset(center.x - radius, center.y - radius),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
        )

        // Draw excess arc - just whatever is above 360 degrees
        if (sweepAngle > 360) {
            drawArc(
                color = excessArcColour,
                startAngle = -90f,
                sweepAngle = sweepAngle - 360,
                useCenter = false,
                style = Stroke(width = strokeWidth),
                topLeft = Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
            )
        }

        // Draw the text in the center
        drawContext.canvas.nativeCanvas.apply {
            val textSize = 20.sp.toPx()
            val paint = android.graphics.Paint().apply {
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
                this.textSize = textSize
                color = android.graphics.Color.BLACK
            }
            drawText(
                writing,
                center.x,
                center.y + textSize / 4, // Adjust to center vertically
                paint
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun myPreview() {
    StepsProgressDial(Color.LightGray, Color.White, Color.Blue, 0.6f, "start")
}
