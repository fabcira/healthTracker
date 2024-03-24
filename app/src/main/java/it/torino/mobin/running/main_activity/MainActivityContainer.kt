package it.torino.mobin.running.main_activity

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import it.torino.mobin.MainActivity
import it.torino.mobin.ui.theme.LargePadding
import it.torino.mobin.R
import it.torino.mobin.running.main_activity.panels.HealthPanel
import it.torino.mobin.running.main_activity.panels.HomePanel
import it.torino.mobin.running.main_activity.panels.MapViewComposable
import it.torino.mobin.running.main_activity.panels.TripsScreen
import it.torino.tracker.view_model.MyViewModel
import java.util.Calendar

@Composable
fun MainContainer(
    activity: MainActivity,
    viewModel: MyViewModel
) {
    val navController: NavHostController = rememberNavController()

    val navigationBarHeight = 88.dp
    val selectedIndex by remember { mutableStateOf(1) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(onDestinationClicked = {
                print("aaa")
            })
        }) {
        Scaffold(
            topBar = {
                TopTitleBar(viewModel)
            },
            bottomBar = {
                BottomIconBar(navController = navController, navigationBarHeight,
                    selectedIndexState = remember { mutableStateOf(selectedIndex) })
            }
        ) { innerPadding ->
            NavHost(navController = navController, startDestination = "Home") {
                composable("Home") {
                    HomePanel(viewModel, innerPadding)
                }
                composable("Trips") { TripsScreen(viewModel, navController, innerPadding) }
                composable("Map") {
                    MapViewComposable(viewModel, innerPadding)
                }

                composable("Health") { HealthPanel(innerPadding) }
            }
            LifeCycleAwareResultComputation(viewModel, navController, "Home")
        }
    }
}

@Composable
fun LifeCycleAwareResultComputation(myViewModel: MyViewModel, navController: NavHostController, startDestination: String) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                myViewModel.onResume(context)
                myViewModel.computeResults()
                // Navigate to "Home" only if not already there to avoid loop or unnecessary navigation
                if (navController.currentDestination?.route != startDestination) {
                    navController.navigate(startDestination) {
                        popUpTo(startDestination) { inclusive = true } // Pop everything up to "Home"
                        launchSingleTop = true // Avoid multiple instances if "Home" is already the top
                    }
                }
            }
        }
        // Adding the observer to the lifecycle
        lifecycleOwner.lifecycle.addObserver(observer)

        // Removing the observer when the composable is disposed
        onDispose {
            myViewModel.onPause()
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    // React to status changes
    LaunchedEffect(myViewModel.currentDateTime.value) {
        myViewModel.computeResults()
    }
}

@Composable
fun BottomIconBar(navController: NavHostController, navigationBarHeight: Dp,
                  selectedIndexState: MutableState<Int> ) {
    val modifier = Modifier
    Box(
        modifier
            .fillMaxWidth()
    ) {
        EvenlyDistributedNavigationBar(
            modifier,
            navigationBarHeight,
            selectedIndex = selectedIndexState.value
        ) { navigationTarget, index ->
            selectedIndexState.value = index // Update the selected index
            // Navigate to the screen
            navController.navigate(navigationTarget) {
                popUpTo(navController.graph.startDestinationId)
                launchSingleTop = true
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopTitleBar(viewModel: MyViewModel) {
    var selectedDate by remember { mutableStateOf("") }
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    TopAppBar(
        title = {
            Text(
                stringResource(id = R.string.app_title),
                style = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.Center),
                modifier = Modifier
                    .fillMaxWidth()
            )
        },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            titleContentColor = MaterialTheme.colorScheme.primary
        ),
        actions = {
            // Menu icon button
            IconButton(onClick = { showMenu = !showMenu }) {
                Icon(Icons.Filled.Menu, "menu", tint = MaterialTheme.colorScheme.onBackground)
            }
            // Dropdown menu
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("History") },
                    onClick = {
                        showMenu = false
                        showDatePicker(context) { year, month, dayOfMonth ->
                            // Construct a Calendar object and set it to the selected date
                            val calendar = Calendar.getInstance().apply {
                                set(year, month, dayOfMonth)
                            }
                            // Convert the selected date to milliseconds
                            val dateInMillis = calendar.timeInMillis
                            // Call the ViewModel's function with the selected date in milliseconds
                            viewModel.setCurrentDateTime(dateInMillis)

                            // Optionally update the UI
                            selectedDate = "$dayOfMonth/${month + 1}/$year"
                        }
                    }
                )
                DropdownMenuItem(
                    text = { Text("Settings") },
                    onClick = {
                        showMenu = false
                        // Handle "Settings" action
                    }
                )
            }
        }
    )
}




