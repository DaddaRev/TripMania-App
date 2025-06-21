package com.example.travelapplicationv5

import android.R.attr.maxHeight
import android.R.attr.maxWidth
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Abc
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoveToInbox
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Reviews
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.material3.Text
import kotlinx.coroutines.flow.onEach


data class UserProfileEditable(
    var firstName: String = "",
    var lastName: String = "",
    var nickname: String = "",
    var image: Uri? = null,
    var imageId: Int? = null,
    var imageMonogram: Boolean = false,
    var phoneNumber: String = "",
    var email: String = "",
    var dateOfBirth: String = "",
)

// ViewModel to manage user profile data
class UserProfileScreenViewModel(
    val tripModel: TripModel,
    val userModel: UserModel,
    val authModel: AuthRepository
) : ViewModel() {

    val travelProposalsList: StateFlow<List<Trip>> = tripModel.travelProposalsList

    // user profile loaded in the screen
    val _userProfileId = MutableStateFlow<Int>(0)
    val userProfileId: StateFlow<Int> = _userProfileId

    val userProfile: StateFlow<UserProfile> = combine(
        userModel.usersList,
        _userProfileId
    ) { users, id ->
        users.find { it.id == id } ?: UserProfile()
    }.onEach { profile ->
        if (userProfileId.value == userId.value) {
            // if the user is the one that is logged in --> Display his informations ready to be modified
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val date = profile.dateOfBirth.format(formatter)
            _userEditable.value = UserProfileEditable(
                profile.firstName,
                profile.lastName,
                profile.nickname,
                profile.image,
                profile.imageId,
                profile.imageMonogram,
                profile.phoneNumber,
                profile.email,
                date
            )
            _preferences.value = preferencesOptions.mapValues { (section, options) ->
                options.associateWith { option ->
                    profile.preferences[section]?.contains(option) == true
                }
            }
        }
        _isSelf.value = (userProfileId.value == userId.value )
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        UserProfile()
    )

    data class GroupedTrips(
        val planned: List<Trip>,
        val saved: List<Trip>,
        val own: List<Trip>,
        val past: List<Trip>
    )

    val groupedTrips: StateFlow<GroupedTrips> = combine(
        tripModel.travelProposalsList,
        userProfileId
    ) { trips, user ->
        val own = trips.filter { it.author.id == user }
        val joinedTrips = trips.filter { trip ->
            trip.requests.any { request ->
                request.status == RequestStatus.Accepted && request.user.id == user
            }
        } + own
        val planned = joinedTrips.filter { it.date.first.isAfter(LocalDate.now()) }
        val past = joinedTrips.filter { !it.date.first.isAfter(LocalDate.now()) }

        GroupedTrips(planned, emptyList(), own, past)
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        GroupedTrips(emptyList(), emptyList(), emptyList(), emptyList())
    )

    private val _isSelf = MutableStateFlow(false)
    val isSelf: StateFlow<Boolean> = _isSelf.asStateFlow()

    // Load the user profile data --> to display info in the screen
    fun loadUserData(user_id: Int) {
        _userProfileId.value = user_id
    }

    var imageMenu by mutableStateOf(false)
    var iconsMenu by mutableStateOf(false)

    fun toggleImageMenu() {
        imageMenu = !imageMenu
    }

    fun getTripById(tripId: Int): Trip {
        return tripModel.travelProposalsList.value.filter { it.id == tripId }[0]
    }

    private val _userReviews = MutableStateFlow<List<MemberReview>>(emptyList())
    val userReviews: StateFlow<List<MemberReview>> = _userReviews

    fun loadUserReviews(userId: Int) {
        _userProfileId.value = userId
        _userReviews.value = collectUserReviews(userId, tripModel.travelProposalsList.value)
    }

    fun isRemoved(userId: Int): Boolean {
        return userModel.isRemoved(userId)
    }

    private val _sectionSelected = MutableStateFlow("planned")
    val sectionSelected: StateFlow<String> = _sectionSelected.asStateFlow()

    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog.asStateFlow()

    fun openDeleteDialog() {
        _showDeleteDialog.value = true
    }

    fun closeDeleteDialog() {
        _showDeleteDialog.value = false
    }

    private val _userEditable = MutableStateFlow(UserProfileEditable())
    val userEditable: StateFlow<UserProfileEditable> = _userEditable

    val preferencesOptions = mapOf(
        "Group dimension" to listOf("2-3 people", "4-10 people", "10+"),
        "Destinations" to listOf("sea", "mountain", "nature", "city"),
        "Activity" to listOf("culture", "adventure", "relax", "shopping"),
        "Transport" to listOf("plane", "train", "car", "bus"),
        "Accomodation" to listOf("hotel", "resort", "apartment", "hostel", "campsite")
    )

    val icons = mapOf(
        "Group dimension" to Icons.Default.Groups,
        "Destinations" to Icons.Default.Place,
        "Activity" to Icons.Default.Palette,
        "Transport" to Icons.Default.DirectionsCar,
        "Accomodation" to Icons.Default.Home
    )


    fun getUserBadge(userProfile: UserProfile): UserBadge {
        return userProfile.currentBadge?.let { badgeName ->
            UserBadge.fromString(badgeName)
        } ?: determineBadgeFromTrips(userProfile.id)
    }
    private fun determineBadgeFromTrips(userId: Int): UserBadge {
        val tripsCount = groupedTrips.value.own.size
        return when {
            tripsCount >= UserBadge.TRAVEL_LEGEND.minTrips -> UserBadge.TRAVEL_LEGEND
            tripsCount >= UserBadge.TRAVEL_GURU.minTrips -> UserBadge.TRAVEL_GURU
            tripsCount >= UserBadge.EXPLORER.minTrips -> UserBadge.EXPLORER
            else -> UserBadge.NOVICE
        }
    }

    // **************** AUTH Features ****************
    // We retrieve the status of the log-in (true/false)
    val isUserLoggedIn = userModel.isUserLoggedIn
    val userId: StateFlow<Int?> = userModel.loggedUser

    private val _authPhase = MutableStateFlow<String>("login")
    val authPhase: StateFlow<String> = _authPhase.asStateFlow()

    fun setLoginPhase(){
        _authPhase.value = "login"
    }

    fun setRegistrationPhase(){
        _authPhase.value = "registration"
    }

    private val _authError = MutableStateFlow<String>("")
    val authError: StateFlow<String> = _authError.asStateFlow()

    fun clearAuthError(){
        _authError.value = ""
    }

    fun setAuthError(error: String){
        _authError.value = error
    }

    fun loadDataForRegistration(email: String, phone: String, name: String) {
        val parts = name.split(" ", limit = 2)
        val firstName = parts[0]
        val lastName = parts.getOrNull(1) ?: ""
        _userEditable.value = UserProfileEditable().copy(
            firstName = firstName,
            lastName = lastName,
            phoneNumber = phone,
            email = email
        )
        _preferences.value = preferencesOptions.mapValues { (section, options) ->
            options.associateWith { option -> false
            }
        }
    }

    // Function to login the user in the firestone database
    fun userLogIn(user: UserProfile) {
        userModel.userLogIn(user)
    }

    fun isRegistered(email: String): UserProfile?{
        return userModel.isRegistered(email)
    }

    fun saveProfileData(): Boolean {
        validateAll()

        if (isValid) {
            return true
        } else {
            return false
        }
    }

    fun userRegistration(context: Context): Boolean {
        val areValidPreferences = validatePreference()
        if (areValidPreferences) {

            val currentEditable = userEditable.value

            viewModelScope.launch {
                var imageUrl: String? = null
                currentEditable.image?.let { uri ->
                    imageUrl = SupabaseHandler.uploadUserImage(uri.toString(), context)
                }

                val updatedUser = currentEditable.copy(
                    image = imageUrl?.let { Uri.parse(it) } ?: currentEditable.image
                )


                userModel.userRegistration(updatedUser, preferences.value)
            }
            return true
        } else
            return false
    }

    fun userLogOut(){
        userModel.userLogOut()
    }

    // ****************************************************

    private val _preferences = MutableStateFlow<Map<String, Map<String, Boolean>>>(emptyMap())
    val preferences: StateFlow<Map<String, Map<String, Boolean>>> = _preferences

    data class ValidationUser(
        var firstName: String = "",
        var lastName: String = "",
        var nickname: String = "",
        var phoneNumber: String = "",
        var email: String = "",
        var dateOfBirth: String = "",
    )

    private val _validationErrors = MutableStateFlow(ValidationUser())
    val validationErrors: StateFlow<ValidationUser> = _validationErrors

    fun resetErrors() {
        _validationErrors.value = ValidationUser()
    }

    fun validateFirstName() {
        val error = when {
            userEditable.value.firstName.isBlank() -> "First name required"
            userEditable.value.firstName.length > 25 -> "First name too long"
            else -> ""
        }
        _validationErrors.value = _validationErrors.value.copy(firstName = error)
    }

    fun validateLastName() {
        val error = when {
            userEditable.value.lastName.isBlank() -> "Last name required"
            userEditable.value.lastName.length > 25 -> "Last name too long"
            else -> ""
        }
        _validationErrors.value = _validationErrors.value.copy(lastName = error)
    }

    fun validatePhone() {
        val error = when {
            userEditable.value.phoneNumber.isBlank() -> "Phone number required"
            userEditable.value.phoneNumber.length > 15 -> "Phone number too long"
            else -> ""
        }
        _validationErrors.value = _validationErrors.value.copy(phoneNumber = error)
    }

    fun validateEmail() {
        val error = when {
            userEditable.value.email.isBlank() -> "Email required"
            !userEditable.value.email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]+$")) -> "Invalid email format"
            else -> ""
        }
        _validationErrors.value = _validationErrors.value.copy(email = error)
    }

    fun validateNickname() {
        val error = when {
            userEditable.value.nickname.isEmpty() -> "Nickname required"
            userEditable.value.nickname.length > 25 -> "Nickname too long"
            !userEditable.value.nickname.matches(Regex("^[a-zA-Z0-9_.-]+$")) -> "Nickname contains invalid characters"
            else -> ""
        }
        _validationErrors.value = _validationErrors.value.copy(nickname = error)
    }

    fun validateDateOfBirth() {
        val italianFormat = Regex("^([0]?[1-9]|[12][0-9]|3[01])/([0]?[1-9]|1[0-2])/\\d{4}$")
        val americanFormat = Regex("^([0]?[1-9]|1[0-2])/([0]?[1-9]|[12][0-9]|3[01])/\\d{4}$")

        val italianFormatter = DateTimeFormatter.ofPattern("d/M/yyyy")
        val americanFormatter = DateTimeFormatter.ofPattern("M/d/yyyy")

        var valid = false

        if (userEditable.value.dateOfBirth.matches(italianFormat)) {
            val parts = userEditable.value.dateOfBirth.split("/")
            val day = parts[0].toInt().toString()
            val month = parts[1].toInt().toString()
            val year = parts[2]
            val data = "$day/$month/$year"
            try {
                val parsedDate = LocalDate.parse(data, italianFormatter)
                val reformatted = parsedDate.format(italianFormatter)
                if (reformatted == data)
                    valid = true
            } catch (e: DateTimeParseException) {
            }
        } else if (userEditable.value.dateOfBirth.matches(americanFormat)) {
            val parts = userEditable.value.dateOfBirth.split("/")
            val day = parts[0].toInt().toString()
            val month = parts[1].toInt().toString()
            val year = parts[2]
            val data = "$month/$day/$year"

            try {
                val parsedDate = LocalDate.parse(data, americanFormatter)
                val reformatted = parsedDate.format(americanFormatter)
                if (reformatted == data)
                    valid = true
            } catch (e: DateTimeParseException) {
            }
        }

        val error = when {
            userEditable.value.dateOfBirth.isEmpty() -> "Date of birth required"
            !valid -> "Invalid date format"
            else -> ""
        }
        _validationErrors.value = _validationErrors.value.copy(dateOfBirth = error)
    }

    fun validateAll() {
        validateFirstName()
        validateLastName()
        validateNickname()
        validateEmail()
        validatePhone()
        validateDateOfBirth()
    }

    val isValid: Boolean
        get() = with(validationErrors.value) {
            firstName.isBlank() &&
                    lastName.isBlank() &&
                    nickname.isBlank() &&
                    email.isBlank() &&
                    phoneNumber.isBlank() &&
                    dateOfBirth.isBlank()
        }

    // Upate image (URL)
    fun updateImage(image: Uri?) {
        _userEditable.value = userEditable.value.copy(imageId = null)
        _userEditable.value = userEditable.value.copy(imageMonogram = false)
        _userEditable.value = userEditable.value.copy(image = image)
    }

    // Update image (id))
    fun updateImage(imageId: Int?) {
        _userEditable.value = userEditable.value.copy(imageId = imageId)
        _userEditable.value = userEditable.value.copy(imageMonogram = false)
        _userEditable.value = userEditable.value.copy(image = null)
    }

    // Update the image (monogram)
    fun updateImage(monogram: Boolean) {
        _userEditable.value = userEditable.value.copy(imageId = null)
        _userEditable.value = userEditable.value.copy(imageMonogram = monogram)
        _userEditable.value = userEditable.value.copy(image = null)
    }

    fun setFirstName(firstName: String) {
        _userEditable.value = userEditable.value.copy(firstName = firstName)
    }

    fun setLastName(lastName: String) {
        _userEditable.value = userEditable.value.copy(lastName = lastName)
    }

    fun setNickname(nickname: String) {
        _userEditable.value = userEditable.value.copy(nickname = nickname)
    }

    fun setPhoneNumber(phoneNumber: String) {
        _userEditable.value = userEditable.value.copy(phoneNumber = phoneNumber)
    }

    fun setEmail(email: String) {
        _userEditable.value = userEditable.value.copy(email = email)
    }

    fun setDateOfBirth(dateOfBirth: String) {
        _userEditable.value = userEditable.value.copy(dateOfBirth = dateOfBirth)
    }

    fun updateUserProfile(context: Context): Boolean {
        validateAll()

        if (isValid) {
            val currentEditable = userEditable.value

            viewModelScope.launch {
                var imageUrl: String? = null
                currentEditable.image?.let { uri ->
                    imageUrl = SupabaseHandler.uploadUserImage(uri.toString(), context)
                }

                val updatedUser = currentEditable.copy(
                    image = imageUrl?.let { Uri.parse(it) } ?: currentEditable.image
                )

                userModel.updateUserProfile(userProfile.value.id, updatedUser)
            }
            return true

        } else {
            return false
        }
    }

    fun deleteUser() {
        tripModel.removeUser(userProfile.value.id)
        userModel.removeUser()
    }

    fun viewPlannedTrips() {
        _sectionSelected.value = "planned"
    }

    fun viewPastTrips() {
        _sectionSelected.value = "past"
    }

    fun viewOrganizedTrips() {
        _sectionSelected.value = "organized"
    }

    fun viewSavedTrips() {
        _sectionSelected.value = "saved"
    }

    fun setPreference(section: String, option: String, checked: Boolean) {
        _preferences.value = _preferences.value
            .mapValues { (sec, options) ->
                if (sec == section) {
                    options.mapValues { (opt, selected) ->
                        if (opt == option) checked else selected
                    }
                } else options
            }
    }

    private fun validatePreference(): Boolean {
        return _preferences.value.all { section ->
            section.value.any { it.value }
        }
    }

    fun updatePreference(): Boolean {
        val areValidPreferences = validatePreference()
        if (areValidPreferences) {
            userModel.updatePreferences(preferences.value)
            return true
        } else
            return false
    }

}

