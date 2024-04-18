package it.torino.mobin.running.main_activity.panels

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import it.torino.mobin.running.ui_elements.TripSummary
import it.torino.mobin.ui.theme.MediumPadding
import it.torino.mobin.ui.theme.MobinTheme
import it.torino.mobin.utils.InterfaceViewModel
import it.torino.tracker.retrieval.data.TripData
import it.torino.tracker.tracker.sensors.location_recognition.LocationData
import it.torino.tracker.utils.Utils
import it.torino.tracker.view_model.MyViewModel
import android.graphics.Color as AndroidColor


data class TimeSeriesData(val timestamp: Long, val cadence: Int)


@Composable
fun MapViewComposable(myViewModel: MyViewModel, interfaceViewModel: InterfaceViewModel, innerPadding: PaddingValues) {
    val currentIndex = myViewModel.currentTripIndex.observeAsState().value ?: 0
    //    LaunchedEffect(currentIndex) {
    //        Log.d("ComposeDebug", "Recomposing with currentIndex: $currentIndex")
    //    }
    val showMarkers by interfaceViewModel.showMarkers.collectAsState()
    val currentTrip = myViewModel.tripsList?.value?.getOrNull(myViewModel.currentTripIndex.value ?: 0)
    val locationsX = currentTrip?.locations ?: emptyList()
    ConstraintLayout(modifier = Modifier
        .padding(innerPadding)
        .fillMaxSize()) {
        val (element, box, graph) = createRefs()
        Box(
            modifier = Modifier
                .constrainAs(element) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    height = Dimension.percent(0.15f)
                }
        ) {
            val trip: TripData? = myViewModel.tripsList?.value?.get(currentIndex)
            trip.let{
                TripSummary(trip!!)
            }
        }
        Box(
            modifier = Modifier
                .padding(MediumPadding)
                .constrainAs(box) {
                    top.linkTo(element.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    height = Dimension.percent(0.60f)
                }
        ) {
            DrawMap(locationsX, showMarkers)
            LaunchedEffect(Unit) {
                // action performed when teh composable is launched
                interfaceViewModel.setShowMarkers(true)
            }
            if (myViewModel.currentTripIndex.value!! > 1)
                ArrowInCircle(
                    Icons.Filled.ArrowBack,
                    modifier= Modifier.align(Alignment.BottomStart)
                        .padding(MediumPadding)
                ) {
                    myViewModel.setCurrentTripIndex(currentIndex - 1)
                }
            if (myViewModel.currentTripIndex.value!! < myViewModel.tripsList!!.value!!.size - 1)
                ArrowInCircle(
                    Icons.Filled.ArrowForward,
                    modifier= Modifier.align(Alignment.BottomEnd)
                        .padding(MediumPadding)
                ) {
                    myViewModel.setCurrentTripIndex(currentIndex!! + 1)
                }
        }

        if (currentTrip?.activityType != DetectedActivity.IN_VEHICLE) {
            TimeSeriesBarChart(
                data = getTimeSeriesData(myViewModel, currentIndex),
                modifier = Modifier
                    .padding(MediumPadding)
                    .constrainAs(graph) {
                        top.linkTo(box.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                        // This box takes 80% of the parent's height
                        height = Dimension.percent(0.20f)
                    }
            )
        }
    }
}


/**
 * creates a time series of teh cadences of the trip
 */
fun getTimeSeriesData(viewModel: MyViewModel, currentIndex: Int): List<TimeSeriesData> {
    val trip = viewModel.tripsList!!.value?.getOrNull(currentIndex)
    val chart = trip?.chart ?: return emptyList()
    return chart.subList(trip.startTime, trip.endTime).map { TimeSeriesData(it.timeInMSecs, it.cadence) }
}

@Composable
fun DrawMap(locationsX: List<LocationData>?, showMarkers: Boolean) {
    AndroidView(
        modifier = Modifier.fillMaxSize(), // This should make the AndroidView fill the Box

        factory = { context ->
            locationsX.let {
                MapView(context).apply {
                    // Initialize the MapView
                    onCreate(null)
                    onResume()
                    getMapAsync { googleMap ->
                        // Customize the map appearance and behavior here
                        googleMap.uiSettings.isZoomControlsEnabled = false

                    }

                }
            }
        }, update = { mapView ->
            if (showMarkers) {
                mapView.getMapAsync { googleMap ->
                    // Clear the map to remove old markers if they are no longer relevant
                    googleMap.clear()

                    val builder = LatLngBounds.Builder()
                    val pattern = listOf(Dash(15f), Gap(10f))

                    var prevMarker: LatLng? = null
                    // Add markers for each location in the list
                    locationsX!!.forEach { location ->
                        val latLng = LatLng(location.latitude, location.longitude)
//                                googleMap.addMarker(
//                                    MarkerOptions()
//                                        .position(latLng)
//                                        .title("Marker at ${location.latitude}, ${location.longitude}")
//                                )
                        val circleOptions = CircleOptions()
                            .center(latLng)
                            .strokeColor(AndroidColor.BLUE) // Using Android's Color class
                            .fillColor(AndroidColor.BLUE)  // Using Android's Color class
                            // the radius is in meters
                            .radius(2.0)

                        // Add the circle to your Google Map instance
                        googleMap.addCircle(circleOptions)
                        if (prevMarker != null) {
                            googleMap.addPolyline(
                                PolylineOptions()
                                    .add(prevMarker, latLng)
                                    .width(5f)
                                    .pattern(pattern)
                                    .color(AndroidColor.BLUE)
                            )
                        }
                        prevMarker = latLng
                        builder.include(latLng) // Include location in bounds
                    }

                    // Optionally, move the camera to the first location
                    if (locationsX.isNotEmpty()) {
                        val bounds = builder.build()
                        val padding =
                            100
//                    100 + innerPadding.calculateTopPadding().value.toInt() // Offset from edges of the map in pixels
                        // Animate the camera to show all markers within the bounds, including padding.
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(
                            bounds,
                            padding
                        ),
                            object : GoogleMap.CancelableCallback {
                                override fun onFinish() {
                                    // This method is called when the camera has finished moving
                                    if (googleMap.cameraPosition.zoom > 18f) {
                                        // If zoom is greater than 14, adjust to 13
                                        googleMap.animateCamera(
                                            CameraUpdateFactory.zoomTo(
                                                18f
                                            )
                                        )
                                    }
                                }

                                override fun onCancel() {
                                    // Called if the animateCamera call is canceled for some reason
                                }
                            })
                    }
                }
            }
        }
    )
}

