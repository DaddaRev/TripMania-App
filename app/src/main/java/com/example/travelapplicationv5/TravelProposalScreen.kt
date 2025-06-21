package com.example.travelapplicationv5

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Reviews
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.Button
import androidx.compose.ui.Alignment
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import coil3.compose.AsyncImage
import java.time.format.DateTimeFormatter
import android.content.res.Configuration
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.rememberAsyncImagePainter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate

class TravelProposalScreenViewModel (val model: TripModel, val userModel: UserModel, val notificationModel: NotificationModel) : ViewModel()
{
    val travelProposalsList: StateFlow<List<Trip>> = model.travelProposalsList

    private val _currentTripId = MutableStateFlow<Int?>(null)

    // Retrieve user informations from the model
    val isUserLoggedIn: StateFlow<Boolean> = userModel.isUserLoggedIn
    val userId: StateFlow<Int?> = userModel.loggedUser

    val tripToShow: StateFlow<Trip> = combine(
        model.travelProposalsList,
        _currentTripId
    ) { trips, id ->
        trips.find { it.id == id } ?: Trip()
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        Trip()
    )

    private val _owned = combine(
        tripToShow,
        userId
    ) { trip, uid ->
        trip.author.id == uid
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        false
    )

    val owned: StateFlow<Boolean> = _owned

    private val _sectionSelected = MutableStateFlow("trip")
    val sectionSelected: StateFlow<String> = _sectionSelected.asStateFlow()

    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog.asStateFlow()

    private val _showJoinDialog = MutableStateFlow(false)
    val showJoinDialog: StateFlow<Boolean> = _showJoinDialog.asStateFlow()

    private val _showLeaveDialog = MutableStateFlow(false)
    val showLeaveDialog: StateFlow<Boolean> = _showLeaveDialog.asStateFlow()

    var initialized by mutableStateOf(false) //used only for notification

    fun loadTripData(tripId: Int, section: String?) {
        if (tripId!=_currentTripId.value){
            _sectionSelected.value = section ?: "trip"
            _currentTripId.value = tripId
        }
        else if (tripId==_currentTripId.value && section!=null && !initialized){
            _sectionSelected.value = section ?: "trip"
            initialized = true
        }
    }

    fun isJoined(userId: Int) : Boolean {
        return model.isJoined(tripToShow.value.id, userId)
    }

    fun isCurrentUserAccepted(): Boolean {
        val currentUserId = userId.value ?: return false
        return tripToShow.value.requests.any { request ->
            request.status == RequestStatus.Accepted && request.user.id == currentUserId
        }
    }

    fun isRefused(userId: Int) : Boolean {
        return model.isRefused(tripToShow.value.id, userId)
    }

    fun openDeleteDialog() {
        _showDeleteDialog.value = true
    }

    fun closeDeleteDialog() {
        _showDeleteDialog.value = false
    }

    fun confirmDelete() {
        viewModelScope.launch {
            val trip = tripToShow.value

            val pathPrefix = "https://${SupabaseHandler.supabaseUrl.removePrefix("https://")}/storage/v1/object/public/${SupabaseHandler.bucketTrips}/"
            val toDeletePaths = trip.images.mapNotNull { url ->
                url.removePrefix(pathPrefix).takeIf { it.isNotEmpty() }
            }

            SupabaseHandler.deleteImages(
                imagePaths = toDeletePaths,
                bucketName = SupabaseHandler.bucketTrips
            )

            _showDeleteDialog.value = false
            model.removeTrip(trip)
        }

        _showDeleteDialog.value = false
        model.removeTrip(tripToShow.value)
    }

    fun openJoinDialog() {
        _showJoinDialog.value = true
    }

    fun closeJoinDialog() {
        _showJoinDialog.value = false
    }

    fun confirmJoin(people: Int) {
        _showJoinDialog.value = false
        userId.value?.let { model.addRequest(tripToShow.value.id, it,people-1) }
        val newNotification = Notification(userId = tripToShow.value.author.id.toLong(), type = NotificationType.NEW_APPLICATION, relatedUserId = userId.value?.toLong() ?: 0L, relatedTripId = tripToShow.value.id.toLong())
        notificationModel.addNotification(newNotification)
    }

    fun openLeaveDialog() {
        _showLeaveDialog.value = true
    }

    fun closeLeaveDialog() {
        _showLeaveDialog.value = false
    }

