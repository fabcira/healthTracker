package it.torino.mobin.ui_utils.main_activity

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.DetectedActivity
import it.torino.mobin.ui.theme.MobinTheme
import it.torino.mobin.ui.theme.h2
import it.torino.mobin.ui.theme.h3
import it.torino.mobin.ui.theme.h5
import it.torino.mobin.ui.theme.marginLateralDouble
import it.torino.mobin.ui.theme.marginLateralHalf
import it.torino.mobin.ui.theme.marginVertical
import it.torino.mobin.utils.getIcon
import it.torino.mobin.utils.getName
import it.torino.mobin.R
import it.torino.tracker.retrieval.data.TripData
import it.torino.tracker.tracker.sensors.location_recognition.LocationData
import it.torino.tracker.utils.Utils
import it.torino.tracker.utils.Utils.Companion.millisecondsToString
import it.torino.tracker.view_model.MyViewModel


@Composable
fun TripsScreen(viewModel: MyViewModel, navController: NavController, innerPadding: PaddingValues) {
    Box(modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        ){
        viewModel.tripsList?.value.let{
         TripsList(navController, viewModel, items =  viewModel.tripsList!!.value!!)
        }
    }
}

@Composable
fun TripsList(navController: NavController, viewModel: MyViewModel, items: List<TripData>) {
    LazyColumn (modifier = Modifier
        .padding(vertical= marginVertical)){
        itemsIndexed(items) { index, item ->
            if (item.activityType != DetectedActivity.STILL) {
                TripItemLayout(navController, viewModel, index)
                // Only add the Box (connector/divider) if this is not the last item
                if (index < items.size - 1) {
                    // Connectors or dividers, adjust as needed
                    Box(
                        modifier = Modifier
                            .padding(start = marginLateralDouble)
                            .size(width = marginLateralHalf, height = 24.dp)
                            .background(Color.DarkGray)
                    )
//        Box(
//            modifier = Modifier
//                .padding(start = marginLateralDouble)
//                .size(width = marginLateralHalf, height = 24.dp)
//                .background(Color.DarkGray)
//        )
                }
            }
        }
    }
}

@Composable
fun TripItemLayout(navController: NavController, viewModel: MyViewModel, index:Int) {
    val trip: TripData = viewModel.tripsList!!.value!![index]
    Column(
        modifier = Modifier
            .padding(horizontal = marginLateralHalf)
            .fillMaxWidth()
            .clickable {
                viewModel.setCurrentTripIndex(index)
                navController.navigate("Map")
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(10.dp)) // Clip the Row's corners to be rounded
                .border(
                    border = BorderStroke(
                        2.dp,
                        Color.Black
                    ), // Specify the border stroke and color
                    shape = RoundedCornerShape(10.dp) // Ensure the border shape matches the clip shape
                )
        ) {
            // Assuming BaseImageView is a composable you have for images
            BaseImageView(
                modifier = Modifier
                    .size(68.dp)
                    .weight(3f),
                imageRes = getIcon(trip.activityType)
            )

            Column(
                modifier = Modifier
                    .weight(5f)
            ) {
                BaseTextView(
                    getName(LocalContext.current, trip.activityType),
                    color = Color.Black,
                    size = h2,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                BaseTextView(
                    text = "${millisecondsToString(trip.getStartTimeInMsecs(), "HH:mm")}"
                            +
                            "-${millisecondsToString(trip.getEndTimeInMsecs(), "HH:mm")}",
                    color = Color.Black,
                    size = h3,
                    fontWeight = FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(4.dp))
                BaseTextView(
                    text = "${trip.steps} steps, (${trip.distanceInMeters}.m/s)",
                    color = Color.Black,
                    size = h5,
                    fontWeight = FontWeight.Normal,
//                    visibility = Visibility.GONE // Handle visibility accordingly in your composable
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primary)
                    .weight(1.6f)
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_map),
                    contentDescription = "map icon"
                )
            }
        }

    }
}

//
//@Preview(showBackground = true)
//@Composable
//fun PreviewYourLayout() {
//    YourLayout()
//}

@Composable
fun BaseTextView(
    text: String,
    color: Color = Color.Black,
    size: TextUnit = h3,
    fontWeight: FontWeight = FontWeight.Normal
) {
    Text(
        text = text,
        color = color,
        fontSize = size,
        fontWeight = fontWeight
    )
}

@Composable
fun BaseImageView(
    imageRes: Int,
    contentDescription: String? = null, // Adding content description for accessibility
    modifier: Modifier = Modifier // Allows passing in size, padding, etc., from the call site
) {
    Image(
        painter = painterResource(id = imageRes),
        contentDescription = contentDescription,
        modifier = modifier
    )
}
@Preview(showBackground = true)
@Composable
fun PreviewMyList() {
    MobinTheme {
        val viewModel= MyViewModel(LocalContext.current)
        val navController = rememberNavController()
        TripsList(
            navController,
            viewModel,
            items = listOf(
                TripData(1000, 5000, 0, mutableListOf()),
                TripData(5000, 10000, 7, mutableListOf()),
                TripData(10000, 20000, 0, mutableListOf())
            )
        )
    }
}

