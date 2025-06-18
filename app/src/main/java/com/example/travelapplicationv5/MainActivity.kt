package com.example.travelapplicationv5

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.NavController
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import android.annotation.SuppressLint
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    private val viewModelTrips: TravelProposalScreenViewModel by viewModels{ Factory }
    private val viewModelNewTrip: HandleTravelProposalScreenViewModel by viewModels{ Factory}
    private val viewModelTripsList: TravelProposalListScreenViewModel by viewModels { Factory }
    private val viewModelOwnList: OwnTravelsViewModel by viewModels { Factory }
    private val viewModelUserProfile: UserProfileScreenViewModel by viewModels { Factory }
    private val viewModelReview: ReviewTripScreenViewModel by viewModels { Factory }
    private val viewModelNotification: NotificationsViewModel by viewModels { Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch {
            SupabaseHandler.anonymousLogin()
        }

        setContent {
            TravelApp(viewModelTrips, viewModelNewTrip, viewModelTripsList, viewModelOwnList, viewModelUserProfile, viewModelNotification, viewModelReview)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //saveUserProfile(viewModelUsers.userProfile)
    }
}


@Composable
fun TravelApp(vmTrips: TravelProposalScreenViewModel, vmNewTrip: HandleTravelProposalScreenViewModel,
                vmTripsList: TravelProposalListScreenViewModel, vmOwnTrips: OwnTravelsViewModel, vmUserProfile: UserProfileScreenViewModel,
              vmNotifications: NotificationsViewModel, vmReview: ReviewTripScreenViewModel) {

    val navController = rememberNavController()

    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarNotification by vmNotifications.snackbarNotification.collectAsState()
    val notification by vmNotifications.notifications.collectAsState(initial = emptyList()) //NON CANCELLARE

    LaunchedEffect(snackbarNotification) {
        snackbarNotification?.let { notification ->
            snackbarHostState.showSnackbar(vmNotifications.generateMessage(notification))
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) { message ->
            Snackbar(
                containerColor = Color.DarkGray,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.wrapContentWidth()
                    .padding(horizontal = 16.dp),
                content = {
                    Box(
                        modifier = Modifier
                            .clickable {
                                snackbarNotification?.let { notification ->
                                    when (notification.type) {
                                        NotificationType.LAST_MINUTE_PROPOSAL -> {vmTrips.initialized=false
                                            navController.navigate("detail/${notification.relatedTripId}?section=trip")}
                                        NotificationType.NEW_APPLICATION -> {vmTrips.initialized=false
                                            navController.navigate("detail/${notification.relatedTripId}?section=members")}
                                        NotificationType.APPLICATION_ACCEPTED -> {vmTrips.initialized=false
                                            navController.navigate("detail/${notification.relatedTripId}?section=members")}
                                        NotificationType.APPLICATION_REFUSED -> {vmTrips.initialized=false
                                            navController.navigate("detail/${notification.relatedTripId}?section=members")}
                                        NotificationType.APPLICATION_REMOVED -> {vmTrips.initialized=false
                                            navController.navigate("detail/${notification.relatedTripId}?section=members")}
                                        NotificationType.USER_REVIEW_RECEIVED -> navController.navigate("profileReviews/${notification.relatedUserId}")
                                        NotificationType.TRIP_REVIEW_RECEIVED -> {vmTrips.initialized=false
                                            navController.navigate("detail/${notification.relatedTripId}?section=reviews")}
                                        NotificationType.RECOMMENDED_TRIP -> {vmTrips.initialized=false
                                            navController.navigate("detail/${notification.relatedTripId}?section=trip")}
                                    }
                                    vmNotifications.clearSnackbar()
                                }
                            }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = message.visuals.message,
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                },
                action = {
                    IconButton(
                        onClick = {
                            snackbarHostState.currentSnackbarData?.dismiss()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }
            )}
        }
    ) { paddingValues ->
        val unused = paddingValues

        NavHost(navController = navController, startDestination = "list") {
            composable(
                "detail/{tripId}?section={section}",
                arguments = listOf(
                    navArgument("tripId") { type = NavType.IntType },
                    navArgument("section") {
                        type = NavType.StringType
                        defaultValue = null
                        nullable = true
                    })
            ) { entry ->
                val tripId = entry.arguments?.getInt("tripId") ?: -1
                val section = entry.arguments?.getString("section")
                DisplayTripScreen(navController, tripId, section, vmTrips)
            }
            composable(
                route = "handle/{mode}?tripId={tripId}",
                arguments = listOf(
                    navArgument("mode") {
                        type = NavType.StringType
                    },
                    navArgument("tripId") {
                        type = NavType.IntType
                        defaultValue = -1
                    }
                )
            ) { entry ->
                val mode = entry.arguments?.getString("mode") ?: "add"
                val tripId = entry.arguments?.getInt("tripId") ?: -1
                NewTravelScreen(navController, mode, tripId, vmNewTrip)
            }
            composable("list") {
                TravelListScreen(navController, vmTripsList, vmUserProfile)
            }
            composable("ownlist") {
                OwnTravelsList(navController, vmOwnTrips)
            }
            composable("notifications",) {
                NotificationsScreen(navController, vmNotifications, vmTrips)
            }
            composable(
                "profile/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.IntType })
            ) { entry ->
                val userId = entry.arguments?.getInt("userId") ?: -1
                DisplayUserScreen(navController, userId, vmUserProfile)
            }
            composable(
                "profileReviews/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.IntType })
            ) { entry ->
                val userId = entry.arguments?.getInt("userId") ?: -1
                DisplayUserReviewScreen(navController, userId, vmUserProfile)
            }
            composable("editProfile",) {
                EditProfileScreen(navController, vmUserProfile)
            }
            composable("editPreferences",) {
                EditableTravelPreferences(navController, vmUserProfile)
            }
            composable(
                "newReview/{tripId}", // Route with parameter
                arguments = listOf(navArgument("tripId") { type = NavType.IntType })
            ) { backStackEntry ->
                val tripId = backStackEntry.arguments?.getInt("tripId") ?: 0
                NewReviewScreen(navController, tripId, vmReview)
            }
            composable(
                "newReviewMembers/{tripId}", // Route with parameter
                arguments = listOf(navArgument("tripId") { type = NavType.IntType })
            ) { backStackEntry ->
                val tripId = backStackEntry.arguments?.getInt("tripId") ?: 0
                NewReviewMembersScreen(navController, tripId, vmReview)
            }
            composable("login") {
                vmUserProfile.clearAuthError()
                LogInScreen(navController, vmUserProfile, "login")
            }
            composable(
                "registration/{n}",
                arguments = listOf(navArgument("n") { type = NavType.IntType })
            ) { backStackEntry ->
                val n = backStackEntry.arguments?.getInt("n") ?: 2
                val phase = "registration" + n
                LogInScreen(navController, vmUserProfile, phase)
            }
        }
    }
}
