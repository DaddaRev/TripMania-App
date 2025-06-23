package com.example.travelapplicationv5

import android.app.Activity
import com.example.travelapplicationv5.Utility.allCountries
import android.app.DatePickerDialog
import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.BackHandler
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AssistChip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Outbox
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.travelapplicationv5.ui.theme.ButtonRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.collections.plus
import kotlin.math.roundToInt

// Data class to validate the mandatory fields of the travel proposal
data class TripValidationErrors(
    var image: String = "",
    val title: String = "",
    val description: String = "",
    val countries: String = "",
    val price: String = "",
    val date: String = "",
    val spots: String = "",
    val stops: String = "",
    val telegramLink: String = ""
)

// view model to implement the new travel proposal screen
class HandleTravelProposalScreenViewModel(val model: TripModel, val userModel :UserModel) : ViewModel() {
    //var tripSource = model.tripExample //editable or imported trip
    val _tripSource = MutableStateFlow(Trip())
    val tripSource: StateFlow<Trip> = _tripSource

    val isUserLoggedIn: StateFlow<Boolean> = userModel.isUserLoggedIn
    val userId: StateFlow<Int?> = userModel.loggedUser

    fun resetAllFields() {
        _tripSource.value = Trip()
        title = ""
        description = ""
        countries = ""
        selectedCountries = emptyList()
        spots = 0
        images.clear()
        onPriceRangeChange(0, 10000)
        _startDate = null
        _endDate = null
        stops.clear()
        _valErrors.value = TripValidationErrors()
        _valErrorsStep.value = emptyList()
        resetStep()
        telegramLink = ""
    }

    fun loadTripData(mode: String, tripId: Int) {
        if (mode=="add" || tripId == -1) {
            _mode.value = "add"
            resetAllFields()
        } else {
            _mode.value = "edit"
            val trip = model.travelProposalsList.value.find { it.id == tripId }!!
            _tripSource.value = trip
            title = trip.title
            description = trip.description
            countries = trip.countries.joinToString(", ")
            selectedCountries = trip.countries
            spots = trip.spotsTotal
            images.clear()
            images.addAll(trip.images)
            onPriceRangeChange(trip.price.first, trip.price.second)
            updateStartDate(trip.date.first)
            updateEndDate(trip.date.second ?: trip.date.first)
            stops.clear()
            stops.addAll(trip.itinerary)
            telegramLink = trip.telegramLink ?: ""
            _valErrors.value = TripValidationErrors()
            _valErrorsStep.value = emptyList()
            resetStep()
        }
    }

    // flag to maintain data of a trip when device is rotated
    var initialized by mutableStateOf(false)

    val _allTrips = MutableStateFlow<List<Trip>>(emptyList())
    val allTrips: StateFlow<List<Trip>> = _allTrips

    var showImportDialog by mutableStateOf(false)
    var selectedImportTrip by mutableStateOf<Trip?>(null)

    init {
        _allTrips.value = model.travelProposalsList.value
    }

    fun getTripsNumber(): Int {
        return model.travelProposalsList.value.size
    }

    // function to populate the import dialog list
    fun addNewTrip(newTrip: Trip) {
        _allTrips.value += newTrip
    }

    private val _mode = MutableStateFlow("add")
    val mode: StateFlow<String> = _mode.asStateFlow()

    private val _step = MutableStateFlow(0)
    val step: StateFlow<Int> = _step.asStateFlow()

    fun incrementStep() {
        _step.value = step.value + 1
    }

    fun decrementStep() {
        _step.value = step.value - 1
    }

    fun setStep(step: Int) {
        _step.value = step
    }

    fun resetStep() {
        _step.value = 0
    }

    private val _valErrors = MutableStateFlow(TripValidationErrors())
    val valErrors: StateFlow<TripValidationErrors> = _valErrors.asStateFlow()

    private val _valErrorsStep = MutableStateFlow(emptyList<Int>())
    val valErrorsStep: StateFlow<List<Int>> = _valErrorsStep.asStateFlow()

    fun addNewTravel(newTravel: Trip) {
        model.addNewTrip(newTravel)
    }

    //variables for the trip
    val isValid: Boolean
        get() = with(valErrors.value) {
            title.isBlank() &&
                    image.isBlank() &&
                    description.isBlank() &&
                    countries.isBlank() &&
                    spots.isBlank() &&
                    price.isBlank() &&
                    date.isBlank() &&
                    stops.isBlank()
        }

    private fun isValidStep() {
        val invalidSteps = mutableListOf<Int>()

        if (valErrors.value.image.isNotBlank()) invalidSteps.add(0)
        if (valErrors.value.title.isNotBlank() || valErrors.value.description.isNotBlank()) invalidSteps.add(
            1
        )
        if (valErrors.value.countries.isNotBlank() || valErrors.value.spots.isNotBlank()) invalidSteps.add(
            2
        )
        if (valErrors.value.price.isNotBlank() || valErrors.value.date.isNotBlank()) invalidSteps.add(
            3
        )
        if (valErrors.value.stops.isNotBlank()) invalidSteps.add(4)

        _valErrorsStep.value = invalidSteps
    }

