package com.example.travelapplicationv5

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.focus.FocusManager
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class OwnTravelsViewModel(val model: TripModel) : ViewModel() {

    private var owner = "Anna_Smith"

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _expanded = MutableStateFlow(false)
    val expanded: StateFlow<Boolean> = _expanded

    val fixedSuggestions = listOf("Italy", "Norway", "Kenya", "USA", "Saudi Arabia", "Japan")
    private val _historySuggestions = MutableStateFlow<List<String>>(emptyList())
    val historySuggestions: StateFlow<List<String>> = _historySuggestions
    val suggestions: List<String>
        get() = historySuggestions.value + fixedSuggestions

    val chips = listOf("Adventure", "Relax", "Culture", "Luxury", "Family", "Plane", "Romantic")
    private val _selectedChips = MutableStateFlow<List<String>>(emptyList())
    val selectedChips: StateFlow<List<String>> = _selectedChips

    val travelProposalsList = model.travelProposalsList  // List of all the trips

    private val _filteredTrips = MutableStateFlow(travelProposalsList.value.filter { it.author.nickname == owner })
    val filteredTrips: StateFlow<List<Trip>> = _filteredTrips

    init {
        _filteredTrips.value = travelProposalsList.value
            .filter { it.author.nickname == owner }
    }

    // HARDCODED USER
    // Suppose to use "Anna_Smith" as the logged user
    // Retrieve the travel proposal associated to the user Anna_Smith <------
    fun filterForSpecifiedUser(owner: String){
        _filteredTrips.value = travelProposalsList.value
            .filter { it.author.nickname == owner }
    }

    // Same functions as in the TravelProposalListScreenViewModel --> similar implementation of the user interface

    fun addNewTrip(newTrip: Trip) {
        model.addNewTrip(newTrip)
        resetFilteredTrips()
    }

    fun getTripsNumber(): Int {
        return model.travelProposalsList.value.filter { it.author.nickname == owner }.size
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

    fun manageSearchbar(focusManager: FocusManager, keyboardController: SoftwareKeyboardController?) {
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
        val result = mutableListOf<Trip>()
        if (query.value.isNotEmpty() || selectedChips.value.isNotEmpty()) {
            travelProposalsList.value.filter { it.author.nickname == owner }.forEach { trip ->
                var found = false
                if (query.value.isNotEmpty() && checkString(trip, query.value)) found = true
                selectedChips.value.forEach { chip ->
                    if (checkString(trip, chip)) found = true
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

        filterForSpecifiedUser(owner)
    }

}

@Composable
fun OwnTravelsList(
    navController: NavController,
    viewModel: OwnTravelsViewModel = viewModel(factory = Factory)
) {
    LaunchedEffect(Unit) {
        viewModel.resetFilteredTrips()
    }

    var hasFilteredTrips by remember { mutableStateOf(false) }

    if (!hasFilteredTrips) {
        viewModel.filterForSpecifiedUser("Anna_Smith")
        hasFilteredTrips = true
    }

    val filteredTrips = viewModel.filteredTrips.collectAsState().value

    // Information's related to the portrait mode or not
    val configuration = LocalConfiguration.current
    val portrait =
        configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT


    Column(Modifier.fillMaxSize()) {
        TopBar(navController)
        SearchBarCustom(viewModel)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            //for each trip display a TripSection (a Row with travel proposal informations)
            println(filteredTrips.size)
            items(filteredTrips) { trip ->
                Spacer(modifier = Modifier.height(8.dp))

                TripSectionOwn(
                    trip = trip,
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
                    onClick = {
                        navController.navigate("detail/${trip.id}")
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarCustom(viewModel: OwnTravelsViewModel) {
    val expanded by viewModel.expanded.collectAsState()
    val query by viewModel.query.collectAsState()
    val selectedChips by viewModel.selectedChips.collectAsState()
    val chips = viewModel.chips

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    SearchBar(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(Color.Transparent),
        expanded = expanded,
        onExpandedChange = { viewModel.onExpandedChange(it) },
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
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                ),
            )
        }
    ) {
        LazyColumn (
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
            .padding(horizontal = 16.dp)
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
                    borderColor = Color.LightGray
                ),
                shape = RoundedCornerShape(8.dp),
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (isSelected) Color.LightGray else Color.Transparent
                )
            )
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

// Composable function to display a single travel proposal in a card
@Composable
fun TripSectionOwn(
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
    onClick: (Trip) -> Unit
) {
    Card(
        modifier = modifier
            .padding(horizontal = 2.dp),
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
            ){
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
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))

                        if(trip.requests.isNotEmpty()){
                            Text("see new applications", style = fontBody, color = Color.Red)
                        } else{
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
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}