fun centerMapOnLocation(map: GoogleMap, latitude: Double, longitude: Double, zoomLevel: Float = 14f) {
    val location = LatLng(latitude, longitude)
    val cameraPosition = CameraPosition.Builder()
        .target(location)      // Sets the center of the map to location
        .zoom(zoomLevel)       // Sets the zoom
        .build()               // Creates a CameraPosition from the builder
    map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
}
@Composable
fun TimeSeriesBarChart(data: List<TimeSeriesData>, modifier: Modifier = Modifier) {
    if (data.isEmpty()) return // Handle empty data gracefully

    // Drawing configuration
    val axisStrokeWidth = 4f
    val axisColor = Color.Black
    val labelColor = Color.Black

    val maxValue = data.maxOf { it.cadence }.toFloat()
    val minValue = data.minOf { it.cadence }.toFloat()
    val timeSpan = (data.last().timestamp - data.first().timestamp).toFloat()

    Canvas(modifier = modifier.fillMaxSize()) {
        val labelPadding = 4.dp.toPx() // Space between labels and axes
        val labelTextSize = 8.dp.toPx() // Size of the text for labels
        val yAxisLabelWidth = 30.dp.toPx() // Estimated width to accommodate Y-axis labels

        val chartHeight = size.height * 0.8f // Allocate 80% height for the chart
        val chartWidth = size.width - yAxisLabelWidth // Adjust chart width to account for Y-axis labels
        val barWidth = chartWidth / data.size
        // Offset drawing start position to the right to accommodate Y-axis labels


        // Draw bars with offset
        data.forEachIndexed { _, _ ->
            val padding = 4f // Define the padding size
            data.forEachIndexed { index, item ->
                // Adjust the starting X position and width for padding
                val adjustedBarWidth = barWidth - 2 * padding // Subtract padding from both sides
                val left =
                    yAxisLabelWidth + index * barWidth + padding // Start from original left plus padding
                val normalizedValue = (item.cadence - minValue) / (maxValue - minValue)
                val barHeight = chartHeight * normalizedValue
                val top = chartHeight - barHeight

                drawRect(
                    color = Color.Blue,
                    topLeft = Offset(left, top),
                    size = Size(adjustedBarWidth, barHeight) // Use the adjusted width
                )
            }
        }
        // Draw a line at a specific Y-value (e.g., 100)
        val targetValue = 100f
        if (targetValue in minValue..maxValue) {
            val lineYPosition = size.height * (1 - (targetValue - minValue) / (maxValue - minValue).toFloat())
            drawLine(
                color = Color.Red,
                start = Offset(yAxisLabelWidth, lineYPosition),
                end = Offset(size.width, lineYPosition),
                strokeWidth = 2.dp.toPx()
            )
        }
        // X Axis and Labels
        val xAxisLabels = 5
        val interval = data.size / (xAxisLabels - 1)
        for (i in 0 until xAxisLabels) {
            val index = i * interval
            if (index < data.size) {
                val xPos = index * barWidth
                drawContext.canvas.nativeCanvas.drawText(
                    Utils.millisecondsToString(data[index].timestamp, "HH:mm:ss").orEmpty(),
                    xPos + yAxisLabelWidth,
                    chartHeight + labelPadding + labelTextSize,
                    android.graphics.Paint().apply {
                        textSize = labelTextSize
                        color = android.graphics.Color.BLACK
                    }
                )
            }
        }
        // Y Axis and Labels
        // Ensure Y-axis and its labels are drawn within the left margin (startX)
        for (i in 0..5) {
            val yValue = minValue + (maxValue - minValue) / 5 * i
            val yPos = chartHeight - (chartHeight / 5) * i
            drawContext.canvas.nativeCanvas.drawText(
                yValue.toInt().toString(),
                labelPadding,
                yPos + labelTextSize / 2, // Adjust to center text vertically
                android.graphics.Paint().apply {
                    textSize = labelTextSize
                    color = android.graphics.Color.BLACK
                }
            )
        }

        // Draw X and Y axes considering startX offset
        drawLine(
            color = axisColor,
            start = Offset(yAxisLabelWidth, chartHeight),
            end = Offset(size.width, chartHeight),
            strokeWidth = axisStrokeWidth
        )
        drawLine(
            color = axisColor,
            start = Offset(yAxisLabelWidth, 0f),
            end = Offset(yAxisLabelWidth, chartHeight),
            strokeWidth = axisStrokeWidth
        )
    }
}