    fun confirmLeave() {
        _showLeaveDialog.value = false
        userId.value?.let { model.removeRequest(tripToShow.value.id, it) }
        val newNotification = Notification(userId = tripToShow.value.author.id.toLong(), type = NotificationType.APPLICATION_REMOVED, relatedUserId = userId.value?.toLong() ?: 0L, relatedTripId = tripToShow.value.id.toLong())
        notificationModel.addNotification(newNotification)
    }

    fun calculateRatingAverage(trip: Trip): Double {
        return model.calculateRatingAverage(trip)
    }

    fun denyRequest(memberId: Int) {
        model.denyRequest(tripToShow.value.id, memberId)
        val newNotification = Notification(userId = memberId.toLong(), type = NotificationType.APPLICATION_REFUSED, relatedTripId = tripToShow.value.id.toLong())
        notificationModel.addNotification(newNotification)
    }

    fun acceptRequest(memberId: Int) {
        model.acceptRequest(tripToShow.value.id, memberId)
        val newNotification = Notification(userId = memberId.toLong(), type = NotificationType.APPLICATION_ACCEPTED, relatedTripId = tripToShow.value.id.toLong())
        notificationModel.addNotification(newNotification)
    }

    fun viewTrip() {
        _sectionSelected.value = "trip"
    }

    fun viewReviews() {
        _sectionSelected.value = "reviews"
    }

    fun viewMembers() {
        _sectionSelected.value = "members"
    }

    fun isRemoved(userId: Int) : Boolean {
        return userModel.isRemoved(userId)
    }
}

object Factory : ViewModelProvider.Factory {
    val userModel: UserModel = UserModel()
    val notificationModel: NotificationModel = NotificationModel(userModel)
    val tripModel: TripModel = TripModel(userModel)
    val authModel: AuthRepository = AuthRepository()

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return when {
            modelClass.isAssignableFrom(TravelProposalScreenViewModel::class.java) ->
                TravelProposalScreenViewModel(tripModel, userModel, notificationModel) as T

            modelClass.isAssignableFrom(HandleTravelProposalScreenViewModel::class.java) ->
                HandleTravelProposalScreenViewModel(tripModel, userModel) as T

            modelClass.isAssignableFrom(TravelProposalListScreenViewModel::class.java) ->
                TravelProposalListScreenViewModel(tripModel, userModel) as T

            modelClass.isAssignableFrom(OwnTravelsViewModel::class.java) ->
                OwnTravelsViewModel(tripModel) as T

            modelClass.isAssignableFrom(ReviewTripScreenViewModel::class.java) ->
                ReviewTripScreenViewModel(tripModel, userModel, notificationModel) as T

            modelClass.isAssignableFrom(UserProfileScreenViewModel::class.java) ->
                UserProfileScreenViewModel(tripModel, userModel, authModel) as T

            modelClass.isAssignableFrom(NotificationsViewModel::class.java) ->
                NotificationsViewModel(notificationModel, tripModel, userModel) as T

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayTripScreen(
    navController: NavController,
    tripId: Int,
    section: String?,
    viewModel: TravelProposalScreenViewModel,
){
    LaunchedEffect(Unit) {
        viewModel.loadTripData(tripId, section)
    }

    val isLogged = viewModel.isUserLoggedIn.collectAsState()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Column(Modifier.fillMaxSize()) {
        TopBar(navController)
        Spacer(modifier = Modifier.height(10.dp))
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                DeleteTripDialog(navController, viewModel)
                JoinTripDialog(navController,viewModel)
                LeaveTripDialog(navController,viewModel)
                if(isLandscape){
                    Row(){
                        Column(
                            modifier = Modifier.fillMaxSize().weight(0.7f),
                        ) {
                            ImagesHorizontal(viewModel)
                        }
                        Column(
                            modifier = Modifier.fillMaxSize().weight(2f).padding(start = 12.dp),
                        ){
                            CorePart(navController, viewModel)
                        }
                    }
                }
                else{
                    ImagesVertical(viewModel)
                    Spacer(modifier = Modifier.padding(vertical = 7.dp))
                    CorePart(navController, viewModel)
                }
            }
        }

    }
}

@Composable
fun CorePart(navController: NavController, vm: TravelProposalScreenViewModel = viewModel(factory = Factory)) {
    val sectionSelected by vm.sectionSelected.collectAsState()

    SwitchSection(vm)
    if (sectionSelected=="trip") {
        TripSection(navController, vm)
    }
    else if(sectionSelected=="reviews"){
        ReviewsSection(navController, vm)
    }
    else{
        MembersSection(navController, vm)
    }
}