    val images = mutableStateListOf<String>()
    var title by mutableStateOf("")
    var description by mutableStateOf("")
    var countries by mutableStateOf("")
    var selectedCountries by mutableStateOf<List<String>>(emptyList())
    var spots by mutableIntStateOf(0)
    val stops = mutableStateListOf<Stop>()
    private var _startDate by mutableStateOf<LocalDate?>(null)
    private var _endDate by mutableStateOf<LocalDate?>(null)
    var telegramLink by mutableStateOf("")

    //stops functions
    fun addStop(): Boolean {
        val start = _startDate ?: startDate
        val end = _endDate ?: endDate
        if (start == null || end == null) return false

        val lastStopDate = stops.lastOrNull()?.date ?: _startDate ?: startDate ?: LocalDate.now()

        stops.add(
            Stop(
                title = "",
                date = lastStopDate,
                location = "",
                free = true,
                activities = ""
            )
        )

        return true
    }

    fun removeStop(index: Int) {
        stops.removeAt(index)
    }

    fun updateStop(index: Int, updatedStop: Stop) {
        stops[index] = updatedStop
    }

    //price functions
    var minPrice by mutableStateOf("0")
        private set
    var maxPrice by mutableStateOf("10000")
        private set

    fun onPriceRangeChange(newMin: Int, newMax: Int) {
        minPrice = newMin.coerceIn(0, newMax).toString()
        maxPrice = newMax.coerceIn(newMin, 10000).toString()
    }

    //date functions
    val startDate: LocalDate?
        get() = _startDate

    val endDate: LocalDate?
        get() = _endDate


    fun updateStartDate(date: LocalDate) {
        _startDate = date
        if (_endDate == null || date.isAfter(_endDate)) {
            _endDate = date
        }
    }

    fun updateEndDate(date: LocalDate) {
        _endDate = date
        if (_startDate == null || date.isBefore(_startDate)) {
            _startDate = date
        }
    }

    fun addImage(uri: String) {
        images.add(uri)
    }

    fun removeImage(uri: String) {
        images.remove(uri)
    }

    //#############################VALIDATIONS#################################
    fun validateImages(): String {
        val error = when {
            images.size != 3 || images.any { it.isBlank() } -> "three images required"
            else -> ""
        }
        _valErrors.value = _valErrors.value.copy(image = error)
        return error
    }

    fun validateTitle(): String {
        val error = when {
            title.isBlank() -> "title required"
            else -> ""
        }
        _valErrors.value = _valErrors.value.copy(title = error)
        return error
    }

    fun validateDescription(): String {
        val error = when {
            description.isBlank() -> "description required"
            else -> ""
        }
        _valErrors.value = _valErrors.value.copy(description = error)
        return error
    }

    fun validateCountries(): String {
        val error = when {
            countries.isBlank() -> "At least one country required"
            else -> ""
        }
        _valErrors.value = _valErrors.value.copy(countries = error)
        return error
    }

    fun validateSpots(): String {
        val actualMembers = tripSource.value.requests.filter { it.status == RequestStatus.Accepted }
            .map { it -> it.companion + 1 }.sum()

        val error = when {
            spots < 1 || spots > 100 -> "Spots must be between 1 and 100"
            //mode.value == "edit" && spots > actualMembers -> "Minimum spots must be at least equal to confirmed participants: $actualMembers"
            else -> ""
        }
        _valErrors.value = _valErrors.value.copy(spots = error)
        return error
    }

    fun validatePrice(): String {
        val error = when {
            minPrice.toIntOrNull() == null || maxPrice.toIntOrNull() == null -> "Insert valid inputs"
            //implementation choice, to be seen if ok
            minPrice.toInt() % 100 != 0 || maxPrice.toInt() % 100 != 0 -> "values must be multiples of 100"
            minPrice.toInt() > maxPrice.toInt() -> "minimum can't be more than maximum"
            else -> ""
        }
        _valErrors.value = _valErrors.value.copy(price = error)
        return error

    }

    fun validateDates(): String {
        val error = when {
            _startDate == null || _endDate == null -> "Select both dates"
            _startDate!!.isAfter(_endDate!!) -> "end date must come after start date"
            _startDate!!.isBefore(LocalDate.now()) -> "Start date cannot be before today"
            else -> ""
        }
        _valErrors.value = _valErrors.value.copy(date = error)
        return error
    }

    fun validateStops() {
        val error = when {
            stops.isEmpty() -> "At least one stop is required"
            else -> ""
        }
        _valErrors.value = _valErrors.value.copy(stops = error)
    }
    fun validateTelegramLink(): String {
        val error = when {
            telegramLink.isNotBlank() && !telegramLink.startsWith("https://t.me/") ->
                "Telegram link should start with https://t.me/"
            else -> ""
        }
        _valErrors.value = _valErrors.value.copy(telegramLink = error)
        return error
    }

    fun validateAll() {
        validateImages()
        validateTitle()
        validateDescription()
        validateCountries()
        validateSpots()
        validatePrice()
        validateDates()
        validateStops()
        validateTelegramLink()
    }

