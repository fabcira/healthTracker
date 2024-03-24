package it.torino.mobin.running.main_activity.panels

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.DetectedActivity
import it.torino.mobin.running.ui_elements.TripSummary
import it.torino.mobin.ui.theme.MobinTheme
import it.torino.mobin.ui.theme.marginLateralDouble
import it.torino.mobin.ui.theme.marginLateralHalf
import it.torino.mobin.ui.theme.marginLateralHalfHalf
import it.torino.mobin.ui.theme.marginVertical
import it.torino.tracker.retrieval.data.TripData
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
                            .size(width = marginLateralHalfHalf, height = 24.dp)
                            .background(Color.DarkGray)
                    )
                }
            }
        }
    }
}

@Composable
fun TripItemLayout(navController: NavController, viewModel: MyViewModel, index:Int) {
    val trip: TripData? = viewModel.tripsList?.value?.get(index)
    trip.let {
        Box(
            modifier = Modifier
                .padding(horizontal = marginLateralHalf)
                .fillMaxWidth()
                .height(80.dp)
                .clickable {
                    viewModel.setCurrentTripIndex(index)
                    navController.navigate("Map")
                }
        ) {
            TripSummary(trip = trip!!)
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

