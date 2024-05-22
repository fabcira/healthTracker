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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.MutableLiveData
import it.torino.mobin.R
import it.torino.mobin.ui.theme.DoubleSpacerHeight
import it.torino.mobin.ui.theme.White
import it.torino.mobin.running.ui_elements.StepsProgressDial
import it.torino.mobin.ui.theme.SpacerHeight
import it.torino.tracker.view_model.MyViewModel

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import it.torino.tracker.tracker.TrackerService
import it.torino.tracker.utils.Globals
import java.io.File

const val heartMinutesTarget = 40
const val walkingMinutesTarget = 100
const val stepsTarget = 10000

@Composable
fun HomePanel(
    trackerViewModel: MyViewModel,
    innerPadding: PaddingValues
) {
    val context = LocalContext.current
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
        val isActive by trackerViewModel.isActive.observeAsState()

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


                Box(modifier = Modifier.size(250.dp)
                    .clickable {
                        if (isActive != false) {
                            val body =
                                "${context.getString(R.string.email_subject)} for ${TrackerService.timeStamp}"
                            trackerViewModel.stopTracker()
                            val fileList = getSensorFiles(context)
                            if (fileList.size>0) {
                                sendEmailWithAttachment(
                                    context,
                                    "fabio.ciravegna@unito.it",
                                    context.getString(R.string.email_body),
                                    body,
                                    fileList
                                )
                            }
                        } else {
                            trackerViewModel.restartTracker()
                        }
                    }
                ) {
                    StepsProgressDial(
                        MaterialTheme.colorScheme.secondary,
                        MaterialTheme.colorScheme.tertiary,
                        MaterialTheme.colorScheme.primary,
                        walkingMinutes / walkingMinutesTarget,
                        context.getString(
                            if (isActive == true)
                                R.string.stop_tracker
                            else
                                R.string.start_tracker
                        )
                    )
                }

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

//                Button(
//                    onClick = {
//                        if (isActive){
//                            val body = "${context.getString(R.string.email_subject)} for ${TrackerService.timeStamp}"
//                            trackerViewModel.stopTracker()
//                            sendEmailWithAttachment(context,
//                                "fabio.ciravegna@unito.it",
//                                context.getString(R.string.email_body),
//                                body,
//                                getSensorFiles(context))
//                        } else {
//                            trackerViewModel.restartTracker()
//                        }
//                    },
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    Text(context.getString(
//                        if (isActive)
//                            R.string.stop_tracker
//                        else
//                            R.string.start_tracker))
//                }
            }
        }
    }
}

fun getSensorFiles(context: Context): List<File> {
    val timeStamp = TrackerService.timeStamp
    val finList : MutableList<File> = mutableListOf()
    val fileAcc = File(context.getExternalFilesDir(null), "${Globals.FILE_ACCELEROMETER}${timeStamp}.csv")
    if (fileAcc.exists()){
        finList.add(fileAcc)
    }
    val fileGyro =  File(context.getExternalFilesDir(null), "${Globals.FILE_GYRO}${timeStamp}.csv")
    if (fileGyro.exists()){
        finList.add(fileGyro)
    }
    val fileMagn =  File(context.getExternalFilesDir(null), "${Globals.FILE_MAGNETOMETER}${timeStamp}.csv")
    if(fileMagn.exists()){
        finList.add(fileMagn)
    }
    return finList
}


fun sendEmailWithAttachment(context: Context, email: String, subject: String, body: String,
                            files: List<File>) {
    val uris = ArrayList<android.net.Uri>()
    for (file in files) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        uris.add(uri)
    }

    val emailIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
        type = "vnd.android.cursor.dir/email"
        putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body as CharSequence)  // otherwise it returns an error for some reasons
        putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(emailIntent, "Send email..."))
}

@Preview(showBackground = true)
@Composable
fun MainDialPanelPreview() {
//    val trackerViewModel= TrackerViewModel()
//    MainDialPanel(trackerViewModel, PaddingValues(MediumPadding), 0.65f, "progress 0.65")
}
