package it.torino.mobin.running.ui_elements

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import it.torino.mobin.running.main_activity.panels.BaseImageView
import it.torino.mobin.ui.theme.MediumPadding
import it.torino.mobin.ui.theme.h4
import it.torino.mobin.ui.theme.h5
import it.torino.mobin.ui.theme.marginLateralHalf
import it.torino.mobin.utils.getIcon
import it.torino.mobin.utils.getName
import it.torino.tracker.retrieval.data.TripData
import it.torino.tracker.utils.Utils
import kotlin.math.roundToInt


@Composable
fun TripSummary(trip: TripData) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = marginLateralHalf)
            .fillMaxSize()
            .clip(RoundedCornerShape(10.dp)) // Clip the Row's corners to be rounded
            .border(
                border = BorderStroke(
                    2.dp,
                    Color.Black
                ), // Specify the border stroke and color
                shape = RoundedCornerShape(10.dp) // Ensure the border shape matches the clip shape
            )
    ) {
        BaseImageView(
            modifier = Modifier.weight(1f),
            imageRes = getIcon(trip.activityType)
        )
        Column(
            modifier = Modifier.fillMaxHeight()
                .weight(3f)
                .padding(start= MediumPadding),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${Utils.millisecondsToString(trip.getStartTimeInMsecs(), "HH:mm")} "
                        +
                        "- ${Utils.millisecondsToString(trip.getEndTimeInMsecs(), "HH:mm")}",
                color = Color.Black,
                fontSize = h5,
                fontWeight = FontWeight.Normal,
            )
            Text(
                getName(LocalContext.current, trip.activityType),
                color = Color.Black,
                fontSize = h4,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
        Column(
            modifier = Modifier.fillMaxHeight()
                .weight(2f),
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "${trip.steps} steps",
                color = Color.Black,
                fontSize = h5,
                fontWeight = FontWeight.Normal
            )
            var speedValue = trip.distanceInMeters.toFloat() / (trip.getDuration(trip.chart)/1000f)
            speedValue = (speedValue * 10).roundToInt() / 10.0f
            Text(
                text = "$speedValue m/s",
                color = Color.Black,
                fontSize = h5,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
