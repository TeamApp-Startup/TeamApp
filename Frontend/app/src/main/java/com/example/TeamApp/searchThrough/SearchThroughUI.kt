package com.example.TeamApp.searchThrough
import android.util.Log
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.TeamApp.R
import com.example.TeamApp.excludedUI.ActivityCard
import com.example.TeamApp.event.CreateEventViewModel
import com.example.TeamApp.event.CreateEventViewModelProvider
import kotlinx.coroutines.delay



@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SearchScreen(navController: NavController, onScroll: (isScrollingDown: Boolean) -> Unit) {
    val viewModel: CreateEventViewModel = CreateEventViewModelProvider.createEventViewModel
    val SearchViewModel: SearchThroughViewModel = SearchViewModelProvider.searchThroughViewModel
    var showEmptyMessage by remember { mutableStateOf(false) }
    val scrollState = rememberLazyListState()
    val activityList = remember { viewModel.activityList }
    val newlyCreatedEvent = viewModel.newlyCreatedEvent
    val context = LocalContext.current
    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(isRefreshing, onRefresh = {
        isRefreshing = true
    })
    val filtersOn by SearchViewModel.filtersOn.observeAsState(false)

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            viewModel.isDataFetched = false

            Log.d("SearchScreen", "isRefreshing = $isRefreshing, filtersOn = $filtersOn")
            if (filtersOn) {

                val selectedSports = SearchViewModel.selectedSports.value ?: listOf()
                Log.d("SearchScreen", "Fetching filtered events with sports: $selectedSports")
                viewModel.fetchFilteredEvents(selectedSports)
            } else {

                Log.d("SearchScreen", "Fetching all events")
                viewModel.fetchEvents()
            }
            delay(300)
            isRefreshing = false
        }
    }

    LaunchedEffect(scrollState, newlyCreatedEvent) {
        if (newlyCreatedEvent != null) {
            val index = activityList.indexOf(newlyCreatedEvent)
            if (index >= 0) {
                scrollState.scrollToItem(0)
                delay(200)
                val averageItemSize = scrollState.layoutInfo.visibleItemsInfo
                    .firstOrNull()?.size ?: 0
                val currentOffset = scrollState.firstVisibleItemIndex * averageItemSize + scrollState.firstVisibleItemScrollOffset
                val targetOffset = index * averageItemSize
                val distance = targetOffset - currentOffset
                scrollState.animateScrollBy(distance.toFloat(), animationSpec = tween(durationMillis = 500))
                delay(500)
                viewModel.clearNewlyCreatedEvent()
            }
        }
    }

    LaunchedEffect(Unit) {
        delay(1000)
        if (activityList.isEmpty()) {
            showEmptyMessage = activityList.isEmpty()
        }
    }

    val gradientColors = listOf(
        Color(0xFFE8E8E8),
        Color(0xFF007BFF),
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.linearGradient(colors = gradientColors))
            .padding(horizontal = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center
                ) {
                    FilterButton(
                        navController = navController,
                        modifier = Modifier
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center
                ) {
                    CurrentCity(
                        value = "WARSZAWA",
                        modifier = Modifier
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pullRefresh(pullRefreshState),
                    state = scrollState
                ) {
                    when {
                        showEmptyMessage -> {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    NoCurrentActivitiesBar()
                                }
                            }
                        }
                        activityList.isNotEmpty() -> {
                            items(activityList) { activity ->
                                val isNewlyCreated = activity == newlyCreatedEvent
                                ActivityCard(
                                    pinIconResId = activity.pinIconResId,
                                    iconResId = activity.iconResId,
                                    date = activity.date,
                                    activityName = activity.activityName,
                                    currentParticipants = activity.currentParticipants,
                                    maxParticipants = activity.maxParticipants,
                                    location = activity.location,
                                    isHighlighted = isNewlyCreated,
                                    onClick = {
                                        navController.navigate("details/${activity.id}")
                                    }
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }

                PullRefreshIndicator(
                    refreshing = isRefreshing,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                )
            }
        }
    }
}
@Composable
fun CurrentCity(value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterEnd
    ) {
        Text(
            text = value,
            style = TextStyle(
                fontSize = 26.sp,
                fontFamily = FontFamily(Font(R.font.robotoblackitalic)),
                fontWeight = FontWeight.ExtraBold,
                fontStyle = FontStyle.Italic,
                color = Color(0xFF003366),
            )
        )
    }
}
@Composable
fun NoCurrentActivitiesBar(){
    Box(contentAlignment = Alignment.Center,modifier = Modifier
        .width(193.dp)
        .height(40.dp)
        .background(color = Color(0xFFF2F2F2), shape = RoundedCornerShape(size = 16.dp))){
        Text(
            text = "Brak aktywności",
            style = TextStyle(
                fontSize = 15.sp,
                fontFamily = FontFamily(Font(R.font.robotoitalic)),
                fontWeight = FontWeight(900),
                fontStyle = FontStyle.Italic,
                color = Color.Black,

                textAlign = TextAlign.Center,

                )
        )


    }
}
@Composable
fun FilterButton(navController: NavController, modifier: Modifier = Modifier) {
    val hapticFeedback = LocalHapticFeedback.current
    val configuration = LocalConfiguration.current
    val height = configuration.screenHeightDp.dp

    IconButton(
        onClick = {
            navController.navigate("filterScreen")
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        },
        modifier = Modifier
            .size(36.dp)

    ) {
        Icon(
            painter = painterResource(id = R.drawable.sliders),
            contentDescription = "search",
            tint = Color(0xFF003366),
            modifier = Modifier.fillMaxSize()
        )
    }
}