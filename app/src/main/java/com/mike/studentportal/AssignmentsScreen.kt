package com.mike.studentportal

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import com.mike.studentportal.CommonComponents as CC


@Composable
fun AssignmentScreen(navController: NavController, context: Context) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    var loading by remember { mutableStateOf(true) }
    val subjects = remember { mutableStateListOf<Subjects>() }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {

        LaunchedEffect(Unit) {
            MyDatabase.getSubjects { fetchedSubjects ->
                subjects.clear()
                subjects.addAll(fetchedSubjects ?: emptyList())
                loading = false
            }
        }


        val indicator = @Composable { tabPositions: List<TabPosition> ->
            Box(
                modifier = Modifier
                    .tabIndicatorOffset(tabPositions[selectedTabIndex])
                    .height(4.dp)
                    .width(screenWidth / (subjects.size.coerceAtLeast(1))) // Avoid division by zero
                    .background(GlobalColors.secondaryColor, CircleShape)
            )
        }

        val coroutineScope = rememberCoroutineScope()

        if (loading) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    color = GlobalColors.secondaryColor, trackColor = GlobalColors.textColor
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Loading Units", style = CC.descriptionTextStyle(context))

            }

        } else {
            if (subjects.isEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("No subjects found", style = CC.descriptionTextStyle(context))
                }

            } else {

                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier.background(Color.LightGray),
                    contentColor = Color.Black,
                    indicator = indicator,
                    edgePadding = 0.dp,
                    containerColor = GlobalColors.primaryColor
                ) {
                    subjects.forEachIndexed { index, subject ->

                        Tab(selected = selectedTabIndex == index, onClick = {
                            selectedTabIndex = index
                            coroutineScope.launch {
                                // Load assignments for the selected subject
                            }
                        }, text = {

                            Box(
                                modifier = Modifier
                                    .background(
                                        if (selectedTabIndex == index) GlobalColors.secondaryColor else GlobalColors.primaryColor,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(8.dp), contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = subject.name,
                                    color = if (selectedTabIndex == index) GlobalColors.textColor else GlobalColors.tertiaryColor,
                                )
                            }
                        }, modifier = Modifier.background(GlobalColors.primaryColor)
                        )
                    }
                }
            }

            when (selectedTabIndex) {
                in subjects.indices -> {
                    AssignmentsList(subjectId = subjects[selectedTabIndex].id, context)
                }
            }
        }

    }
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AssignmentsList(subjectId: String, context: Context) {
    var assignments by remember { mutableStateOf<List<Assignment>?>(null) }
    LaunchedEffect(subjectId) {
        MyDatabase.getAssignments(subjectId) { fetchedAssignments ->
            assignments = fetchedAssignments
        }
    }

    if (assignments == null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = GlobalColors.secondaryColor, trackColor = GlobalColors.textColor
            )
            Text("Loading Assignments...Please wait", style = CC.descriptionTextStyle(context))
        }
    } else {

        LazyColumn {
            if (assignments!!.isEmpty() || subjectId.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No assignments found.", style = CC.descriptionTextStyle(context))
                    }
                }
            }
            items(assignments!!) { assignment ->

                AssignmentCard(assignment = assignment, context)

            }
        }
    }
}


@Composable
fun AssignmentCard(
    assignment: Assignment, context: Context
) {
    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(tween(1000)),
        exit = slideOutVertically(tween(1000))
    ) {


        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp), colors = CardDefaults.cardColors(
                containerColor = GlobalColors.secondaryColor, contentColor = GlobalColors.textColor
            ), elevation = CardDefaults.elevatedCardElevation(), shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = GlobalColors.textColor,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = assignment.name,
                        style = CC.titleTextStyle(context).copy(fontSize = 18.sp),
                        color = GlobalColors.textColor
                    )

                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Author: ${Details.name.value}",
                    style = CC.descriptionTextStyle(context),
                    color = GlobalColors.tertiaryColor
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = assignment.description,
                    style = CC.descriptionTextStyle(context),
                    color = GlobalColors.textColor
                )

            }
        }
    }
}


@Preview
@Composable
fun AssignmentScreenPreview() {
    AssignmentScreen(rememberNavController(), LocalContext.current)
}
