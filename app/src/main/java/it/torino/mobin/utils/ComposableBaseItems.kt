package it.torino.mobin.utils

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import it.torino.mobin.ui.theme.MediumPadding
import it.torino.mobin.ui.theme.SmallPadding

/**
 * MaterialTheme.colorScheme.primary
 * MaterialTheme.colorScheme.onPrimary
 *
 */
@Composable
fun CustomButton(
    modifier: Modifier,
    text: String,
    containerColour: Color,
    contentColour: Color,
    consecutiveButtons: Boolean = false,
    onButtonClick: () -> Unit
) {
    Button(
        modifier = modifier.fillMaxWidth().padding(if (consecutiveButtons) SmallPadding else MediumPadding),
        onClick = { onButtonClick() },
        shape = RoundedCornerShape(8.dp), // Adjust the corner size as needed
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColour, // Background color
            contentColor = contentColour // Text color
        )
    ) {
        Text(
            text = text,
            color = contentColour
        )
    }
}

// Example usage of CustomButton
@Composable
fun YourComposableScreen() {
//    CustomButton(onButtonClick = {
//        // Define what happens when the button is clicked
//        println("Button was clicked")
//    })
}