@Composable
fun UserBadgeDisplay(userProfile: UserProfile) {
    val badge = remember(userProfile) {
        UserBadge.fromString(userProfile.currentBadge)
    }

    Box(
        modifier = Modifier
            .background(badge.color, RoundedCornerShape(16.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = badge.iconResId),
                contentDescription = badge.displayName,
                modifier = Modifier.size(16.dp),
                tint = Color.White
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = badge.displayName,
                color = Color.White,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
@Composable
fun DisplayUserScreen(
    navController: NavController,
    userId: Int,
    viewModel: UserProfileScreenViewModel = viewModel(factory = Factory)
) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()
    val isSelf by viewModel.isSelf.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadUserData(userId)
    }

    Column(Modifier.fillMaxSize()) {
        TopBar(navController)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .background(Color.White)
        ) {
            val portrait = maxWidth < maxHeight

            LazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Spacer(Modifier.height(5.dp))

                    if (portrait) {
                        //Image:
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(top = 20.dp)) {
                                ProfileImage(context, viewModel, false)
                            }
                            Spacer(Modifier.width(10.dp))
                            UserInfoColumn(navController, viewModel, portrait)
                        }
                        Spacer(Modifier.height(10.dp))
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProfileImage(context, viewModel, false)
                            Spacer(Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(2f)) {
                                UserInfoColumn(navController, viewModel, portrait)
                            }
                        }
                    }
                }

                // Check if the profile is the one of the user watching it
                if (!isSelf) {
                    item {
                        //Travel preferences:
                        TravelPreferencesSection(userProfile.preferences, viewModel)
                        Spacer(Modifier.height(10.dp))
                    }
                } else {
                    item {
                        //User additional info:
                        UserAdditionalInfoColumn(viewModel)
                        Spacer(Modifier.height(5.dp))
                        TravelPreferencesSection(userProfile.preferences, viewModel)
                        Spacer(Modifier.height(8.dp))
                    }

                }
                item {
                    //Trips bar:
                    TravelTabBar(isSelf, viewModel)
                    TripsPart(navController, viewModel)
                }
            }

        }
    }
}

