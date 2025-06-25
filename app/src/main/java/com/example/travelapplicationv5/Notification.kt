package com.example.travelapplicationv5

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewModelScope
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


class NotificationsViewModel (val model: NotificationModel, val tripModel: TripModel, val userModel: UserModel) : ViewModel()
{
    private var lastNotificationIds = emptySet<String>()

    val isLogged = model.isLogged
    val loggedUser = model.loggedUser
    val firstTime = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            isLogged.collectLatest { logged ->
                if (logged) {
                    firstTime.value = true
                }
            }
        }
    }

    val notifications: Flow<List<Notification>> = isLogged
        .flatMapLatest { isLogged ->
            if (isLogged) {
                model.getNotifications(loggedUser.value)
            }
            else {
                flowOf(emptyList())
            }
        }
        .onEach { currentNotifications ->
            if (firstTime.value){
                lastNotificationIds = currentNotifications.map { it.id }.toSet()
                firstTime.value = false
            }
            val newNotifs = checkNewNotifications(currentNotifications)
            if (newNotifs.isNotEmpty()) {
                newNotifs.forEach { notif ->
                    showSnackbar(notif)
                }
            }
        }

    private fun checkNewNotifications(currentNotifications: List<Notification>): List<Notification> {
        val currentIds = currentNotifications.map { it.id }.toSet()
        val newIds = currentIds - lastNotificationIds
        val newNotifs = currentNotifications.filter { it.id in newIds }
        lastNotificationIds = currentIds
        return newNotifs
    }

    fun generateMessage(notification: Notification): String {
        val trip = getTripById(notification.relatedTripId.toInt())
        val user = getUserById(notification.relatedUserId.toInt())
        val message = when (notification.type) {
            NotificationType.NEW_APPLICATION -> "${user.nickname} wants to join your trip: \"${trip.title}\""
            NotificationType.LAST_MINUTE_PROPOSAL -> "Last chance to join \"${trip.title}\""
            NotificationType.APPLICATION_ACCEPTED -> "Your application to the trip \"${trip.title}\" has been accepted"
            NotificationType.APPLICATION_REFUSED -> "Your application to the trip \"${trip.title}\" has been refused"
            NotificationType.APPLICATION_REMOVED -> "${user.nickname} has left your trip \"${trip.title}\""
            NotificationType.USER_REVIEW_RECEIVED -> "${user.nickname} reviewed you for the trip \"${trip.title}\""
            NotificationType.TRIP_REVIEW_RECEIVED -> "${user.nickname} reviewed your trip \"${trip.title}\""
            NotificationType.RECOMMENDED_TRIP -> "We found a trip you might like: \"${trip.title}\""
        }
        return message
    }

    fun getTripById(tripId: Int): Trip {
        val result = tripModel.travelProposalsList.value.filter { it.id == tripId }.firstOrNull()
        return result ?: Trip()
    }

    fun getUserById(userId: Int): UserProfile {
        val result =  userModel.usersList.value.filter { it.id == userId }.firstOrNull()
        return result ?: UserProfile()
    }

    fun markNotificationAsRead(notificationId: String) {
        model.markNotificationAsRead(notificationId)
    }

    private val _snackbarNotification = MutableStateFlow<Notification?>(null)
    val snackbarNotification: StateFlow<Notification?> = _snackbarNotification

    fun showSnackbar(notification: Notification) {
        _snackbarNotification.value = notification
    }

    fun clearSnackbar() {
        _snackbarNotification.value = null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    navController: NavController,
    viewModel: NotificationsViewModel = viewModel(factory = Factory),
    viewModelTPS: TravelProposalScreenViewModel = viewModel(factory = Factory),
){
    val notifications by viewModel.notifications.collectAsState(initial = listOf())

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Column(Modifier.fillMaxSize()) {
        TopBar(navController)
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(notifications) { notification ->
                    NotificationItem(notification, viewModel, onClick = {
                        when (notification.type) {
                            NotificationType.LAST_MINUTE_PROPOSAL -> {viewModelTPS.initialized=false
                                navController.navigate("detail/${notification.relatedTripId}?section=trip")}
                            NotificationType.NEW_APPLICATION -> {viewModelTPS.initialized=false
                                navController.navigate("detail/${notification.relatedTripId}?section=members")}
                            NotificationType.APPLICATION_ACCEPTED -> {viewModelTPS.initialized=false
                                navController.navigate("detail/${notification.relatedTripId}?section=members")}
                            NotificationType.APPLICATION_REFUSED -> {viewModelTPS.initialized=false
                                navController.navigate("detail/${notification.relatedTripId}?section=members")}
                            NotificationType.APPLICATION_REMOVED -> {viewModelTPS.initialized=false
                                navController.navigate("detail/${notification.relatedTripId}?section=members")}
                            NotificationType.USER_REVIEW_RECEIVED -> navController.navigate("profileReviews/${notification.relatedUserId}")
                            NotificationType.TRIP_REVIEW_RECEIVED -> {viewModelTPS.initialized=false
                                navController.navigate("detail/${notification.relatedTripId}?section=reviews")}
                            NotificationType.RECOMMENDED_TRIP -> {viewModelTPS.initialized=false
                                navController.navigate("detail/${notification.relatedTripId}?section=trip")}
                        }})

                    Divider(
                        color = Color.LightGray,
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No more notifications to show",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }

                }
            }
        }

    }
}

