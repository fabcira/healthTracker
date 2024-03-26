package it.torino.mobin.running.main_activity.panels

import CustomProgressBarWithIcon
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import it.torino.mobin.R
import it.torino.mobin.ui.theme.DoubleSpacerHeight
import it.torino.mobin.ui.theme.White
import it.torino.mobin.running.ui_elements.StepsProgressDial
import it.torino.mobin.ui.theme.SpacerHeight
import it.torino.tracker.view_model.MyViewModel

const val heartMinutesTarget = 40
const val walkingMinutesTarget = 100
const val stepsTarget = 10000

@Composable
fun HomePanel(
    trackerViewModel: MyViewModel,
    innerPadding: PaddingValues
) {
    // Using MaterialTheme to adhere to MD3 guidelines
    val cardColors = CardDefaults.cardColors(
        containerColor = Color.Transparent // Setting the card background to transparent
    )
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .background(White),
        colors = cardColors
    ) {
        val mobilityChart = trackerViewModel.mobilityChart?.observeAsState(null)

        // Using Box to center content within the Card
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                val heartMinutes =
                    (mobilityChart?.value?.summaryData?.heartActivityMSecs?.div(1000)?.div(60)
                        ?: 0).toFloat()
                val walkingMinutes = (
                        (mobilityChart?.value?.summaryData?.walkingMsecs ?: 0) +
                                (mobilityChart?.value?.summaryData?.runningMsecs ?: 0)
                        ).div(1000).div(60).toFloat()
                val numberOfSteps = (mobilityChart?.value?.summaryData?.steps ?: 0)


                StepsProgressDial(
                    MaterialTheme.colorScheme.secondary,
                    MaterialTheme.colorScheme.tertiary,
                    MaterialTheme.colorScheme.primary,
                    walkingMinutes/ walkingMinutesTarget
                )
                Text(
                    text = "${walkingMinutes.toInt()} ${LocalContext.current.getString(R.string.move_minutes)}\n$numberOfSteps ${LocalContext.current.getString(R.string.steps)}",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier
                    .size(DoubleSpacerHeight)
                    .fillMaxWidth())
                CustomProgressBarWithIcon(MaterialTheme.colorScheme.primary, heartMinutes/ heartMinutesTarget)
                Text(
                    text = "${heartMinutes.toInt()} ${LocalContext.current.getString(R.string.heart_minutes)}",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
                    modifier = Modifier.fillMaxWidth().padding(top= SpacerHeight)
                )

            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun MainDialPanelPreview() {
//    val trackerViewModel= TrackerViewModel()
//    MainDialPanel(trackerViewModel, PaddingValues(MediumPadding), 0.65f, "progress 0.65")
}