@Composable
fun TripsPart(
    navController: NavController,
    viewModel: UserProfileScreenViewModel = viewModel(factory = Factory)
) {
    val sectionSelected by viewModel.sectionSelected.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val groupedTrips by viewModel.groupedTrips.collectAsState()
    val isSelf by viewModel.isSelf.collectAsState()

    when (sectionSelected) {
        "planned" -> {
            groupedTrips.planned.forEach { tripDetail ->
                TripSectionUser(
                    navController,
                    isSelf,
                    trip = tripDetail,
                    portrait = false,
                    spotsLeft = computeSpotsLeft(tripDetail),
                    owned = if (tripDetail.author.id == userProfile.id) true else false,
                    past = false,
                    modifier = Modifier
                        .fillMaxWidth(),
                    onClick = {
                        navController.navigate("detail/${tripDetail.id}")
                    }
                )
                Spacer(Modifier.height(8.dp))
            }
        }

        "saved" -> {
            groupedTrips.saved.forEach { tripDetail ->
                TripSectionUser(
                    navController,
                    isSelf,
                    trip = tripDetail,
                    portrait = false,
                    spotsLeft = computeSpotsLeft(tripDetail),
                    owned = if (tripDetail.author.id == userProfile.id) true else false,
                    past = false,
                    modifier = Modifier
                        .fillMaxWidth(),
                    onClick = {
                        navController.navigate("detail/${tripDetail.id}")
                    }
                )
                Spacer(Modifier.height(8.dp))
            }
        }

        "organized" -> {
            groupedTrips.own.forEach { tripDetail ->
                TripSectionUser(
                    navController,
                    isSelf,
                    trip = tripDetail,
                    portrait = false,
                    spotsLeft = computeSpotsLeft(tripDetail),
                    owned = if (tripDetail.author.id == userProfile.id) true else false,
                    past = false,
                    modifier = Modifier
                        .fillMaxWidth(),
                    onClick = {
                        navController.navigate("detail/${tripDetail.id}")
                    }
                )
                Spacer(Modifier.height(8.dp))
            }
        }

        "past" -> {
            groupedTrips.past.forEach { tripDetail ->
                TripSectionUser(
                    navController,
                    isSelf,
                    trip = tripDetail,
                    portrait = false,
                    spotsLeft = computeSpotsLeft(tripDetail),
                    owned = if (tripDetail.author.id == userProfile.id) true else false,
                    past = true,
                    modifier = Modifier
                        .fillMaxWidth(),
                    onClick = {
                        navController.navigate("detail/${tripDetail.id}")
                    }
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}


// Edit own profile composable
@Composable
fun EditProfileScreen(
    navController: NavController,
    viewModel: UserProfileScreenViewModel = viewModel(factory = Factory),
    auth: Boolean = false
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val context = LocalContext.current

    // Then we show the graphical interface for the edit
    Column(Modifier.fillMaxSize()) {
        TopBar(navController)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            val portrait = maxWidth < maxHeight

            DeleteDialog(navController, viewModel)
            LazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Spacer(Modifier.height(20.dp))

                    if (portrait) {
                        ProfileImage(context, viewModel, true)
                        Spacer(Modifier.height(10.dp))
                        UserEditColumn(viewModel)
                        Spacer(Modifier.height(10.dp))
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProfileImage(context, viewModel, true)
                            Spacer(Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(2f)) {
                                UserEditColumn(viewModel)
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    // Choice buttons
                    Row(
                        modifier = Modifier
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (auth) {
                            Button(
                                onClick = {
                                    val success = viewModel.saveProfileData()
                                    if (success) {
                                        navController.navigate("registration/3")
                                    }
                                },
                                modifier = Modifier
                                    .width(250.dp)
                                    .height(50.dp)
                            ) {
                                Text("Complete sign up")
                            }
                        }else {
                            // Delete Button
                            Button(
                                onClick = {
                                    viewModel.openDeleteDialog()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.elevatedButtonColors(
                                    containerColor = Color.Red,
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Delete Account")
                            }
                            // Confirm Button
                            Button(
                                onClick = {
                                    val success = viewModel.updateUserProfile(context)
                                    if (success) {
                                        navController.navigate("profile/${userProfile.id}")
                                    }
                                },
                                modifier = Modifier.weight(1f),
                            ) {
                                Text("Confirm")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileImage(context: Context, viewModel: UserProfileScreenViewModel, isEditing: Boolean) {
    val userProfile by viewModel.userProfile.collectAsState()
    val userEditable by viewModel.userEditable.collectAsState()

    val image = if (isEditing) userEditable.image else userProfile.image
    val imageId = if (isEditing) userEditable.imageId else userProfile.imageId
    val imageMonogram = if (isEditing) userEditable.imageMonogram else userProfile.imageMonogram

    val painter = if (image != null) {
        rememberAsyncImagePainter(model = image, onError = {
            Toast.makeText(context, "Error on image loading", Toast.LENGTH_SHORT).show()
            viewModel.updateImage(image = null)
        })
    } else {
        if (imageId != null && imageId != -1) {
            painterResource(id = imageId)
        } else {
            painterResource(id = R.drawable.avatar1)
        }
    }

    fun processImageResult(result: ActivityResult) {
        if (result.resultCode == RESULT_OK) {
            viewModel.updateImage(result.data?.data)
        } else {
            Toast.makeText(context, "Error loading image", Toast.LENGTH_SHORT).show()
        }
    }

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result -> processImageResult(result) }

    Row {
        Box {
            if (!imageMonogram) {
                Image(
                    painter = painter,
                    contentDescription = "Profile image",
                    modifier = Modifier
                        .width(125.dp)
                        .height(125.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .width(125.dp)
                        .height(125.dp)
                        .clip(CircleShape)
                        .background(Color.Cyan),
                    contentAlignment = Alignment.Center
                ) {
                    if (userProfile.firstName.isNotBlank() && userProfile.lastName.isNotBlank()) {
                        Text(
                            text = "${userProfile.firstName[0]}${userProfile.lastName[0]}",
                            style = MaterialTheme.typography.headlineLarge
                        )
                    }
                }
            }
            if (isEditing) {
                IconButton(
                    onClick = { viewModel.toggleImageMenu() },
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 10.dp, start = 10.dp)
                            .background(Color.White, shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Modify image",
                            tint = Color.Black
                        )
                    }
                }
            }
        }
        DropdownMenu(
            expanded = viewModel.imageMenu,
            onDismissRequest = {
                viewModel.toggleImageMenu()
                viewModel.iconsMenu = false
            }
        ) {
            DropdownMenuItem(
                leadingIcon = {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null)
                },
                text = { Text("Take a photo") },
                onClick = {
                    val intent = Intent(context, Camera::class.java)
                    imageLauncher.launch(intent)
                    viewModel.toggleImageMenu()
                }

            )
            DropdownMenuItem(
                leadingIcon = {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                },
                text = { Text("Select from the gallery") },
                onClick = {
                    val selectIntent = Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    ).apply {
                        type = "image/*"
                    }
                    imageLauncher.launch(selectIntent)
                    viewModel.toggleImageMenu()
                }
            )
            DropdownMenuItem(
                leadingIcon = {
                    Icon(Icons.Default.Image, contentDescription = null)
                },
                text = { Text("Choose a default icon") },
                onClick = {
                    viewModel.iconsMenu = true
                    viewModel.toggleImageMenu()
                }
            )
            DropdownMenuItem(
                leadingIcon = {
                    Icon(Icons.Default.Abc, contentDescription = null)
                },
                text = { Text("Generate a monogram") },
                onClick = {
                    viewModel.updateImage(true)
                    viewModel.toggleImageMenu()
                }
            )
        }

        DropdownMenu(
            expanded = viewModel.iconsMenu,
            onDismissRequest = {
                viewModel.iconsMenu = false
            }
        ) {
            val images: List<Int> = listOf(
                R.drawable.avatar1,
                R.drawable.avatar2,
                R.drawable.avatar3,
                R.drawable.avatar4,
                R.drawable.avatar5,
                R.drawable.avatar6
            )
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                images.chunked(3).forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        row.forEach { img ->
                            Image(
                                painter = painterResource(id = img),
                                contentDescription = "Avatar icon",
                                modifier = Modifier
                                    .width(65.dp)
                                    .height(65.dp)
                                    .clickable {
                                        viewModel.iconsMenu = false
                                        viewModel.updateImage(img)
                                    }
                                    .padding(2.dp)
                                    .clip(CircleShape)
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun UserInfoColumn(
    navController: NavController,
    viewModel: UserProfileScreenViewModel = viewModel(factory = Factory),
    portrait: Boolean
) {
    val isSelf by viewModel.isSelf.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val allTrips by viewModel.travelProposalsList.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.End
        ) {
            if (isSelf) {
                IconButton(
                    onClick = {
                        viewModel.resetErrors()
                        navController.navigate("editProfile")
                    },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(
                    onClick = {
                        navController.navigate("editPreferences")
                    },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(Icons.Default.FavoriteBorder, contentDescription = "Preferences")
                }
            }
            IconButton(
                onClick = {
                    navController.navigate("profileReviews/${userProfile.id}")
                },
                modifier = Modifier.size(32.dp),
            ) {
                Icon(Icons.Default.Reviews, contentDescription = "Reviews")
            }
            if (isSelf){
                IconButton(
                    onClick = {
                        viewModel.userLogOut()
                        navController.navigate("list")
                    },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                }
            }
        }
        /*Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.End
        ) {
            if (isSelf) {
                Text(
                    text = "Logout",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    style = TextStyle(textDecoration = TextDecoration.Underline),
                    modifier = Modifier.padding(end = 7.dp).clickable {
                        viewModel.userLogOut()
                        navController.navigate("list")
                    }
                )
            }
        }*/

        Spacer(modifier = Modifier.height(7.dp))
        //Full name:
        Text(
            text = "${userProfile.firstName} ${userProfile.lastName}",
            modifier = Modifier.align(Alignment.Start),
            style = MaterialTheme.typography.titleLarge
        )

        //Nickname:
        Text(
            text = "@${userProfile.nickname}",
            modifier = Modifier
                .padding(top = 2.dp)
                .align(Alignment.Start),
            style = MaterialTheme.typography.titleLarge.copy(color = Color.Gray)
        )
        Spacer(Modifier.height(5.dp))

        //badge
        UserBadgeDisplay(userProfile = userProfile)

        Row(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            RatingStars(computeAverageRating(collectUserReviews(userProfile.id, allTrips)))
        }
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
fun UserAdditionalInfoColumn(viewModel: UserProfileScreenViewModel) {
    val userProfile by viewModel.userProfile.collectAsState()

    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val date = userProfile.dateOfBirth.format(formatter)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color(0xFFF5F5F5), shape = RoundedCornerShape(12.dp))
            .padding(2.dp)
            .border(
                width = 1.dp,
                color = Color(0xFFD8D4EC),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(10.dp)
    ) {
        Text(
            "User Info",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        //Phone number:
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = "Phone number",
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                userProfile.phoneNumber,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        //Email:
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = "Email",
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                userProfile.email,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        //Birthday:
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Cake,
                contentDescription = "Birthday",
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                date,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun UserEditColumn(viewModel: UserProfileScreenViewModel) {
    val alignment = Alignment.CenterHorizontally
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        val userEditable by viewModel.userEditable.collectAsState()
        val validationErrors by viewModel.validationErrors.collectAsState()

        OutlinedTextField(
            value = userEditable.firstName,
            onValueChange = { viewModel.setFirstName(it) },
            label = { Text("First Name") },
            isError = validationErrors.firstName.isNotBlank(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = userEditable.lastName,
            onValueChange = { viewModel.setLastName(it) },
            label = { Text("Last Name") },
            isError = validationErrors.lastName.isNotBlank(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = userEditable.nickname,
            onValueChange = { viewModel.setNickname(it) },
            label = { Text("Nickname") },
            isError = validationErrors.nickname.isNotBlank(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = userEditable.phoneNumber,
            onValueChange = { viewModel.setPhoneNumber(it) },
            label = { Text("Phone Number") },
            isError = validationErrors.phoneNumber.isNotBlank(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )
        /*
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = userEditable.email,
            onValueChange = { viewModel.setEmail(it) },
            label = { Text("Email") },
            isError = validationErrors.email.isNotBlank(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        */
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = userEditable.dateOfBirth,
            onValueChange = { viewModel.setDateOfBirth(it) },
            label = { Text("Date of birth") },
            isError = validationErrors.dateOfBirth.toString().isNotBlank(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )
        Spacer(Modifier.height(15.dp))
        //EditableTravelPreferencesSection(userProfile.preferences)
        //Spacer(Modifier.height(20.dp))

        if (validationErrors.firstName.isNotBlank()) {
            Text(validationErrors.firstName, color = MaterialTheme.colorScheme.error)
        }
        if (validationErrors.lastName.isNotBlank()) {
            Text(validationErrors.lastName, color = MaterialTheme.colorScheme.error)
        }
        if (validationErrors.phoneNumber.isNotBlank()) {
            Text(validationErrors.phoneNumber, color = MaterialTheme.colorScheme.error)
        }
        if (validationErrors.email.isNotBlank()) {
            Text(validationErrors.email, color = MaterialTheme.colorScheme.error)
        }
        if (validationErrors.nickname.isNotBlank()) {
            Text(validationErrors.nickname, color = MaterialTheme.colorScheme.error)
        }
        if (validationErrors.dateOfBirth.isNotBlank()) {
            Text(validationErrors.dateOfBirth, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun TravelPreferencesSection(
    preferences: Map<String, List<String>>,
    viewModel: UserProfileScreenViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color(0xFFF5F5F5), shape = RoundedCornerShape(12.dp))
            .padding(2.dp)
            .border(
                width = 1.dp,
                color = Color(0xFFD8D4EC),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(10.dp)
    ) {
        Text(
            "Travel Preferences",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        preferences.forEach { (section, values) ->

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                val valuesSize = values.size
                var valuesCounter = 0

                Icon(
                    imageVector = viewModel.icons[section] ?: Icons.Default.Settings,
                    contentDescription = section,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(4.dp))
                values.forEach { value ->
                    Text(
                        "$value${if (valuesCounter == valuesSize - 1) "" else "; "}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    valuesCounter++
                }
            }
        }
    }
}

@Composable
fun EditableTravelPreferences(navController: NavController, viewModel: UserProfileScreenViewModel, auth: Boolean = false) {
    val userProfile by viewModel.userProfile.collectAsState()
    val context = LocalContext.current

    val error = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        TopBar(navController)
        LazyColumn(
            modifier = Modifier.padding(16.dp)
        ) {
            item {
                Spacer(Modifier.height(15.dp))
                Text(
                    "Travel Preferences",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))

                viewModel.preferencesOptions.forEach { (section, list) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = viewModel.icons[section] ?: Icons.Default.Settings,
                            contentDescription = section,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            section,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    CheckboxSection(viewModel, section, list)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                Spacer(modifier = Modifier.height(10.dp))
                if (error.value) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Please select at least one option for each section",
                            color = Color.Red,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
                if(auth) {
                    Button(
                        onClick = {
                            val success = viewModel.userRegistration(context)
                            error.value = !success
                            if (success)
                                navController.navigate("list")
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Confirm sign up")
                    }
                }else{
                    Button(
                        onClick = {
                            val success = viewModel.updatePreference()
                            error.value = !success
                            if (success)
                                navController.navigate("profile/${userProfile.id}")
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun CheckboxSection(viewModel: UserProfileScreenViewModel, section: String, options: List<String>) {
    val preferences by viewModel.preferences.collectAsState()

    Column(
        modifier = Modifier.padding(start = 16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        options.forEach { option ->

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Checkbox(
                    checked = preferences[section]?.get(option) == true,
                    onCheckedChange = { isChecked ->
                        viewModel.setPreference(
                            section,
                            option,
                            isChecked
                        )
                    }
                )
                Text(text = option)
            }
        }
    }
}

data class Section(
    val key: String,
    val title: String,
    val icon: ImageVector
)

fun getTravelTabSections(isSelf: Boolean): List<Section> {
    return if (isSelf) {
        listOf(
            Section("planned", "Planned", Icons.Default.FlightTakeoff),
            Section("saved", "Saved", Icons.Default.Bookmark),
            Section("organized", "My trips", Icons.Default.EditNote),
            Section("past", "Archive", Icons.Default.MoveToInbox)
        )
    } else {
        listOf(
            Section("planned", "Planned", Icons.Default.FlightTakeoff),
            Section("organized", "My trips", Icons.Default.EditNote),
            Section("past", "Archive", Icons.Default.MoveToInbox)
        )

    }
}

@Composable
fun TravelTabBar(isSelf: Boolean, viewModel: UserProfileScreenViewModel) {
    val sections = getTravelTabSections(isSelf)
    val sectionSelected = viewModel.sectionSelected.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        sections.forEach { section ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable {
                    when (section.key) {
                        "organized" -> viewModel.viewOrganizedTrips()
                        "planned" -> viewModel.viewPlannedTrips()
                        "past" -> viewModel.viewPastTrips()
                        "saved" -> viewModel.viewSavedTrips()
                    }
                }
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (sectionSelected.value == section.key) Color(0xFFD8D4EC) else Color.Transparent
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = section.icon,
                        contentDescription = section.title,
                        tint = if (sectionSelected.value == section.key) Color.Black else Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (sectionSelected.value == section.key) Color.Black else Color.Gray
                    )
                )
            }
        }
    }
}


@Composable
fun TripSectionUser(
    navController: NavController,
    isSelf: Boolean,
    trip: Trip,
    portrait: Boolean,
    spotsLeft: Int,
    owned: Boolean,
    past: Boolean,
    modifier: Modifier,
    onClick: (Trip) -> Unit
) {
    Card(
        modifier = modifier
            .padding(horizontal = 2.dp)
            .clickable { navController.navigate("detail/${trip.id}")
    },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        val fontTitle = MaterialTheme.typography.titleMedium
        val fontBody = if (portrait) {
            MaterialTheme.typography.bodyMedium
        } else {
            MaterialTheme.typography.bodyLarge
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(195.dp)
                .padding(6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(120.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = trip.images[0],
                    contentDescription = "Trip image",
                    modifier = Modifier
                        .size(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Gray),
                    contentScale = ContentScale.Crop,
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .wrapContentWidth()
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .border(2.dp, Color.Black, RoundedCornerShape(8.dp))
                        .padding(6.dp)
                ) {
                    Text(text = "€ ${trip.price.first} - ${trip.price.second}")
                }
            }

            Spacer(Modifier.width(8.dp))
            Column(
                Modifier
                    .fillMaxHeight()
                    .weight(1f)
            ) {
                Text(
                    text = trip.title,
                    style = fontTitle,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )

                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(if (portrait) 3f else 4f)
                ) {
                    Spacer(Modifier.height(4.dp))

                    Spacer(Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.DateRange,
                            contentDescription = null,
                            Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        val start = trip.date.first.format(formatter)
                        val end = trip.date.second?.format(formatter)
                        Text("${start}${if (portrait) "\n" else " - "}${end}", style = fontBody)
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Place, contentDescription = null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(trip.countries.joinToString(", "), style = fontBody)
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Spots left: $spotsLeft", style = fontBody)
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (owned) {
                            if(isSelf){  //if the user is the logged one (HARDCODED Anna_Smith)
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = null,
                                    Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                if (trip.requests.isNotEmpty()) {
                                    Text("see new applications", style = fontBody, color = Color.Red)
                                } else {
                                    Text("No application to see", style = fontBody, color = Color.Black)
                                }

                                Spacer(Modifier.width(40.dp))

                                IconButton(
                                    onClick = { onClick(trip) },
                                    modifier = Modifier
                                        .padding(end = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = "Edit trip",
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                        } else if (past) {
                            if(isSelf){
                                TextButton(onClick = { navController.navigate("newReview/${trip.id}") }) {
                                    Text(
                                        text = "New review",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        } else {
                            // Maintain same row height and spacing with invisible placeholders
                            Spacer(Modifier.width(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("", style = fontBody, modifier = Modifier.weight(1f))
                            Spacer(Modifier.width(40.dp))
                            Spacer(Modifier.size(24.dp))
                        }
                    }
                }
            }
        }

    }
}

@Composable
fun DeleteDialog(
    navController: NavController,
    vm: UserProfileScreenViewModel = viewModel(factory = Factory),
) {
    val showDialog by vm.showDeleteDialog.collectAsState()

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { vm.closeDeleteDialog() },
            title = { Text(text = "Confirm Deletion") },
            text = { Text("Are you sure you want to delete this account?") },
            confirmButton = {
                Button(
                    onClick = {
                        vm.deleteUser()
                        navController.navigate("list")
                    }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    vm.closeDeleteDialog()
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Preview(name = "Portrait")
@Composable
fun ProfilePortraitPreview() {
    val userModel = UserModel()
    val tripModel = TripModel(userModel)
    val authModel = AuthRepository()

    val vm = UserProfileScreenViewModel(tripModel, userModel, authModel)
    //ShowProfile(vm, false)
}

@Preview(name = "Landscape", widthDp = 640, heightDp = 360)
@Composable
fun ProfileLandscapePreview() {
    val userModel = UserModel()
    val tripModel = TripModel(userModel)
    val authModel = AuthRepository()

    val vm = UserProfileScreenViewModel(tripModel, userModel, authModel)

    //ShowProfile(vm, false)
}

@Preview(name = "Portrait")
@Composable
fun MyProfilePortraitPreview() {
    val userModel = UserModel()
    val tripModel = TripModel(userModel)
    val authModel = AuthRepository()

    val vm = UserProfileScreenViewModel(tripModel, userModel, authModel)
    //ShowProfile(vm, true)
}

@Preview(name = "Landscape", widthDp = 640, heightDp = 360)
@Composable
fun MyProfileLandscapePreview() {
    val userModel = UserModel()
    val tripModel = TripModel(userModel)
    val authModel = AuthRepository()

    val vm = UserProfileScreenViewModel(tripModel, userModel, authModel)
    //ShowProfile(vm, true)
}

@Preview(name = "Tablet Portrait", widthDp = 800, heightDp = 1280)
@Composable
fun ProfileTabletPortraitPreview() {
    val userModel = UserModel()
    val tripModel = TripModel(userModel)
    val authModel = AuthRepository()

    val vm = UserProfileScreenViewModel(tripModel, userModel, authModel)
    //ShowProfile(vm, true)
}

@Preview(name = "Tablet Landscape", widthDp = 1280, heightDp = 800)
@Composable
fun ProfileTabletLandscapePreview() {
    val userModel = UserModel()
    val tripModel = TripModel(userModel)
    val authModel = AuthRepository()

    val vm = UserProfileScreenViewModel(tripModel, userModel, authModel)
    //ShowProfile(vm, true)
}

@Preview(name = "Tablet Portrait", widthDp = 800, heightDp = 1280)
@Composable
fun MyProfileTabletPortraitPreview() {
    val userModel = UserModel()
    val tripModel = TripModel(userModel)
    val authModel = AuthRepository()

    val vm = UserProfileScreenViewModel(tripModel, userModel, authModel)
    //ShowProfile(vm, true)
}

@Preview(name = "Tablet Landscape", widthDp = 1280, heightDp = 800)
@Composable
fun MyProfileTabletLandscapePreview() {
    val userModel = UserModel()
    val tripModel = TripModel(userModel)
    val authModel = AuthRepository()

    val vm = UserProfileScreenViewModel(tripModel, userModel, authModel)
    //ShowProfile(vm, true)
}