@Composable
fun ArrowInCircle(image: ImageVector, modifier: Modifier, onclick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(color = MaterialTheme.colorScheme.primary, shape = CircleShape)
            .size(48.dp) // Size of the circle
            .clickable {
                onclick()
            }

    ) {
        Icon(
            imageVector = image,
            contentDescription = "An icon showing an arrow",
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(24.dp) // Size of the icon
        )
    }
}


@Preview
@Composable
private fun PreviewMap() {
    MobinTheme {

        val viewModel = MyViewModel(LocalContext.current)
        val interfaceViewModel = InterfaceViewModel(LocalContext.current)
        val locations: List<LocationData> = listOf(
            LocationData(
                Utils.midnightinMsecs(System.currentTimeMillis() + 1000),
                54.019, -1.17, 10.0, 100.0
            ),
            LocationData(
                Utils.midnightinMsecs(System.currentTimeMillis() + 2000),
                54.020, -1.18, 20.0, 102.0
            ),
            LocationData(
                Utils.midnightinMsecs(System.currentTimeMillis() + 3000),
                54.022, -1.20, 30.0, 108.0
            ),
        )
        MapViewComposable(viewModel, interfaceViewModel, PaddingValues(16.dp))
        viewModel.setRelevantLocations(locations)
    }

}

@Preview
@Composable
private fun ElemPreview() {
    val trip = TripData(1000, 20000, 7, 1000, 2000, 3000, false)
    TripSummary(trip)

}