    ///######################CREATION OF A NEW TRIP################################
    fun saveTrip(context: Context) {
        val minPrice = minPrice.toIntOrNull() ?: 0
        val maxPrice = maxPrice.toIntOrNull() ?: 0

        val id = if (mode.value != "edit") 0 else tripSource.value.id

        viewModelScope.launch {
            val uploadedImages = SupabaseHandler.uploadImagesList(
                uris = images.toList(),
                context = context,
                bucketName = SupabaseHandler.bucketTrips
            )

            if (mode.value == "edit") {
                val oldImages = tripSource.value.images
                val toDelete = oldImages.filterNot { it in uploadedImages }

                val pathPrefix = "https://${SupabaseHandler.supabaseUrl.removePrefix("https://")}/storage/v1/object/public/${SupabaseHandler.bucketTrips}/"
                val toDeletePaths = toDelete.mapNotNull { url ->
                    url.removePrefix(pathPrefix).takeIf { it.isNotEmpty() }
                }

                /*
                SupabaseHandler.deleteImages(
                    imagePaths = toDeletePaths,
                    bucketName = SupabaseHandler.bucketTrips
                )
                */
            }

            // returns the author users, if exists
            // If the code reaches this point I'm sure the user exist
            val authorUserProfile = userModel.getUserById(userId.value!!)
                ?: error("User not found")

            val newTrip = Trip(
                id = id,
                author = authorUserProfile,
                title = title,
                description = description,
                countries = countries.split(",").map { it.trim() },
                spotsTotal = spots,
                price = Pair(minPrice, maxPrice),
                images = uploadedImages,
                date = Pair(
                    startDate ?: LocalDate.now(),
                    endDate ?: LocalDate.now()
                ),
                itinerary = stops.toList(),
                requests = if (mode.value == "edit") tripSource.value.requests else emptyList(),
                reviews = if (mode.value == "edit") tripSource.value.reviews else emptyList(),
                memberReviews = if (mode.value == "edit") tripSource.value.memberReviews else emptyList(),
                telegramLink = telegramLink
            )

            if (mode.value != "edit") {
                addNewTravel(newTrip)
                println("Trip added successfully")
                println(newTrip)
            } else {
                model.updateTrip(newTrip)
            }
        }
    }

    fun confirm(context: Context): Boolean {
        //Trigger Basic Validation:
        validateAll()

        // Check if all validations pass
        if (isValid) {
            val indexedStops = stops.withIndex().toList()
            val sortedStops = indexedStops.sortedWith(
                compareBy<IndexedValue<Stop>> { it.value.date }.thenBy { it.index }
            ).map { it.value }

            stops.clear()
            stops.addAll(sortedStops)

            saveTrip(context)
            return true

        } else {
            if (mode.value == "add") {
                isValidStep()
                _step.value = valErrorsStep.value[0]
            }
            println("Validation Error")
            return false
        }
    }
}