@Composable
fun NotificationItem(notification: Notification, viewModel: NotificationsViewModel, onClick: (Notification) -> Unit) {

    val backgroundColor = if (notification.read) Color(0xFFF7F7F7) else Color(0xFFE3F2FD)
    val fontWeight = if (notification.read) FontWeight.Normal else FontWeight.Bold

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(notification)
                viewModel.markNotificationAsRead(notification.id)}
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (notification.type) {
            NotificationType.NEW_APPLICATION -> {
                val user = viewModel.getUserById(notification.relatedUserId.toInt())
                val trip = viewModel.getTripById(notification.relatedTripId.toInt())
                ImageUserProfile(user)
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${user.nickname} wants to join your trip \"${trip.title}\"",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = fontWeight)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                ImageTrip(trip)
            }

            NotificationType.LAST_MINUTE_PROPOSAL -> {
                val trip = viewModel.getTripById(notification.relatedTripId.toInt())
                ImageTrip(trip)
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Last chance to join \"${trip.title}\"",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = fontWeight)
                    )
                }
            }

            NotificationType.USER_REVIEW_RECEIVED -> {
                val user = viewModel.getUserById(notification.relatedUserId.toInt())
                val trip = viewModel.getTripById(notification.relatedTripId.toInt())
                ImageUserProfile(user)
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${user.nickname} reviewed you for the trip \"${trip.title}\"",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = fontWeight)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                ImageTrip(trip)
            }

            NotificationType.TRIP_REVIEW_RECEIVED -> {
                val user = viewModel.getUserById(notification.relatedUserId.toInt())
                val trip = viewModel.getTripById(notification.relatedTripId.toInt())
                ImageUserProfile(user)
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${user.nickname} reviewed your trip \"${trip.title}\"",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = fontWeight)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                ImageTrip(trip)
            }

            NotificationType.RECOMMENDED_TRIP -> {
                val trip = viewModel.getTripById(notification.relatedTripId.toInt())
                ImageTrip(trip)
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "We found a trip you might like: \"${trip.title}\"",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = fontWeight)
                    )
                }
            }

            NotificationType.APPLICATION_ACCEPTED -> {
                val trip = viewModel.getTripById(notification.relatedTripId.toInt())
                ImageTrip(trip)
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Your application to the trip \"${trip.title}\" has been accepted",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = fontWeight)
                    )
                }
            }

            NotificationType.APPLICATION_REFUSED ->  {
                val trip = viewModel.getTripById(notification.relatedTripId.toInt())
                ImageTrip(trip)
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Your application to the trip \"${trip.title}\" has been refused",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = fontWeight)
                    )
                }
            }

            NotificationType.APPLICATION_REMOVED -> {
                val user = viewModel.getUserById(notification.relatedUserId.toInt())
                val trip = viewModel.getTripById(notification.relatedTripId.toInt())
                ImageUserProfile(user)
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${user.nickname} has left your trip \"${trip.title}\"",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = fontWeight)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                ImageTrip(trip)

            }
        }
    }
}

@Composable
fun ImageUserProfile(user: UserProfile) {

    val painter = if (user.image != null) {
        rememberAsyncImagePainter(model = user.image,  error = painterResource(id = user.imageId ?: R.drawable.avatar1))
    } else {
        if (user.imageId != null) {
            painterResource(id = user.imageId!!)
        } else {
            painterResource(id = R.drawable.avatar1)
        }
    }

    if (!user.imageMonogram) {
        Image(
            painter = painter,
            contentDescription = "Profile image",
            modifier = Modifier
                .width(40.dp)
                .height(40.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFD8D4EC)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${user.firstName[0]}${user.lastName[0]}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ImageTrip(trip: Trip) {
    AsyncImage(
        model = trip.images[0],
        placeholder = painterResource(R.drawable.ic_launcher_foreground),
        contentDescription = "Trip Image",
        modifier = Modifier.width(40.dp)
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp)),
        contentScale = ContentScale.Crop
    )
}