private data class NavigationButtonInfo(val iconSelected: Int, val iconNotSelected: Int,
                                        val height: Dp, val navigationDestination: String)

@Composable
fun EvenlyDistributedNavigationBar(
    modifier: Modifier,
    navigationBarHeight: Dp,
    selectedIndex: Int,
    onItemSelected: (String, Int) -> Unit
) {
    val ratio = 0.7f
    val halfNavigationBarHeight = (navigationBarHeight.value * ratio).dp
    val items = listOf(
        NavigationButtonInfo(
            iconSelected = R.drawable.bottom_nav_trips_selected,
            iconNotSelected = R.drawable.bottom_nav_trips_idle,
            height = halfNavigationBarHeight,
            navigationDestination = "trips"
        ),
        NavigationButtonInfo(
            iconSelected =  R.drawable.bottom_nav_main_selected,
            iconNotSelected = R.drawable.bottom_nav_main_idle,
            height = navigationBarHeight,
            navigationDestination = "home"
        ),
        NavigationButtonInfo(
            iconSelected = R.drawable.bottom_nav_health_selected,
            iconNotSelected = R.drawable.bottom_nav_health_idle,
            height = halfNavigationBarHeight,
            navigationDestination = "health"
        )
    )

    Box(
        modifier = modifier
            .height(navigationBarHeight)
            .fillMaxWidth()

    ) {
        Box(
            modifier = modifier
                .fillMaxHeight(ratio)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .align(Alignment.BottomCenter)
        )
        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(start = LargePadding, end = LargePadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            items.forEachIndexed { index, triple ->
                // This composable represents each item
                CustomFABInBottomBar(
                    modifier,
                    bottomBarHeight = triple.height,
                    isSelected = selectedIndex == index,
                    iconSelectedResId = triple.iconSelected,
                    iconNotSelectedResId = triple.iconNotSelected,
                    onItemSelected = { onItemSelected(triple.navigationDestination, index) }
                )
            }
        }


    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomFABInBottomBar(
    modifier: Modifier,
    bottomBarHeight: Dp = 56.dp,
    isSelected: Boolean,
    iconSelectedResId: Int,
    iconNotSelectedResId: Int,
    onItemSelected: () -> Unit
) {
    val iconResId = if (isSelected) iconSelectedResId else iconNotSelectedResId
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(height = bottomBarHeight, width = bottomBarHeight)
    ) {
        // Custom FAB
        Surface(
            modifier = modifier
                .align(Alignment.Center), // Center it within the Box (which is as tall as the bottom bar)
            shape = CircleShape,
            color = Color.Transparent, // FAB background color
            shadowElevation = 8.dp, // Elevation to mimic the FAB shadow
            onClick = onItemSelected,
        ) {
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = "Custom FAB",
                modifier = modifier.align(Alignment.Center),
                contentScale = ContentScale.Fit,

                )
        }
    }
}



@Composable
fun DrawerContent(onDestinationClicked: (String) -> Unit) {
    Column {
//        DrawerItem(icon = Icons.Filled.History, label = "History", onItemClick = { onDestinationClicked("History") })
        DrawerItem(icon = Icons.Filled.Settings, label = "Settings", onItemClick = { onDestinationClicked("Settings") })
    }
}

@Composable
fun DrawerItem(icon: ImageVector, label: String, onItemClick: () -> Unit) {
    TextButton(onClick = onItemClick) {
        Icon(icon, contentDescription = null)
        Text(text = label, modifier = androidx.compose.ui.Modifier.padding(start = 8.dp))
    }
}


fun showDatePicker(context: android.content.Context, onDateSelected: (year: Int, month: Int, dayOfMonth: Int) -> Unit) {
    // Get the current date
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    // Create and show the date picker dialog
    val datePickerDialog = DatePickerDialog(context, { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
        onDateSelected(year, month, dayOfMonth)
    }, year, month, day)

    datePickerDialog.show()
}
@Preview(showBackground = true)
@Composable
fun ScaffoldWithFABInBottomBarM3Preview() {
//    EvenlyDistributedNavigationBar(modifier = Modifier, 32.dp, 2,
//        onItemSelected = { index ->
//
//        })
//    ScaffoldWithFABInBottomBarM3( 0.65f)
}