@Composable
fun NewTravelScreen(
    navController: NavController,
    mode: String,
    selectedTrip: Int,
    viewModel: HandleTravelProposalScreenViewModel = viewModel(factory = Factory)
) {
    LaunchedEffect(Unit) {
        if (!viewModel.initialized) {
            viewModel.loadTripData(mode, selectedTrip)
            viewModel.initialized = true
        }
    }

    BackHandler {
        viewModel.initialized = false
        navController.popBackStack()
    }

    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Column(Modifier.fillMaxSize()) {
        TopBar(navController, initializeHandle = true, viewModelHandle = viewModel)
        Spacer(modifier = Modifier.height(10.dp))
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text("Trip Informations", style = MaterialTheme.typography.titleLarge)

                if (mode != "edit") {
                    if (!isLandscape) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Import(viewModel)
                        }
                        Spacer(Modifier.height(25.dp))
                        Wizard(navController, viewModel)
                    } else {
                        Row(modifier = Modifier.fillMaxSize()) {
                            Column(
                                modifier = Modifier
                                    .weight(0.8f)
                                    .fillMaxHeight(),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Import(viewModel)
                                Spacer(Modifier.height(40.dp))
                            }
                            Column(modifier = Modifier.weight(2f)) {
                                Wizard(navController, viewModel)
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(20.dp))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        item {
                            Column {
                                val validationErrors by viewModel.valErrors.collectAsState()
                                ImageUpload(viewModel)
                                //TITLE
                                Spacer(Modifier.height(6.dp))
                                Title(viewModel)
                                //DESCRIPTION
                                Spacer(Modifier.height(6.dp))
                                Description(viewModel)
                                //TELEGRAM
                                Spacer(Modifier.height(6.dp))
                                TelegramLinkField(viewModel)
                                //COUNTRIES
                                Spacer(Modifier.height(6.dp))
                                Countries(viewModel)
                                //SPOTS
                                Spacer(Modifier.height(6.dp))
                                SpotsEdit(onSpotsChange = { viewModel.spots = it }, viewModel)
                                //PRICE
                                Spacer(Modifier.height(6.dp))
                                PriceRangeSection(viewModel)
                                //DATES
                                Spacer(Modifier.height(6.dp))
                                DateSelectionSection(viewModel)
                                //STOPS
                                if (viewModel.startDate != null) {
                                    Spacer(Modifier.height(6.dp))
                                    StopsSection(viewModel)
                                }
                                Spacer(Modifier.height(10.dp))
                                //CHOICE BUTTONS
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // Cancel Button
                                    Button(
                                        onClick = {
                                            navController.popBackStack()
                                            viewModel.initialized = false
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.elevatedButtonColors(
                                            containerColor = ButtonRed,
                                            contentColor = MaterialTheme.colorScheme.onSecondary
                                        ),
                                    ) {
                                        Text("Cancel")
                                    }
                                    // Confirm Button
                                    Button(
                                        //"turn back in the navigation"
                                        onClick = {
                                            if (viewModel.confirm(context) == true) {
                                                navController.popBackStack()
                                                viewModel.initialized = false
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.elevatedButtonColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        ),
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
    }
}

@Composable
private fun Wizard(
    navController: NavController,
    viewModel: HandleTravelProposalScreenViewModel,
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val step by viewModel.step.collectAsState()
    val stepDescription = listOf(
        "Upload three meaningful images that capture the location and planned activities",
        "Set the title and a brief description to introduce your journey",
        "Select the countries to visit and the number of people you'd like to share the experience with",
        "Specify the price range and travel period to your budget and schedule",
        "Provide a detailed itinerary structured by stops"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        StepIndicator(viewModel)
        Spacer(modifier = Modifier.height(if (!isLandscape) 12.dp else 5.dp))

        Text(
            "Step ${step + 1}",
            fontSize = if (!isLandscape) 22.sp else 15.sp,
            fontWeight = FontWeight.Bold
        )
        if (!isLandscape) {
            Text(
                text = stepDescription[step],
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
        }
        Spacer(modifier = Modifier.height(if (!isLandscape) 15.dp else 5.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = MaterialTheme.shapes.large
        ) {
            AnimatedContent(
                targetState = step,
                label = "",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (!isLandscape) 16.dp else 10.dp)
            ) { currentStep ->
                when (currentStep) {
                    0 -> StepOne(viewModel)
                    1 -> StepTwo(viewModel)
                    2 -> StepThree(viewModel)
                    3 -> StepFour(viewModel)
                    4 -> StepFive(viewModel)
                }
            }
        }

        Spacer(modifier = Modifier.height(if (!isLandscape) 15.dp else 5.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.Start) {
                if (step > 0) {
                    Button(
                        onClick = { viewModel.decrementStep() },
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                    ) {
                        Text("Back")
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        navController.popBackStack()
                        viewModel.initialized = false
                    },
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = ButtonRed,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ),
                ) {
                    Text("Cancel")
                }

            }

            if (step < 4) {
                Button(
                    onClick = { viewModel.incrementStep() },
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                ) {
                    Text("Next")
                }
            } else {
                Button(
                    onClick = {
                        if (viewModel.confirm(context) == true) {
                            navController.popBackStack()
                            viewModel.initialized = false
                        }
                    },
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                ) {
                    Text("Confirm")
                }
            }
        }
        Spacer(modifier = Modifier.height(if (!isLandscape) 60.dp else 5.dp))
    }
}

@Composable
fun isKeyboardOpen(): Boolean {
    return WindowInsets.ime.getBottom(LocalDensity.current) > 0
}

@Composable
private fun StepOne(viewModel: HandleTravelProposalScreenViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        ImageUpload(viewModel)
    }
}

@Composable
private fun StepTwo(viewModel: HandleTravelProposalScreenViewModel) {
    val keyboardOpen = isKeyboardOpen()

    LazyColumn(
        modifier = Modifier.fillMaxWidth()
    ) {
        item {
            Title(viewModel)
            Spacer(Modifier.height(20.dp))
            Description(viewModel)
            Spacer(Modifier.height(20.dp))
            TelegramLinkField(viewModel)
            if (keyboardOpen) {
                Spacer(Modifier.height(250.dp))
            }
        }
    }
}

@Composable
private fun StepThree(viewModel: HandleTravelProposalScreenViewModel) {
    val keyboardOpen = isKeyboardOpen()

    LazyColumn(
        modifier = Modifier.fillMaxWidth()
    ) {
        item {
            Countries(viewModel)
            Spacer(Modifier.height(20.dp))
            SpotsAdd(onSpotsChange = { viewModel.spots = it }, viewModel)
            if (keyboardOpen) {
                Spacer(Modifier.height(250.dp))
            }
        }
    }
}

@Composable
private fun StepFour(viewModel: HandleTravelProposalScreenViewModel) {
    LazyColumn {
        item {
            PriceRangeSection(viewModel)
            Spacer(Modifier.height(20.dp))
            DateSelectionSection(viewModel)
        }
    }
}

@Composable
private fun StepFive(viewModel: HandleTravelProposalScreenViewModel) {
    val listState = rememberLazyListState()
    val stopsCount = viewModel.stops.size

    LaunchedEffect(stopsCount) {
        if (stopsCount > 0) {
            listState.animateScrollToItem(stopsCount - 1)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxWidth()
    ) {
        item {
            StopsSection(viewModel)
        }
    }
}


@Composable
private fun Import(viewModel: HandleTravelProposalScreenViewModel) {
    Text(
        "Load from an existing trip",
        style = MaterialTheme.typography.bodyLarge
    )
    //Button to import previous travel
    importButton(viewModel)
}

@Composable
private fun ImageUpload(viewModel: HandleTravelProposalScreenViewModel) {
    val validationErrors by viewModel.valErrors.collectAsState()
    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) {
        uris: List<Uri> ->
        val currentImages = viewModel.images
        val remainingSlots = 3 - currentImages.size

        val newUris = uris.take(remainingSlots).map { it.toString() }
        newUris.forEach { viewModel.addImage(it) }
    }

    Text("Three images", style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(3.dp))
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        println(this.maxWidth)
        val boxWidth = (maxWidth - 16.dp) / 3
        val imageHeight = boxWidth * 1.2f
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            viewModel.images.forEach { uri ->

                Box(
                    modifier = Modifier
                        .width(boxWidth)
                        .height(imageHeight)
                ) {
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )

                    IconButton(
                        onClick = { viewModel.removeImage(uri) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .background(Color.White, CircleShape)
                            .size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove",
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (viewModel.images.size < 3) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                        .clickable { imageLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Image",
                        tint = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }
    if (validationErrors.image.isNotBlank()) {
        Text(
            text = validationErrors.image,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun Title(viewModel: HandleTravelProposalScreenViewModel) {
    val validationErrors by viewModel.valErrors.collectAsState()

    Text("Title", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(3.dp))
    OutlinedTextField(
        value = viewModel.title,
        onValueChange = { viewModel.title = it },
        label = { Text("Title") },
        isError = validationErrors.title.isNotBlank(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        modifier = Modifier.fillMaxWidth()

    )
    if (validationErrors.title.isNotBlank()) {
        Text(
            text = validationErrors.title,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun Description(viewModel: HandleTravelProposalScreenViewModel) {
    val validationErrors by viewModel.valErrors.collectAsState()
    Text("Description", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(3.dp))
    OutlinedTextField(
        value = viewModel.description,
        onValueChange = { viewModel.description = it },
        label = { Text("Description") },
        placeholder = { Text("A nice vacation in the mountains...") },
        isError = validationErrors.description.isNotBlank(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        modifier = Modifier.fillMaxWidth()
    )
    if (validationErrors.description.isNotBlank()) {
        Text(
            text = validationErrors.description,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Countries(viewModel: HandleTravelProposalScreenViewModel) {
    val validationErrors by viewModel.valErrors.collectAsState()
    var suggestions by remember { mutableStateOf(listOf<String>()) }
    val selectedCountries = viewModel.selectedCountries
    var inputText by remember { mutableStateOf(String()) }

    Text("Countries", style = MaterialTheme.typography.titleMedium)

    OutlinedTextField(
        value = inputText.toString(),
        onValueChange = { newValue ->
            inputText = newValue

            val entered = newValue.trim()

            suggestions = if (entered.isNotEmpty()) {
                allCountries.filter {
                    it.split(" ").any { word ->
                        word.startsWith(entered, ignoreCase = true)
                    } && it !in selectedCountries
                }
            } else emptyList()
        },
        label = { Text("Country") },
        placeholder = { Text("Start typing a country...") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        modifier = Modifier.fillMaxWidth()
    )
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        selectedCountries.forEach { selectedCountry ->
            AssistChip(
                onClick = {},
                label = {
                    Text(
                        text = selectedCountry
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove",
                        modifier = Modifier
                            .size(18.dp)
                            .clickable {
                                viewModel.selectedCountries =
                                    selectedCountries.filter { it != selectedCountry }
                                viewModel.countries = viewModel.selectedCountries.joinToString(", ")
                            }
                    )
                }
            )
        }
    }

    suggestions.forEach { country ->
        Text(
            text = country,
            modifier = Modifier
                .clickable {
                    if (country !in selectedCountries) {
                        viewModel.selectedCountries = selectedCountries + country
                    }
                    inputText = ""
                    suggestions = emptyList()
                    viewModel.countries = viewModel.selectedCountries.joinToString(", ")
                }
                .padding(8.dp)
                .fillMaxWidth()
        )
    }

    if (validationErrors.countries.isNotBlank()) {
        Text(
            text = validationErrors.countries,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
        )
    }
}


@Composable
private fun TelegramLinkField(viewModel: HandleTravelProposalScreenViewModel) {
    val validationErrors by viewModel.valErrors.collectAsState()

    Text("Telegram Group Link (optional)", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(3.dp))
    OutlinedTextField(
        value = viewModel.telegramLink,
        onValueChange = { viewModel.telegramLink = it },
        label = { Text("https://t.me/yourgroup") },
        isError = validationErrors.telegramLink.isNotBlank(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
        modifier = Modifier.fillMaxWidth()
    )
    if (validationErrors.telegramLink.isNotBlank()) {
        Text(
            text = validationErrors.telegramLink,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun StepIndicator(viewModel: HandleTravelProposalScreenViewModel) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val step by viewModel.step.collectAsState()
    val errorStep by viewModel.valErrorsStep.collectAsState()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        for (i in 0..4) {
            Box(
                modifier = Modifier
                    .size(if (!isLandscape) 32.dp else 15.dp)
                    .clickable(enabled = i != step) {
                        viewModel.setStep(i)
                    }
                    .background(
                        color =
                            if (i == step) MaterialTheme.colorScheme.primaryContainer
                            else if (errorStep.contains(i))
                                ButtonRed
                            else
                                Color.LightGray,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("${i + 1}", color = Color.White)
            }
        }
    }
}


@Composable
private fun PriceRangeSection(viewModel: HandleTravelProposalScreenViewModel) {
    val minPriceValue = viewModel.minPrice.toIntOrNull() ?: 0
    val maxPriceValue = viewModel.maxPrice.toIntOrNull() ?: 10000
    val roundedMin = (minPriceValue / 100) * 100
    val roundedMax = (maxPriceValue / 100) * 100

    Column {
        Text("Price range", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(3.dp))

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Min: ${roundedMin}€", style = MaterialTheme.typography.bodyMedium)
                Text("Max: ${roundedMax}€", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(8.dp))

            RangeSlider(
                value = roundedMin.toFloat()..roundedMax.toFloat(),
                onValueChange = { range ->
                    val newMin = (range.start / 100).roundToInt() * 100
                    val newMax = (range.endInclusive / 100).roundToInt() * 100
                    viewModel.onPriceRangeChange(newMin, newMax)
                },
                valueRange = 0f..10000f,
                steps = 99,
                modifier = Modifier.fillMaxWidth()
            )
            //use also a text input???
            /*
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedTextField(
                    value = roundedMin.toString(),
                    onValueChange = {
                        it.toIntOrNull()?.let { value ->
                            viewModel.onPriceRangeChange(value, roundedMax)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(120.dp)
                )

                OutlinedTextField(
                    value = roundedMax.toString(),
                    onValueChange = {
                        it.toIntOrNull()?.let { value ->
                            viewModel.onPriceRangeChange(roundedMin, value)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(120.dp)
                )
            }
            */

        }
    }
}

@Composable
private fun DateSelectionSection(viewModel: HandleTravelProposalScreenViewModel) {
    val context = LocalContext.current
    var showStart by remember { mutableStateOf(false) }
    var showEnd by remember { mutableStateOf(false) }
    val validationErrors by viewModel.valErrors.collectAsState()

    Column {
        Spacer(modifier = Modifier.height(16.dp))
        Text("Trip Dates", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        Column {
            Button(
                onClick = { showStart = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
            ) {
                Text(
                    viewModel.startDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        ?: "Select start date"
                )
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { showEnd = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
            ) {
                Text(
                    viewModel.endDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        ?: "Select end date"
                )
            }
            if (validationErrors.date.isNotBlank()) {
                Text(
                    text = validationErrors.date,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
    if (showStart == true) {
        val currentDate = viewModel.startDate ?: LocalDate.now()
        DatePickerDialog(
            context,
            { _, year, month, day ->
                viewModel.updateStartDate(LocalDate.of(year, month + 1, day))
                showStart = false
            },
            currentDate.year,
            currentDate.monthValue - 1,
            currentDate.dayOfMonth
        ).show()
    }
    if (showEnd == true) {
        val currentDate = viewModel.endDate ?: LocalDate.now()
        DatePickerDialog(
            context,
            { _, year, month, day ->
                viewModel.updateEndDate(LocalDate.of(year, month + 1, day))
                showEnd = false
            },
            currentDate.year,
            currentDate.monthValue - 1,
            currentDate.dayOfMonth
        ).show()
    }
}

@Composable
fun StopsSection(vm: HandleTravelProposalScreenViewModel) {
    fun isStopEmpty(stop: Stop): Boolean {
        return stop.title.isBlank() && stop.location.isBlank() && stop.activities.isBlank()
    }

    var showEdit by remember { mutableStateOf(false) }
    var selectedStopIndex by remember { mutableIntStateOf(-1) }
    val validationErrors by vm.valErrors.collectAsState()
    val context = LocalContext.current

    Column {
        Text("Stops", style = MaterialTheme.typography.titleMedium)

        Column {
            vm.stops.forEachIndexed { index, stop ->
                StopItem(
                    stop = stop,
                    onEditClick = {
                        selectedStopIndex = index
                        showEdit = true
                    },
                    onDeleteClick = { vm.removeStop(index) }
                )
            }
        }

        Button(onClick = {
            if (vm.addStop()) {
                selectedStopIndex = vm.stops.lastIndex
                showEdit = true
            } else {
                Toast.makeText(
                    context,
                    "Please set start and end date before adding a stop",
                    Toast.LENGTH_LONG
                ).show()
            }
        },
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
        ) {
            Text("Add Stop")
        }
    }
    if (showEdit && selectedStopIndex >= 0) {
        EditStop(
            stop = vm.stops[selectedStopIndex],
            onDismiss = {
                val stop = vm.stops.getOrNull(selectedStopIndex)
                if (stop != null && isStopEmpty(stop)) {
                    vm.removeStop(selectedStopIndex)
                }
                showEdit = false
            },
            onSave = { updatedStop ->
                vm.updateStop(selectedStopIndex, updatedStop)
                showEdit = false
            },
            vm = vm
        )
    }
    if (validationErrors.stops.isNotBlank()) {
        Text(
            text = validationErrors.stops,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun StopItem(stop: Stop, onEditClick: () -> Unit, onDeleteClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.LightGray,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stop.title, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(6.dp))
            Text(
                stop.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(6.dp))
            Text("Location: " + stop.location, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(6.dp))
            Text("Activities: " + stop.activities, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(6.dp))
            Text(
                if (stop.free) "Free" else "Not free",
                style = MaterialTheme.typography.bodyMedium
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

@Composable
fun EditStop(
    stop: Stop,
    onDismiss: () -> Unit,
    onSave: (Stop) -> Unit,
    vm: HandleTravelProposalScreenViewModel
) {
    var title by remember { mutableStateOf(stop.title) }
    var date by remember { mutableStateOf(stop.date) }
    var location by remember { mutableStateOf(stop.location) }
    var free by remember { mutableStateOf(stop.free) }
    var activities by remember { mutableStateOf(stop.activities) }
    var pickDate by remember { mutableStateOf(false) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Stop") },
        containerColor = MaterialTheme.colorScheme.background,
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    placeholder = { Text("Stop title") }
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    placeholder = { Text("e.g. Uffizi, Florence, Italy") }
                )
                Spacer(Modifier.height(6.dp))
                Button(
                    onClick = { pickDate = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                ) {
                    Text(
                        date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                            ?: "Select start date"
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = free,
                        onCheckedChange = { free = it }
                    )
                    Text("Free")
                }

                OutlinedTextField(
                    value = activities,
                    onValueChange = { activities = it },
                    label = { Text("Activities") },
                    placeholder = { Text("Suggested activities (e.g., climbing, skiing attraction seeking, exploring the city….)") },
                    minLines = 3,
                    maxLines = 5,
                )
            }
        },
        confirmButton = {
            val isInputValid = title.trim().isNotEmpty() && location.trim().isNotEmpty()

            Button(onClick = {
                val coords = UtilityMaps.getLatLngFromLocationName(context, location, Locale.ENGLISH)
                    ?: UtilityMaps.getLatLngFromLocationName(context, location, Locale("it"))
                onSave(
                    stop.copy(
                        title = title,
                        date = date,
                        location = location,
                        latitude = coords?.first,
                        longitude = coords?.second,
                        free = free,
                        activities = activities
                    )
                )
            },
                enabled = isInputValid,
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = ButtonRed,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
            ) {
                Text("Cancel")
            }
        }
    )
    if (pickDate == true) {
        DatePickerDialog(
            context,
            { _, year, month, day ->
                if (LocalDate.of(year, month + 1, day).isBefore(vm.startDate) || LocalDate.of(
                        year,
                        month + 1,
                        day
                    ).isAfter(vm.endDate)
                ) {
                    Toast.makeText(
                        context,
                        "Please select a date between ${vm.startDate} and ${vm.endDate}.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    date = LocalDate.of(year, month + 1, day)
                }
                pickDate = false
            },
            date.year,
            date.monthValue - 1,
            date.dayOfMonth
        ).show()
    }
}

@Composable
fun SpotsAdd(onSpotsChange: (Int) -> Unit, vm: HandleTravelProposalScreenViewModel) {
    val validationErrors by vm.valErrors.collectAsState()

    Text("Spots", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.width(20.dp))
    Row(
        Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SpotsSelector(onSpotsChange, vm)
    }
    if (validationErrors.spots.isNotBlank()) {
        Text(
            text = validationErrors.spots,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun SpotsEdit(onSpotsChange: (Int) -> Unit, vm: HandleTravelProposalScreenViewModel) {
    val validationErrors by vm.valErrors.collectAsState()

    Row(
        Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Spots: ", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.width(20.dp))
        SpotsSelector(onSpotsChange, vm)
    }
    if (validationErrors.spots.isNotBlank()) {
        Text(
            text = validationErrors.spots,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun SpotsSelector(onSpotsChange: (Int) -> Unit, vm: HandleTravelProposalScreenViewModel) {
    val minSpots: Int = 1
    val maxSpots: Int = 100

    IconButton(
        onClick = {
            if (vm.spots > minSpots) {
                onSpotsChange(vm.spots - 1)
            }
        }
    ) {
        Icon(
            imageVector = Icons.Default.ArrowBackIosNew,
            contentDescription = "Decrement",
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape // Make it circular
                )
                .padding(12.dp),
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
    Text(
        text = vm.spots.toString(),
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
    IconButton(
        onClick = {
            if (vm.spots < maxSpots) {
                onSpotsChange(vm.spots + 1)
            }
        }
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = "Increment",
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape // Make it circular
                )
                .padding(12.dp),
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
fun importButton(vm: HandleTravelProposalScreenViewModel) {
    val configuration = LocalConfiguration.current
    val portrait =
        configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT

    //var selectedTrip by remember { mutableStateOf<Trip?>(null) }


    //var showDialog by remember { mutableStateOf(false) }
    val selectedTrip = vm.selectedImportTrip
    //val showDialog = vm.showImportDialog
    IconButton(
        onClick = {
            vm.showImportDialog = true
            vm._allTrips.value = vm.model.travelProposalsList.value
        },
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
        Icon(
            imageVector = Icons.Default.Outbox,
            contentDescription = "import",
            modifier = Modifier.size(28.dp),
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }

    if (vm.showImportDialog) {
        val importTrips = vm.allTrips.collectAsState().value
        AlertDialog(
            containerColor = MaterialTheme.colorScheme.background,
            onDismissRequest = { vm.showImportDialog = false },
            title = { Text("Choose an existing trip") },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp)
                ) {
                    println(importTrips.size)
                    items(importTrips) { trip ->
                        Spacer(modifier = Modifier.height(8.dp))

                        TripSectionForImport(
                            portrait = portrait,
                            title = trip.title,
                            dateStart = trip.date.first.toString(),
                            dateEnd = trip.date.second.toString(),
                            countries = trip.countries,
                            spotsLeft = computeSpotsLeft(trip),
                            price = "${trip.price.first} - ${trip.price.second}",
                            img = trip.images[0],
                            modifier = Modifier
                                .fillMaxWidth(),
                            selected = trip == selectedTrip,
                            onClick = { vm.selectedImportTrip = trip },


                            )
                    }
                    item {
                        Spacer(Modifier.height(20.dp))
                    }
                }


            },
            confirmButton = {
                Button(onClick = {

                    selectedTrip?.let { trip ->
                        vm.title = trip.title
                        vm.description = trip.description
                        vm.countries = trip.countries.joinToString(", ")
                        vm.selectedCountries = trip.countries
                        vm.spots = trip.spotsTotal
                        vm.images.clear()
                        vm.images.addAll(trip.images.map { it })
                        vm.onPriceRangeChange(trip.price.first, trip.price.second)
                        vm.updateStartDate(trip.date.first)
                        vm.updateEndDate(trip.date.second ?: trip.date.first)
                        vm.stops.clear()
                        vm.stops.addAll(trip.itinerary)
                    }

                    vm.resetStep()

                    vm.showImportDialog = false
                },
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                ) {
                    Text("Import")
                }
            },
            dismissButton = {
                Button(onClick = {
                    vm.showImportDialog = false
                },
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = ButtonRed,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ),) {
                    Text("Cancel")
                }
            }
        )
    }
}


@Composable
fun TripSectionForImport(
    portrait: Boolean,
    title: String,
    dateStart: String,
    dateEnd: String,
    countries: List<String>,
    spotsLeft: Int,
    price: String,
    img: String,
    modifier: Modifier,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier
            .padding(horizontal = 2.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface
        )
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
                    model = img,
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
                    Text(text = "€ $price")
                }
            }

            Spacer(Modifier.width(8.dp))
            Column(
                Modifier
                    .fillMaxHeight()
                    .weight(1f)
            ) {
                Text(
                    text = title,
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
                        Text("$dateStart${if (portrait) "\n" else " - "}$dateEnd", style = fontBody)
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Place, contentDescription = null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(countries.joinToString(", "), style = fontBody)
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Spots left: $spotsLeft", style = fontBody)
                    }
                    /*
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                    }
                    */
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(navController: NavController, show: Boolean = true, initializeHandle: Boolean = false, initializeReview: Boolean = false, viewModelHandle: HandleTravelProposalScreenViewModel = viewModel(factory = Factory), viewModelReview: ReviewTripScreenViewModel = viewModel(factory = Factory)) {

    val isLogged by viewModelHandle.isUserLoggedIn.collectAsState()
    val userId by viewModelHandle.userId.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val backPressedRecently = remember { mutableStateOf(false) }
    val listPressedRecently = remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.app_name),
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.clickable {
                        if (!listPressedRecently.value) {
                            listPressedRecently.value = true

                            coroutineScope.launch {
                                delay(500)
                                listPressedRecently.value = false
                            }

                            navController.navigate("list") {
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        navigationIcon = {
            if (show) {
                IconButton(onClick = {
                    if (!backPressedRecently.value) {
                        backPressedRecently.value = true

                        if (initializeHandle) {
                            viewModelHandle.initialized = false
                        } else if (initializeReview) {
                            viewModelReview.initialized = false
                            viewModelReview.resetAllFields()
                        }
                        navController.popBackStack()

                        coroutineScope.launch {
                            delay(500)
                            backPressedRecently.value = false
                        }
                    }
                }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        },
        actions = {
            if (isLogged) {
                IconButton(onClick = {
                    if (isLogged) {
                        navController.navigate("notifications") {
                            launchSingleTop = true
                        }
                    } else {
                        navController.navigate("login") {
                            launchSingleTop = true
                        }
                    }
                }) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                }
            }
            IconButton(onClick = {
                if (isLogged){
                    navController.navigate("profile/${userId}") {
                        launchSingleTop = true
                    }
                }else{
                    navController.navigate("login") {
                        launchSingleTop = true
                    }
                }
            }) {
                Icon(Icons.Filled.AccountCircle, contentDescription = "Profile")
            }
        },
    )
}