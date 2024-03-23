import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun CustomProgressBarWithIcon(color: Color, progress: Float) { // progress is a value between 0f and 1f
    val progressBarHeight = 32.dp
    val progressBarWidth = 200.dp
    val iconSize = 24.dp

    Box(modifier = Modifier.size(width = progressBarWidth, height = progressBarHeight)) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val cornerRadius = CornerRadius(x = progressBarHeight.toPx() / 2, y = progressBarHeight.toPx() / 2)

            // Background of the progress bar
            drawRoundRect(
                color = Color.LightGray,
                size = size,
                cornerRadius = cornerRadius
            )

            // Foreground of the progress bar (actual progress)
            drawRoundRect(
                color = color,
                topLeft = Offset.Zero,
                size = size.copy(width = if (progress<1)  size.width * progress else size.width),
                cornerRadius = cornerRadius
            )
        }

        // Icon indicating current progress level
        Icon(
            imageVector = Icons.Filled.Favorite,
            contentDescription = "Heart",
            tint = Color.White, // Consider the icon color that contrasts well with the progress color
            modifier = Modifier
                .size(iconSize)
                // Calculate the offset to keep the icon inside the progress bar, considering its size
                .align(alignment = androidx.compose.ui.Alignment.CenterStart)
                .offset(x =
                if (progress<1) ((progressBarWidth - iconSize) * progress - iconSize / 2)
                    .coerceAtLeast(0.dp).coerceAtMost((progressBarWidth - iconSize) * progress)
                else progressBarWidth - iconSize )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCustomProgressBarWithIcon() {
    Box(modifier = Modifier.background(Color.Gray), contentAlignment = androidx.compose.ui.Alignment.Center) {
        CustomProgressBarWithIcon(color = MaterialTheme.colorScheme.primary, progress = 0.5f) // Example progress
    }
}
