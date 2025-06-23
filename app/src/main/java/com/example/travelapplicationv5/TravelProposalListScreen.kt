package com.example.travelapplicationv5

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material.icons.filled.Gif
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

// view model to implement the travel proposals list
class TravelProposalListScreenViewModel(val model: TripModel, val userModel: UserModel) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _expanded = MutableStateFlow(false)
    val expanded: StateFlow<Boolean> = _expanded

    private val _additionalFilters = MutableStateFlow(false)
    val additionalFilters: StateFlow<Boolean> = _additionalFilters

    val fixedSuggestions = listOf("Italy", "Norway", "Kenya", "USA", "Saudi Arabia", "Japan")
    private val _historySuggestions = MutableStateFlow<List<String>>(emptyList())
    val historySuggestions: StateFlow<List<String>> = _historySuggestions
    val suggestions: List<String>
        get() = historySuggestions.value + fixedSuggestions

    val chips = listOf("Adventure", "Relax", "Culture", "Luxury", "Family", "Plane", "Romantic")
    private val _selectedChips = MutableStateFlow<List<String>>(emptyList())
    val selectedChips: StateFlow<List<String>> = _selectedChips

    val groupDimensionChips = listOf("2-3 people", "4-10 people", "10+")
    private val _selectedGroupDimensions = MutableStateFlow<List<String>>(emptyList())
    val selectedGroupDimensions: StateFlow<List<String>> = _selectedGroupDimensions

    val priceChips = listOf("Max 1000€", "Max 3000€", "Max 5000€", "Over 5000€")
    private val _selectedPrices = MutableStateFlow<List<String>>(emptyList())
    val selectedPrices: StateFlow<List<String>> = _selectedPrices

    val durationChips = listOf("Weekend", "Week", "Two weeks", "More")
    private val _selectedDurations = MutableStateFlow<List<String>>(emptyList())
    val selectedDurations: StateFlow<List<String>> = _selectedDurations

    // List of all trips
    val allTravelProposalsList = MutableStateFlow(model.travelProposalsList.value)

    // List of all the future trips
    private val _travelProposalsList = MutableStateFlow<List<Trip>>(
        model.travelProposalsList.value.filter {
            it.date.first.isEqual(LocalDate.now()) || it.date.first.isAfter(
                LocalDate.now()
            )
        }
    )
    val travelProposalsList: StateFlow<List<Trip>> = _travelProposalsList

    private val _filteredTrips = MutableStateFlow(travelProposalsList.value)
    val filteredTrips: StateFlow<List<Trip>> = _filteredTrips

    init {
        viewModelScope.launch {
            model.travelProposalsList.collect { trips ->
                val futureTrips = trips.filter {
                    it.date.first.isEqual(LocalDate.now()) || it.date.first.isAfter(LocalDate.now())
                }
                _travelProposalsList.value = futureTrips    // Use the private mutable flow here
                _filteredTrips.value = futureTrips           // Use the private mutable flow here
            }
        }
    }


    fun addNewTrip(newTrip: Trip) {
        model.addNewTrip(newTrip)
        resetFilteredTrips()
    }

    fun getTripsNumber(): Int {
        return model.travelProposalsList.value.size
    }

    fun getFilteredTripsNumber(): Int {
        return _filteredTrips.value.size
    }

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        filterTrips()
    }

    fun onExpandedChange(isExpanded: Boolean) {
        _expanded.value = isExpanded
    }

    fun onAdditionalFiltersChange(shownAdditionalFilters: Boolean) {
        _additionalFilters.value = shownAdditionalFilters
    }

    fun onChipClick(chip: String) {
        val selChips = _selectedChips.value.toMutableList()
        if (selChips.contains(chip)) {
            selChips.remove(chip)
        } else {
            selChips.add(chip)
        }
        _selectedChips.value = selChips
        filterTrips()
    }

    fun onAdditionalChipClick(category: String, value: String) {
        val chipFlow = when (category) {
            "group" -> _selectedGroupDimensions
            "price" -> _selectedPrices
            "duration" -> _selectedDurations
            else -> return
        }

        val updated = chipFlow.value.toMutableList().apply {
            if (contains(value)) remove(value) else add(value)
        }
        chipFlow.value = updated
        filterTrips()
    }

    fun manageSearchbar(
        focusManager: FocusManager,
        keyboardController: SoftwareKeyboardController?
    ) {
        keyboardController?.hide()
        focusManager.clearFocus()
    }

    fun updateHistorySuggestions(query: String) {
        val currentHistory = _historySuggestions.value
        if (query.isNotEmpty() && !currentHistory.contains(query)) {
            val newHistory = currentHistory + query
            _historySuggestions.value = newHistory
        }
    }

    private fun filterTrips() {
        println("Filtered trips count: ${_filteredTrips.value.size}")  // debug log
        val result = mutableListOf<Trip>()
        if (query.value.isNotEmpty() ||
            selectedChips.value.isNotEmpty() ||
            selectedGroupDimensions.value.isNotEmpty() ||
            selectedPrices.value.isNotEmpty() ||
            selectedDurations.value.isNotEmpty()
        ) {
            travelProposalsList.value.forEach { trip ->
                var found = false
                if (query.value.isNotEmpty() && checkString(trip, query.value)) found = true

                selectedChips.value.forEach { chip ->
                    if (checkString(trip, chip)) found = true
                }

                selectedGroupDimensions.value.forEach { chip ->
                    val group = when (trip.spotsTotal) {
                        in 2..3 -> "2-3 people"
                        in 4..10 -> "4-10 people"
                        else -> "10+"
                    }
                    if (chip == group) found = true
                }

                selectedPrices.value.forEach { chip ->
                    val maxPrice = trip.price.second
                    val priceCategory = when {
                        maxPrice <= 1000 -> "Max 1000€"
                        maxPrice <= 3000 -> "Max 3000€"
                        maxPrice <= 5000 -> "Max 5000€"
                        else -> "Over 5000€"
                    }
                    if (chip == priceCategory) found = true
                }

                selectedDurations.value.forEach { chip ->
                    val days = ChronoUnit.DAYS.between(trip.date.first, trip.date.second).toInt()
                    val duration = when {
                        days <= 3 -> "Weekend"
                        days <= 7 -> "Week"
                        days <= 14 -> "Two weeks"
                        else -> "More"
                    }
                    if (chip == duration) found = true
                }

                if (found) result.add(trip)
            }
            _filteredTrips.value = result
        } else {
            resetFilteredTrips()
        }
    }

    private fun checkString(trip: Trip, value: String): Boolean {
        return if (trip.title.contains(value, ignoreCase = true) ||
            trip.description.contains(value, ignoreCase = true) ||
            trip.author.nickname.contains(value, ignoreCase = true) ||
            trip.countries.contains(value)
        ) true else false
    }

    fun resetFilteredTrips() {
        _query.value = ""
        _selectedChips.value = emptyList()

        _filteredTrips.value = travelProposalsList.value
        println("Reset trips count: ${_filteredTrips.value.size}")  // debug log
    }

    val savedTrips: StateFlow<List<Int>> = combine(
        userModel.usersList,
        userModel.loggedUser
    ) { users, id ->
        users.find { it.id == id }?.saved ?: emptyList()
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        emptyList()
    )

    fun addSavedTrip(tripId: Int){
        userModel.addSavedTrip(tripId)
    }

    fun removeSavedTrip(tripId: Int){
        userModel.removeSavedTrip(tripId)
    }
}


