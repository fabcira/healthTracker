package it.torino.mobin.ui.drawable

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun BottomNavTripsSelected(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(79.dp, 60.dp)) {
        val path = Path().apply {
            // Main path data
            moveTo(29.189f, 50.75f)
            cubicTo(29.116f, 50.75f, 29.053f, 50.724f, 29.002f, 50.673f)
            cubicTo(28.951f, 50.622f, 28.925f, 50.559f, 28.925f, 50.486f)
            lineTo(28.925f, 44.403f)
            lineTo(26.89f, 44.403f)
            cubicTo(26.817f, 44.403f, 26.754f, 44.377f, 26.703f, 44.326f)
            cubicTo(26.652f, 44.275f, 26.626f, 44.212f, 26.626f, 44.139f)
            lineTo(26.626f, 43.325f)
            cubicTo(26.626f, 43.244f, 26.652f, 43.178f, 26.703f, 43.127f)
            cubicTo(26.754f, 43.076f, 26.817f, 43.05f, 26.89f, 43.05f)
            lineTo(32.5f, 43.05f)
            // Add the rest of the path data here
        }

        val clipPath = Path().apply {
            // Define clip path data if needed
        }

        clipPath(clipPath) {
            drawPath(
                path = path,
                color = color
            )
        }
    }
}


@Preview
@Composable
fun CustomVectorDrawablePreview() {
    MaterialTheme {
        BottomNavTripsSelected(MaterialTheme.colorScheme.primary)
    }
}