@Composable
fun ImagesVertical(vm: TravelProposalScreenViewModel = viewModel(factory = Factory)) {
    val trip by vm.tripToShow.collectAsState()

    Row(
        modifier = Modifier.fillMaxWidth().fillMaxHeight(0.33f),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if(trip.images.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1.78f)
                    .fillMaxHeight(),
            ) {
                AsyncImage(
                    model = trip.images[0],
                    placeholder = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = "First Image",
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(modifier = Modifier
                    .weight(1f)){
                    AsyncImage(
                        model = trip.images[1],
                        placeholder = painterResource(R.drawable.ic_launcher_foreground),
                        contentDescription = "Second Image",
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                Row(modifier = Modifier
                    .weight(1f)) {
                    AsyncImage(
                        model = trip.images[2],
                        placeholder = painterResource(R.drawable.ic_launcher_foreground),
                        contentDescription = "Map",
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Composable
fun ImagesHorizontal(vm: TravelProposalScreenViewModel = viewModel(factory = Factory)) {
    val trip by vm.tripToShow.collectAsState()

    Column(
        modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(bottom = 10.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Row(
            modifier = Modifier
                .weight(1.78f)
                .fillMaxWidth(),
        ) {
            AsyncImage(
                model = trip.images[0],
                placeholder = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = "First Image",
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Crop
            )
        }
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier
                .weight(1f)) {
                AsyncImage(
                    model = trip.images[1],
                    placeholder = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = "Second Image",
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            Column(modifier = Modifier
                .weight(1f)) {
                AsyncImage(
                    model = trip.images[2],
                    placeholder = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = "Map",
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun SwitchSection(vm: TravelProposalScreenViewModel = viewModel(factory = Factory)) {
    val sectionSelected by vm.sectionSelected.collectAsState()

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 30.dp).height(40.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ){
        Column( modifier = Modifier.weight(1f)
            .clickable {
                vm.viewTrip()
            },
            horizontalAlignment = Alignment.CenterHorizontally

        ) {
            if (sectionSelected=="trip"){
                Row(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.6f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Box(modifier = Modifier
                        .size(60.dp, 30.dp)
                        .background(Color(0xFFD8D4EC), shape = RoundedCornerShape(50))
                    ){
                        Icon(
                            imageVector = Icons.Filled.TravelExplore,
                            contentDescription = "Trip",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp).align(Alignment.Center)
                        )
                    }
                }
                Text(text = "Trip", color=Color.Black)
            }
            else{
                Row(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.6f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Icon(
                        imageVector = Icons.Filled.TravelExplore,
                        contentDescription = "Travel",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Text(text = "Trip", color=Color.DarkGray)
            }
        }
        Column( modifier = Modifier.weight(1f)
            .clickable {
                vm.viewReviews()
            },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if(sectionSelected=="reviews"){
                Row(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.6f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Box(modifier = Modifier
                        .size(60.dp, 30.dp)
                        .background(Color(0xFFD8D4EC), shape = RoundedCornerShape(50))
                    ){
                        Icon(
                            imageVector = Icons.Filled.Reviews,
                            contentDescription = "Review",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp).align(Alignment.Center)
                        )
                    }
                }
                Text(text = "Review", color = Color.Black)
            }
            else{
                Row(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.6f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Icon(
                        imageVector = Icons.Filled.Reviews,
                        contentDescription = "Review",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Text(text = "Review", color=Color.DarkGray)
            }
        }
        Column( modifier = Modifier.weight(1f)
            .clickable {
                vm.viewMembers()
            },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (sectionSelected=="members"){
                Row(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.6f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Box(modifier = Modifier
                        .size(60.dp, 30.dp)
                        .background(Color(0xFFD8D4EC), shape = RoundedCornerShape(50))
                    ){
                        Icon(
                            imageVector = Icons.Filled.Groups,
                            contentDescription = "Members",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp).align(Alignment.Center)
                        )
                    }
                }
                Text(text = "Members", color = Color.Black)
            }
            else{
                Row(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.6f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Icon(
                        imageVector = Icons.Filled.Groups,
                        contentDescription = "Members",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Text(text = "Members", color = Color.DarkGray)
            }
        }
    }
}

@Composable
fun TripSection(navController: NavController, vm: TravelProposalScreenViewModel = viewModel(factory = Factory)) {
    val trip by vm.tripToShow.collectAsState()
    val owned by vm.owned.collectAsState()
    val isLogged by vm.isUserLoggedIn.collectAsState()
    val userId by vm.userId.collectAsState()

    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Text(
        text = trip.title,
        style = MaterialTheme.typography.titleMedium,
        color = Color.Black,
        modifier = Modifier.padding(top = 12.dp)
    )
    Text(
        text = trip.description,
        style = MaterialTheme.typography.bodySmall,
        color = Color.Black,
        modifier = Modifier.padding(top = 5.dp)
    )
    Box(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyColumn(
                modifier = Modifier.weight(if (isLandscape) 1.3f else 2f).fillMaxHeight().padding(
                    end = if (isLandscape) 40.dp else 15.dp,
                    bottom = if (isLandscape) 10.dp else 50.dp
                ).fillMaxWidth().padding(top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(trip.itinerary.size) { index ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp)
                    ) {
                        Column() {
                            Row() {
                                Column(
                                    modifier = Modifier.weight(2f)
                                ) {
                                    Text(
                                        text = "• " + trip.itinerary[index].title,
                                        modifier = Modifier.padding(bottom = 2.dp),
                                        color = Color.Black,
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                                Column(
                                    horizontalAlignment = Alignment.End,
                                    modifier = Modifier.weight(0.5f)
                                ) {
                                    Row(
                                    ) {
                                        if (trip.itinerary[index].free) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = "Free",
                                                tint = Color.Black,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        } else {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Free",
                                                tint = Color.Black,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                        Text(
                                            text = "free",
                                            color = Color.Black,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(start = 3.dp)
                                        )
                                    }
                                }

                            }
                            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                            val date = trip.itinerary[index].date.format(formatter)
                            Text(
                                text = "on: " + date + ", at: " + trip.itinerary[index].location,
                                modifier = Modifier.padding(start = 6.dp),
                                color = Color.Black,
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Text(
                                "activities: " + trip.itinerary[index].activities,
                                modifier = Modifier.padding(start = 6.dp),
                                color = Color.Black,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxHeight()
                    .padding(
                        top = if (isLandscape) 10.dp else 10.dp,
                        bottom = if (isLandscape) 10.dp else 50.dp
                    )
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Today,
                            contentDescription = "Date",
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        val first = trip.date.first.format(formatter)
                        if (trip.date.second != null) {
                            val second = trip.date.second?.format(formatter)
                            Text(
                                text = "$first - $second",
                                fontSize = 12.sp,
                                color = Color.Black
                            )
                        } else
                            Text(text = first, fontSize = 12.sp, color = Color.Black)
                    }
                }
                item {
                    Row(
                        modifier = Modifier.padding(top = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "Countries",
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        val countries = trip.countries.joinToString(", ")
                        Text(
                            text = countries,
                            fontSize = 12.sp,
                            color = Color.Black
                        )
                    }
                }
                item {
                    Row(
                        modifier = Modifier.padding(top = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Group,
                            contentDescription = "Spots",
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Column(modifier = Modifier.fillMaxWidth()){
                            val spotsLeft = computeSpotsLeft(trip)

                            Text(
                                text = if (spotsLeft>0) "$spotsLeft free spots" else "no free spots",
                                fontSize = 12.sp,
                                color = Color.Black,
                                style = TextStyle(background = Color(0xFFD8D4EC))
                            )
                            Text(
                                text = trip.spotsTotal.toString()+ " total spots",
                                fontSize = 12.sp,
                                color = Color.Black
                            )
                        }
                    }
                }
                item {
                    Row(
                        modifier = Modifier.padding(top = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Euro,
                            contentDescription = "Price",
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = trip.price.first.toString() + " - " + trip.price.second.toString(),
                            fontSize = 12.sp,
                            color = Color.Black
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            UtilityMaps.openTripItinerary(context, trip.itinerary)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                    ) {
                        Icon(Icons.Default.Map, contentDescription = "Map", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Map")
                    }
                }
            }
        }
        if (isLogged && owned){
            FloatingActionButton(
                onClick = {
                    navController.navigate("handle/edit?tripId=${trip.id}")
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = if (isLandscape) 75.dp else 85.dp)
                    .size(50.dp),
                containerColor = Color(0xFF0288D1),
                contentColor = Color.White,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit"
                    )
                    //Spacer(modifier = Modifier.width(8.dp))
                    //Text(text = "Edit")
                }
            }
            FloatingActionButton(
                onClick = { vm.openDeleteDialog() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = if (isLandscape) 20.dp else 30.dp)
                    .size(50.dp),
                containerColor = Color.Red,
                contentColor = Color.White,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete"
                    )
                }
            }
        }
        else {
            if (
                isLogged &&  // If the user is logged in
                userId != null &&  // And exists
                !vm.isJoined(userId!!) &&  // And He's not yet part of the trip
                !vm.isRefused(userId!!) &&
                trip.date.first.isAfter(LocalDate.now()) &&
                computeSpotsLeft(trip) >= 1
            ) {
                FloatingActionButton(
                    onClick = {
                        vm.openJoinDialog()
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = if (isLandscape) 45.dp else 75.dp),
                ) {
                    Text(text = "Join")
                }
            }
            else if (
                isLogged &&
                userId != null &&
                vm.isJoined(userId!!) &&
                trip.date.first.isAfter(LocalDate.now())
            ) {
                FloatingActionButton(
                    onClick = {
                        vm.openLeaveDialog()
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = if (isLandscape) 45.dp else 75.dp),
                ) {
                    Text(text = "Leave")
                }
            }else if (!isLogged && trip.date.first.isAfter(LocalDate.now())){
                FloatingActionButton(
                    onClick = {
                        navController.navigate("login")
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = if (isLandscape) 45.dp else 75.dp),
                ) {
                    Text(text = "Join")
                }
            }
        }
    }
}

@Composable
fun UserData(navController: NavController, vm: TravelProposalScreenViewModel = viewModel(factory = Factory), user: UserProfile, removed: Boolean = false) {
    val imageId = user.imageId

    val userId by vm.userId.collectAsState()

    val painter = if (user.image != null) {
        rememberAsyncImagePainter(model = user.image)
    } else {
        if (imageId != null && imageId > 0) {
            painterResource(id = imageId)
        } else {
            painterResource(id = R.drawable.avatar1)
        }
    }

    if (removed){
        Row {
            Text(
                text = "Deleted User",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
        }
    }
    else {
        Row(
            modifier = Modifier
                .clickable {
                    navController.navigate("profile/${user.id}")
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
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
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (user.id == userId) {
                    "Me"
                } else {
                    user.nickname
                },
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun MembersSection(navController: NavController, vm: TravelProposalScreenViewModel = viewModel(factory = Factory)) {
    val trip by vm.tripToShow.collectAsState()
    val allTrips by vm.travelProposalsList.collectAsState()
    val owned by vm.owned.collectAsState()
    val isLogged by vm.isUserLoggedIn.collectAsState()

    val accepted = trip.requests.filter { it.status == RequestStatus.Accepted }
    val pending = trip.requests.filter { it.status == RequestStatus.Pending }
    val refused = trip.requests.filter { it.status == RequestStatus.Refused }

    val spotsLeft = computeSpotsLeft(trip)

    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.padding(top = 12.dp)
        ) {
            if (isLogged && owned && pending.size > 0) {
                item {
                    Text(
                        text = "Requests",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                items(pending.size) { index ->
                    val request = pending[index]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth().padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UserData(navController, vm, request.user)
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = computeAverageRating(collectUserReviews(request.user.id, allTrips)).round(1).toString(),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "star"
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        IconButton(onClick = {
                            if (spotsLeft>=(request.companion+1)) {
                                vm.acceptRequest(request.user.id)
                            }else {
                                Toast.makeText(context, "No spots available", Toast.LENGTH_SHORT).show()
                            }
                        }
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "accept",
                                tint = Color.Green
                            )
                        }
                        IconButton(onClick = { vm.denyRequest(request.user.id) }) {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                contentDescription = "deny",
                                tint = Color.Red
                            )
                        }
                    }
                    if (request.companion>0) {
                        Row {
                            Spacer(modifier = Modifier.width(47.dp))
                            Text(
                                text = "with " + request.companion + " companion",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(0.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
                item{
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
            item {
                Text(
                    text = "Members",
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserData(navController, vm, trip.author)
                    Spacer(modifier = Modifier.width(8.dp))
                    Image(
                        painter = painterResource(id = R.drawable.crown),
                        contentDescription = "Crown Icon",
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(Color.Black)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = computeAverageRating(collectUserReviews(trip.author.id, allTrips)).round(1).toString(),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "star"
                    )
                    //if (isLogged && owned) Spacer(modifier = Modifier.width(60.dp))
                }
                if (accepted.size == 0) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Still no other member",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            items(accepted.size) { index ->
                val request = accepted[index]

                Row(
                    modifier = Modifier
                        .fillMaxWidth().padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserData(navController, vm, request.user)
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = computeAverageRating(collectUserReviews(request.user.id, allTrips)).round(1).toString(),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "star"
                    )
                    if (owned) {
                        Spacer(modifier = Modifier.width(12.dp))
                        IconButton(onClick = { vm.denyRequest(request.user.id) }) {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                contentDescription = "remove",
                                tint = Color.Red
                            )
                        }
                    }
                }
                if (request.companion>0) {
                    Row {
                        Spacer(modifier = Modifier.width(47.dp))
                        Text(
                            text = "with " + request.companion + " companion",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(0.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
            if (owned && refused.size > 0) {
                item {
                    Text(
                        text = "Refused",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                items(refused.size) { index ->
                    val request = refused[index]

                    Row(
                        modifier = Modifier
                            .fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UserData(navController, vm, request.user)
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = computeAverageRating(collectUserReviews(request.user.id, allTrips)).round(1).toString(),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "star"
                        )
                    }
                    if (request.companion>0) {
                        Row {
                            Spacer(modifier = Modifier.width(47.dp))
                            Text(
                                text = "with " + request.companion + " companion",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(0.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
            item {
                Spacer(modifier = Modifier.height(120.dp))
            }
        }
        if (isLogged && owned){
            FloatingActionButton(
                onClick = {
                    navController.navigate("handle/edit?tripId=${trip.id}")
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = if (isLandscape) 75.dp else 85.dp)
                    .size(50.dp),
                containerColor = Color(0xFF0288D1),
                contentColor = Color.White,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit"
                    )
                }
            }
            FloatingActionButton(
                onClick = { vm.openDeleteDialog() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = if (isLandscape) 20.dp else 30.dp)
                    .size(50.dp),
                containerColor = Color.Red,
                contentColor = Color.White,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete"
                    )
                }
            }
        }
        else if(isLogged && vm.isCurrentUserAccepted()) {
            FloatingActionButton(
                onClick = {
                    navController.navigate("newReviewMembers/${trip.id}")
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = if (isLandscape) 45.dp else 75.dp),
            ) {
                Row() {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "AddMemberReview",
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Review member")
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }
    }
}

@Composable
fun RatingStars(rating: Double) {
    var fullStars = rating.toInt()
    val decimalPart = rating - fullStars

    val hasHalfStar = decimalPart > 0.25f && decimalPart < 0.75f
    if (decimalPart >= 0.75f) fullStars++
    val totalStars = 5
    val emptyStars = totalStars - fullStars - if (hasHalfStar) 1 else 0

    repeat(fullStars) {
        Icon(imageVector = Icons.Filled.Star, contentDescription = "Full Star")
    }

    if (hasHalfStar) {
        Icon(imageVector = Icons.Filled.StarHalf, contentDescription = "Half Star")
    }
    repeat(emptyStars) {
        Icon(imageVector = Icons.Default.StarOutline, contentDescription = "Empty Star")
    }
}

@Composable
fun ReviewsSection(navController: NavController, vm: TravelProposalScreenViewModel = viewModel(factory = Factory)) {
    val trip by vm.tripToShow.collectAsState()
    val owned by vm.owned.collectAsState()
    val isLogged by vm.isUserLoggedIn.collectAsState()

    var selectedImages by remember { mutableStateOf<List<String>>(emptyList()) }

    val generalRating = vm.calculateRatingAverage(trip)

    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.padding(top = 12.dp)
        ) {
            item {
                Text(
                    text = "Reviews",
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RatingStars(generalRating)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = generalRating.round(1).toString()+"/5",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = trip.reviews.size.toString()+ " global ratings",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            items(trip.reviews.size) { index ->
                val review = trip.reviews[index]
                val removed = vm.isRemoved(review.author.id)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserData(navController, vm, review.author, removed)
                    Spacer(modifier = Modifier.weight(1f))
                    RatingStars(review.rating.toDouble())
                }
                Text(
                    text = review.body,
                    style = MaterialTheme.typography.bodyMedium
                )
                if(!review.tips.isBlank()){
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tip:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = review.tips,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if(review.images.isNotEmpty()){
                    TextButton(onClick = { selectedImages = review.images }) {
                        Text("See photos (${review.images.size})")
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(140.dp))
            }
        }
        if (isLogged && owned){
            FloatingActionButton(
                onClick = {
                    navController.navigate("handle/edit?tripId=${trip.id}")
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = if (isLandscape) 75.dp else 85.dp)
                    .size(50.dp),
                containerColor = Color(0xFF0288D1),
                contentColor = Color.White,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit"
                    )
                }
            }
            FloatingActionButton(
                onClick = { vm.openDeleteDialog() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = if (isLandscape) 20.dp else 30.dp)
                    .size(50.dp),
                containerColor = Color.Red,
                contentColor = Color.White,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete"
                    )
                    //Spacer(modifier = Modifier.width(8.dp))
                    //Text(text = "Delete")
                }
            }
        }
        else if(vm.isCurrentUserAccepted()){
            FloatingActionButton(
                onClick = {
                    if(isLogged) {
                        navController.navigate("newReview/${trip.id}")
                    }else{
                        navController.navigate("login")  // Non reachable code
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = if (isLandscape) 45.dp else 75.dp),
            ) {
                Row() {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "AddReview",
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Review")
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }
        // If the user wants to see the photos related to a review --> open a dialog
        if (selectedImages.isNotEmpty()) {
            AlertDialog(
                onDismissRequest = { selectedImages = emptyList() },
                title = { Text("Review photos") },
                text = {
                    LazyRow (
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ){
                        items(selectedImages) { imageUri ->
                            Card(
                                modifier = Modifier
                                    .size(180.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                AsyncImage(
                                    model = imageUri,
                                    contentDescription = "Image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedImages = emptyList() }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}


@Composable
fun JoinTripDialog(
    navController: NavController,
    vm: TravelProposalScreenViewModel = viewModel(factory = Factory),
) {
    val showDialog by vm.showJoinDialog.collectAsState()
    val trip by vm.tripToShow.collectAsState()
    val minSpots: Int = 1
    val maxSpots: Int = computeSpotsLeft(trip)
    var spots by remember { mutableStateOf(1) }

    if (showDialog) {
        Dialog(onDismissRequest = {vm.closeJoinDialog()}) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Join the trip",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                    )
                    Text(
                        text = "How many are you?",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Row (modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center){
                        IconButton(
                            onClick = {
                                if (spots > minSpots) {
                                    spots -= 1
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBackIosNew,
                                contentDescription = "Decrement",
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = CircleShape
                                    )
                                    .padding(12.dp)
                            )
                        }
                        Text(
                            text = spots.toString(),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(12.dp)
                        )
                        IconButton(
                            onClick = {
                                if (spots < maxSpots) {
                                    spots += 1
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = "Increment",
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = CircleShape
                                    )
                                    .padding(12.dp)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { vm.closeJoinDialog()}){
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            vm.confirmJoin(spots)
                        }) {
                            Text("Apply")
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun DeleteTripDialog(
    navController: NavController,
    vm: TravelProposalScreenViewModel = viewModel(factory = Factory),
) {
    val showDialog by vm.showDeleteDialog.collectAsState()

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { vm.closeDeleteDialog() },
            title = { Text(text = "Confirm Deletion") },
            text = { Text("Are you sure you want to delete this trip?") },
            confirmButton = {
                Button(
                    onClick = {
                        vm.confirmDelete()
                        navController.popBackStack()
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { vm.closeDeleteDialog()
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}


@Composable
fun LeaveTripDialog(
    navController: NavController,
    vm: TravelProposalScreenViewModel = viewModel(factory = Factory),
) {
    val showDialog by vm.showLeaveDialog.collectAsState()

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { vm.closeLeaveDialog() },
            title = { Text(text = "Confirm Unjoin") },
            text = { Text("Are you sure you want to leave this trip?") },
            confirmButton = {
                Button(
                    onClick = {
                        vm.confirmLeave()
                    }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { vm.closeLeaveDialog()
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/*
@Preview
@Composable
fun TripPreview() {
    val tripModel = TripModel()
    //val vm = TravelProposalScreenViewModel(tripModel,)
    //DisplayTripScreen(navController, 0, vm)  // Empty trip --> Preview will not show anyting
}

 */