@Composable
fun TravelListScreen(
    navController: NavController,
    viewModel: TravelProposalListScreenViewModel = viewModel(factory = Factory),
    vmUserProfile: UserProfileScreenViewModel,
) {
    LaunchedEffect(Unit) {
        viewModel.resetFilteredTrips()
    }

    val isLoggedIn = vmUserProfile.isUserLoggedIn.collectAsState().value
    val activity = LocalActivity.current

    val filteredTrips = viewModel.filteredTrips.collectAsState().value
    println("TRIPS : " + filteredTrips)
    // Information's related to the portrait mode or not
    val configuration = LocalConfiguration.current
    val portrait = configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT
    val itemsPerRow = if (portrait) 2 else 3

    BackHandler {
        activity?.finish()
    }

    Scaffold(
        topBar = { TopBar(navController, false) },
        floatingActionButton = {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomEnd
            ) {
                FloatingActionButton(
                    onClick = {
                        if (!isLoggedIn) {
                            navController.navigate("login")
                        } else {
                            navController.navigate("handle/add")
                        }
                    },
                    modifier = Modifier
                        .padding(end = 16.dp, bottom = 16.dp)
                        .size(56.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                }
                /*
                //TO BE DELETED
                FloatingActionButton(
                    onClick = { navController.navigate("firebase test") },
                    modifier = Modifier
                        .padding(end = 16.dp, bottom = 88.dp) // sposto sopra il primo bottone
                        .size(56.dp)
                ) {
                    Icon(imageVector = Icons.Default.BugReport, contentDescription = "New Screen")
                }

                 */
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SearchBarCustom(viewModel)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(filteredTrips.chunked(itemsPerRow)) { rowTrips ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowTrips.forEach { trip ->
                            TripSection(
                                trip = trip,
                                portrait = portrait,
                                title = trip.title,
                                dateStart = trip.date.first.toString(),
                                dateEnd = trip.date.second.toString(),
                                countries = trip.countries,
                                spotsLeft = computeSpotsLeft(trip),
                                price = "${trip.price.first} - ${trip.price.second}",
                                img = trip.images[0],
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    navController.navigate("detail/${trip.id}")
                                },
                                viewModel = viewModel,
                                vmUserProfile = vmUserProfile
                            )
                        }
                        // Fill empty spaces if needed
                        if (rowTrips.size < itemsPerRow) {
                            repeat(itemsPerRow - rowTrips.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarCustom(viewModel: TravelProposalListScreenViewModel) {
    val expanded by viewModel.expanded.collectAsState()
    val additionalFilters by viewModel.additionalFilters.collectAsState()
    val query by viewModel.query.collectAsState()
    val selectedChips by viewModel.selectedChips.collectAsState()
    val chips = viewModel.chips
    val selectedGroupDimensionChips by viewModel.selectedGroupDimensions.collectAsState()
    val groupDimensionChips = viewModel.groupDimensionChips
    val selectedPriceChips by viewModel.selectedPrices.collectAsState()
    val priceChips = viewModel.priceChips
    val selectedDurationChip by viewModel.selectedDurations.collectAsState()
    val durationChips = viewModel.durationChips

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    SearchBar(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(Color.Transparent),
        expanded = expanded,
        onExpandedChange = { viewModel.onExpandedChange(it) },
        colors = SearchBarDefaults.colors(
            containerColor = MaterialTheme.colorScheme.background,
            dividerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(16.dp),
        windowInsets = WindowInsets(top = 0.dp),
        inputField = {
            TextField(
                value = query,
                onValueChange = { viewModel.onQueryChange(it) },
                placeholder = { Text("Search travels...") },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged {
                        viewModel.onExpandedChange(it.isFocused)
                    },
                keyboardActions = KeyboardActions(
                    onDone = {
                        viewModel.updateHistorySuggestions(query)
                        viewModel.onExpandedChange(false)
                        viewModel.manageSearchbar(focusManager, keyboardController)
                    }
                ),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Clear",
                    )
                },
                trailingIcon = {
                    IconButton(onClick = {
                        viewModel.onQueryChange("")
                        viewModel.onExpandedChange(false)
                        viewModel.manageSearchbar(focusManager, keyboardController)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                        )
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.onSurface,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 5.dp)
                .background(Color.Transparent)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(viewModel.suggestions) { suggestion ->
                val history = !viewModel.fixedSuggestions.contains(suggestion)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.onQueryChange(suggestion)
                            viewModel.onExpandedChange(false)
                            viewModel.manageSearchbar(focusManager, keyboardController)
                        }
                        .padding(vertical = 8.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (history) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Box(
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        modifier = Modifier.weight(9f),
                        text = suggestion,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }

    Spacer(Modifier.height(4.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(
            onClick = { viewModel.onAdditionalFiltersChange(!additionalFilters) },
            modifier = Modifier
                .size(36.dp)
                .align(Alignment.CenterVertically)
        ) {
            Icon(
                imageVector = if (!additionalFilters) Icons.Default.FilterList else Icons.Default.FilterListOff,
                contentDescription = "More filters"
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            chips.forEach {
                val isSelected = selectedChips.contains(it)

                AssistChip(
                    onClick = { viewModel.onChipClick(it) },
                    label = { Text(it) },
                    leadingIcon = {
                        if (isSelected) {
                            Icon(
                                modifier = Modifier.size(12.dp),
                                imageVector = Icons.Default.Done,
                                contentDescription = "Done",
                            )
                        }
                    },
                    border = AssistChipDefaults.assistChipBorder(
                        enabled = true,
                        borderColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    shape = RoundedCornerShape(8.dp),
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor =
                            if (isSelected) MaterialTheme.colorScheme.surfaceContainer
                            else Color.Transparent
                    )
                )
            }
        }
    }

    if (additionalFilters) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text("Group Size", style = MaterialTheme.typography.bodyMedium)
            FilterChipRow(groupDimensionChips, "group", selectedGroupDimensionChips, viewModel)

            Text("Price", style = MaterialTheme.typography.bodyMedium)
            FilterChipRow(priceChips, "price", selectedPriceChips, viewModel)

            Text("Duration", style = MaterialTheme.typography.bodyMedium)
            FilterChipRow(durationChips, "duration", selectedDurationChip, viewModel)
        }
    }


    if (query.isNotEmpty() || selectedChips.isNotEmpty()) {
        Spacer(Modifier.height(4.dp))
        Text(
            text = "${viewModel.getFilteredTripsNumber()} destinations found",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            textAlign = TextAlign.Start
        )
    }
}

@Composable
fun FilterChipRow(
    options: List<String>,
    category: String,
    selectedValues: List<String>,
    viewModel: TravelProposalListScreenViewModel
) {
    Row(
        Modifier
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { value ->
            val isSelected = selectedValues.contains(value)
            AssistChip(
                onClick = { viewModel.onAdditionalChipClick(category, value) },
                label = { Text(value) },
                leadingIcon = {
                    if (isSelected) {
                        Icon(
                            modifier = Modifier.size(12.dp),
                            imageVector = Icons.Default.Done,
                            contentDescription = "Done",
                        )
                    }
                },
                border = AssistChipDefaults.assistChipBorder(
                    enabled = true,
                    borderColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                shape = RoundedCornerShape(8.dp),
                colors = AssistChipDefaults.assistChipColors(
                    containerColor =
                        if (isSelected) MaterialTheme.colorScheme.surfaceContainer
                        else Color.Transparent
                )
            )
        }
    }
}

// Composable function to display a single travel proposal in a card
@Composable
fun TripSection(
    trip: Trip,
    portrait: Boolean,
    title: String,
    dateStart: String,
    dateEnd: String,
    countries: List<String>,
    spotsLeft: Int,
    price: String,
    img: String,
    modifier: Modifier,
    onClick: (Trip) -> Unit,   // lambda to call the composable without using navigation,
    viewModel: TravelProposalListScreenViewModel,
    vmUserProfile: UserProfileScreenViewModel
) {
    val savedTrips by viewModel.savedTrips.collectAsState()
    val isLoggedIn by vmUserProfile.isUserLoggedIn.collectAsState()
    val loggedUser by vmUserProfile.userId.collectAsState()

    Card(
        modifier = modifier
            .padding(horizontal = 2.dp)
            .clickable { onClick(trip) },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        val fontTitle = MaterialTheme.typography.titleMedium
        val fontBody = if (portrait) {
            MaterialTheme.typography.bodyMedium
        } else {
            MaterialTheme.typography.bodyLarge
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = img,
                    contentDescription = "Trip image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                if (isLoggedIn && trip.author.id!=loggedUser) {
                    val isSaved = savedTrips.any{ it == trip.id }
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 6.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                            )
                            .clickable {
                                if (isSaved) viewModel.removeSavedTrip(trip.id)
                                else viewModel.addSavedTrip(trip.id)
                            }
                            .padding(2.dp)
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Salva",
                            modifier = Modifier.size(30.dp),
                            tint = Color.Black
                        )
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                title,
                style = fontTitle,
                minLines = 2,
                maxLines = 2,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
        }
    }
}

