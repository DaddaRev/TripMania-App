package com.example.travelapplicationv5

import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.Int
import kotlin.collections.plus
import androidx.compose.material.icons.filled.Cancel
import kotlinx.coroutines.launch


// Data class to validate the mandatory fields of the review
data class ReviewValidationErrors(
    val description: String = "",
    val rating: String = "",
    val tips: String = ""
)

data class MemberReviewValidationErrors(
    val ratings: Map<Int, Boolean> = emptyMap(),  // userId -> error
    val comments: Map<Int, Boolean> = emptyMap()  // userId -> error
)

enum class ReviewMemberState {
    NotAdded,
    Editing,
    Saved
}

// view model to implement the new review screen
class ReviewTripScreenViewModel(val model: TripModel, val userModel: UserModel, val notificationModel: NotificationModel) : ViewModel() {

    private val _tripToShow = MutableStateFlow<Trip>(Trip())
    val tripToShow: StateFlow<Trip> = _tripToShow.asStateFlow()

    // flag to maintain data of a trip when device is rotated
    var initialized by mutableStateOf(false)

    private val _valErrors = MutableStateFlow(ReviewValidationErrors())
    val valErrors: StateFlow<ReviewValidationErrors> = _valErrors.asStateFlow()

    private val _valErrorsMembers = MutableStateFlow(MemberReviewValidationErrors())
    val valErrorsMembers: StateFlow<MemberReviewValidationErrors> = _valErrorsMembers.asStateFlow()

    //variables for the review

    val isValid: Boolean
        get() = with(valErrors.value) {
            description.isBlank()
                    && rating.isEmpty()
                    && tips.isBlank()
        }

    var description by mutableStateOf("")
    var rating by mutableIntStateOf(0)
    var tips by mutableStateOf("")
    var images = mutableStateListOf<String>()

    //#############################VALIDATIONS REVIEW#################################
    /*
        fun validateImages(): String {
            val error = when {
                images.size != 3 || images.any { it.isBlank() } -> "three images required"
                else -> ""
            }
            _valErrors.value = _valErrors.value.copy(image = error)
            return error
        }
     */
    fun validateDescription(): String {
        val error = when {
            description.isBlank() -> "description required"
            else -> ""
        }
        _valErrors.value = _valErrors.value.copy(description = error)
        return error
    }

    fun validateRating(): String {
        val error = when {
            rating < 1 || rating > 5 -> "Rating required"
            else -> ""
        }
        _valErrors.value = _valErrors.value.copy(rating = error)
        return error
    }

    fun validateTips(): String {
        val error = when {
            tips.isBlank() -> "tips required"
            else -> ""
        }
        _valErrors.value = _valErrors.value.copy(tips = error)
        return error
    }

    fun addImage(uri: String) {
            images.add(uri)
    }

    fun removeImage(uri: String) {
        images.remove(uri)
    }

    fun validateAll() {
        //validateDescription()
        validateRating()
        //validateTips()
    }

    ///######################CREATION OF A NEW REVIEW################################
    fun confirmReview(tripId: Int, context: Context): Boolean {

        //Trigger Basic Validation:
        validateAll()

        // Check if all validations pass
        if (isValid) {
            saveReview(tripId, context)
            resetAllFields()
            return true

        } else {
            println("Validation Error")
            return false
        }
    }

    fun saveReview(tripId: Int, context: Context) {
        val currentRating = rating
        val currentDescription = description
        val currentTips = tips
        val currentImages = images.toList()

        viewModelScope.launch {
            val uploadedImages = SupabaseHandler.uploadReviewImages(
                uris = currentImages,
                context = context,
            )

            model.addReview(tripId, userModel.loggedUser.value, currentRating, currentDescription, currentTips, uploadedImages)
            val newNotification = Notification(userId = tripToShow.value.author.id.toLong(), type = NotificationType.TRIP_REVIEW_RECEIVED, relatedUserId = userModel.loggedUser.value?.toLong() ?: 0L, relatedTripId = tripId.toLong())
            notificationModel.addNotification(newNotification)
        }
    }


    //###########################REVIEW MEMBERS#######################################
    private val _memberReviews = mutableStateMapOf<Int, MemberReview>() // key = userId
    val memberReviews: Map<Int, MemberReview> = _memberReviews

    private val _membersToReview = MutableStateFlow<List<UserProfile>>(emptyList())
    val membersToReview: StateFlow<List<UserProfile>> = _membersToReview

    private val _isEditingReviews = MutableStateFlow<Map<Int, ReviewMemberState>>(emptyMap())
    val isEditingReviews: StateFlow<Map<Int, ReviewMemberState>> = _isEditingReviews

    private val _savedMemberReviews = mutableStateMapOf<Int, MemberReview>()
    val savedMemberReviews: Map<Int, MemberReview> = _savedMemberReviews

    private val _generalMemberReviewValidation = MutableStateFlow(false)
    val generalMemberReviewValidation: StateFlow<Boolean> = _generalMemberReviewValidation

    fun loadUserToReview(tripId: Int) {
        _tripToShow.value = model.travelProposalsList.value.find{ it.id == tripId } ?: Trip()
        val accepted = _tripToShow.value.requests.filter { it.status == RequestStatus.Accepted }.map { r -> r.user }.filter { it.id != userModel.loggedUser.value }
        val toReview = listOf(tripToShow.value.author) + accepted
        _membersToReview.value = toReview
        val isEditingMap : Map<Int, ReviewMemberState> = toReview.associate { it.id to ReviewMemberState.NotAdded }
        _isEditingReviews.value = isEditingMap
        _memberReviews.clear()
        toReview.forEach{ m ->
            _memberReviews[m.id] = MemberReview(userModel.loggedUser.value!! , m.id)
        }
        _savedMemberReviews.clear()
        _valErrorsMembers.value = MemberReviewValidationErrors()
        _generalMemberReviewValidation.value = false
    }

    fun updateIsEditing(userId: Int, state: ReviewMemberState) {
        _isEditingReviews.value = _isEditingReviews.value.toMutableMap().apply {
            this[userId] = state
        }
    }

    fun updateMemberRating(userId: Int, rating: Int) {
        val review = _memberReviews[userId] ?: MemberReview(userModel.loggedUser.value!!, userId)
        _memberReviews[userId] = review.copy(rating = rating)
    }

    fun updateMemberReview(userId: Int, text: String) {
        val review = _memberReviews[userId] ?: MemberReview(userModel.loggedUser.value!!, userId)
        _memberReviews[userId] = review.copy(body = text)
    }

    fun saveMemberReview(userId: Int) {
        _memberReviews[userId]?.let { review ->
            _savedMemberReviews[userId] = review
        }
    }

    fun removeMemberReview(userId: Int) {
        _memberReviews[userId] = MemberReview(userModel.loggedUser.value!!, userId)
        if (_savedMemberReviews.containsKey(userId)) {
            _savedMemberReviews.remove(userId)
        }
        val ratingMap = _valErrorsMembers.value.ratings.toMutableMap()
        if (ratingMap.containsKey(userId)) {
            ratingMap.remove(userId)
        }
        val commentsMap = _valErrorsMembers.value.comments.toMutableMap()
        if (commentsMap.containsKey(userId)) {
            commentsMap.remove(userId)
        }
        _valErrorsMembers.value = _valErrorsMembers.value.copy(
            ratings = ratingMap,
            comments = commentsMap
        )
    }

    //funzione di reset
    fun resetAllFields() {
        description = ""
        rating = 0
        tips = ""
        images.clear()
        _memberReviews.clear()
    }

    //#############################VALIDATIONS MEMBER REVIEW#################################
    private fun validateMemberRating(userId: Int): Boolean {
        if (memberReviews[userId]?.rating ?: 0 < 1)
             return false
        else
            return true
    }

    private fun validateMemberComment(userId: Int): Boolean {
        if (memberReviews[userId]?.body.isNullOrBlank())
            return false
        else
            return true
    }

    fun validateMemberReviews(userId: Int): Boolean {
        val ratingValidation = validateMemberRating(userId)
        val commentValidation = validateMemberComment(userId)

        val ratingMap = _valErrorsMembers.value.ratings.toMutableMap()
        if (ratingValidation)
            ratingMap[userId] = false
        else
            ratingMap[userId] = true

        val commentMap = _valErrorsMembers.value.comments.toMutableMap()
        if (commentValidation)
            commentMap[userId] = false
        else
            commentMap[userId] = true

        _valErrorsMembers.value = _valErrorsMembers.value.copy(
                ratings = ratingMap,
                //comments = commentMap
        )

        return ratingValidation //&& commentValidation
    }

    /*fun validateAllMemberReviews(currentUserId: Int) {
        val ratingErrors = mutableMapOf<Int, String>()
        val commentErrors = mutableMapOf<Int, String>()

        savedMemberReviews.keys.forEach { userId ->
            if (userId == currentUserId) return@forEach
            ratingErrors[userId] = validateMemberRating(userId)
            commentErrors[userId] = validateMemberComment(userId)
        }

        _valErrorsMembers.value = MemberReviewValidationErrors(
            ratings = ratingErrors,
            comments = commentErrors
        )
    }*/

    /*fun validateMemberReviews(currentUserId: Int): Boolean {
        validateAllMemberReviews(currentUserId)

        return valErrorsMembers.value.ratings.all { it.value.isEmpty() } &&
                valErrorsMembers.value.comments.all { it.value.isEmpty() }
    }*/

    ///######################CREATION OF MEMBER REVIEW################################
    fun submitMemberReviews(): Boolean{
        if (savedMemberReviews.size>0){
            savedMemberReviews.forEach { (userId, review) ->
                model.addMemberReview(tripToShow.value.id, userModel.loggedUser.value, userId, review.rating, review.body)
                val newNotification = Notification(userId = userId.toLong(), type = NotificationType.USER_REVIEW_RECEIVED, relatedUserId = userModel.loggedUser.value?.toLong() ?: 0L, relatedTripId = tripToShow.value.id.toLong())
                notificationModel.addNotification(newNotification)
            }
            _generalMemberReviewValidation.value = false
            return true
        }
        else {
            _generalMemberReviewValidation.value = true
            return false
        }
    }
}


@Composable
fun NewReviewScreen(
    navController: NavController,
    selectedTrip: Int,
    viewModel: ReviewTripScreenViewModel = viewModel(factory = Factory)
) {
    LaunchedEffect(Unit) {
        if (!viewModel.initialized) {
            viewModel.loadUserToReview(selectedTrip)
            viewModel.initialized = true
        }
    }

    BackHandler {
        viewModel.initialized = false
        viewModel.resetAllFields()
        navController.popBackStack()
    }

    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val generalValidation by viewModel.valErrors.collectAsState()

    Column(Modifier.fillMaxSize()) {
        //TODO{check topbar}
        TopBar(navController, initializeReview = true, viewModelReview = viewModel)
        Spacer(modifier = Modifier.height(10.dp))
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text("New review", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(20.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    item {
                        Column {
                            //DESCRIPTION
                            Spacer(Modifier.height(6.dp))
                            //Rating
                            Rating(
                                rating = viewModel.rating,
                                onRatingChange = { viewModel.rating = it },
                                validationError = generalValidation.rating)
                            Spacer(Modifier.height(6.dp))
                            Description(viewModel)
                            //Tips
                            Spacer(Modifier.height(6.dp))
                            Tips(viewModel)
                            //image
                            Spacer(Modifier.height(6.dp))
                            ImageUpload(viewModel)
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
                                        viewModel.resetAllFields()
                                        navController.popBackStack()
                                        viewModel.initialized = false
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.elevatedButtonColors(
                                        containerColor = Color.Red,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text("Cancel")
                                }
                                // Confirm Button
                                Button(
                                    //"turn back in the navigation"
                                    onClick = {
                                        if (viewModel.confirmReview(selectedTrip, context) == true) {
                                            navController.popBackStack()
                                            viewModel.initialized = false
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
}
//image
@Composable
private fun ImageUpload(viewModel: ReviewTripScreenViewModel) {
    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        val currentImages = viewModel.images
        val remainingSlots = 5 - currentImages.size

        val newUris = uris.take(remainingSlots).map { it.toString() }
        newUris.forEach { viewModel.addImage(it) }
    }

    Text("Five images maximum", style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(3.dp))
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        println(this.maxWidth)
        val boxWidth = (maxWidth - 32.dp) / 5
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
                        .padding(top=5.dp)
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

            if (viewModel.images.size < 5) {
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
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Add Image",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }
}

//description
@Composable
private fun Description(viewModel: ReviewTripScreenViewModel) {
    val validationErrors by viewModel.valErrors.collectAsState()
    Text("Review", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(3.dp))
    OutlinedTextField(
        value = viewModel.description,
        onValueChange = { viewModel.description = it },
        label = { Text("Review") },
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

//rating
@Composable
fun Rating(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    validationError: String = ""
) {
    Column {
        Text("Rating", style = MaterialTheme.typography.titleMedium)
        Row {
            (1..5).forEach { index ->
                Icon(
                    imageVector = if (index <= rating) Icons.Filled.Star else Icons.Default.StarOutline,
                    contentDescription = "Star $index",
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onRatingChange(index) }
                )
            }
        }
        if (validationError.isNotBlank()) {
            Text(
                text = validationError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}


//tips
@Composable
private fun Tips(viewModel: ReviewTripScreenViewModel) {
    val validationErrors by viewModel.valErrors.collectAsState()
    Text("Tips", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(3.dp))
    OutlinedTextField(
        value = viewModel.tips,
        onValueChange = { viewModel.tips = it },
        label = { Text("Tips") },
        placeholder = { Text("More free time...") },
        isError = validationErrors.tips.isNotBlank(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        modifier = Modifier.fillMaxWidth()
    )
    if (validationErrors.tips.isNotBlank()) {
        Text(
            text = validationErrors.tips,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
        )
    }
}


@Composable
fun MemberReviewText(
    userId: Int,
    userName: String,
    reviewText: String,
    onReviewChange: (String) -> Unit,
    validationError: String = ""
) {
    Text("Review", style = MaterialTheme.typography.titleMedium)
    OutlinedTextField(
        value = reviewText,
        onValueChange = { onReviewChange(it) },
        label = { Text("Review for $userName") },
        modifier = Modifier.fillMaxWidth()
    )
    if (validationError.isNotBlank()) {
        Text(
            text = validationError,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun NewReviewMembersScreen(
    navController: NavController,
    selectedTrip: Int,
    viewModel: ReviewTripScreenViewModel = viewModel(factory = Factory)
) {

    LaunchedEffect(Unit) {
        if (!viewModel.initialized) {
            viewModel.loadUserToReview(selectedTrip)
            viewModel.initialized = true
        }
    }

    BackHandler {
        viewModel.initialized = false
        navController.popBackStack()
    }

    val toReview by viewModel.membersToReview.collectAsState()
    val isEditing by viewModel.isEditingReviews.collectAsState()
    val error by viewModel.valErrorsMembers.collectAsState()
    val generalValidation by viewModel.generalMemberReviewValidation.collectAsState()

    Column(Modifier.fillMaxSize()) {
        //TODO{check topbar}
        TopBar(navController, initializeReview = true, viewModelReview = viewModel)
        Spacer(modifier = Modifier.height(10.dp))
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text("Review members", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(20.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    item {
                        Column {
                            //Review
                            Spacer(Modifier.height(6.dp))
                            toReview.forEach { member ->

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth().padding(top = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    UserData(navController = navController, user = member)
                                    Spacer(modifier = Modifier.weight(1f))
                                    if ((isEditing[member.id]?:ReviewMemberState.NotAdded)==ReviewMemberState.NotAdded) {
                                        IconButton(onClick = {
                                            viewModel.updateIsEditing(member.id, ReviewMemberState.Editing) }) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "add"
                                            )
                                        }
                                    }
                                    else if ((isEditing[member.id]?:ReviewMemberState.NotAdded)==ReviewMemberState.Editing) {
                                        IconButton(onClick = {
                                            if (viewModel.validateMemberReviews(member.id)){
                                                viewModel.updateIsEditing( member.id, ReviewMemberState.Saved)
                                                viewModel.saveMemberReview(member.id)
                                            }
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "save"
                                            )
                                        }
                                        IconButton(onClick = {
                                                viewModel.updateIsEditing( member.id, ReviewMemberState.NotAdded)
                                                viewModel.removeMemberReview(member.id)
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Cancel,
                                                contentDescription = "delete"
                                            )
                                        }
                                    }
                                    else{
                                        IconButton(onClick = {
                                            viewModel.updateIsEditing(member.id, ReviewMemberState.Editing) }) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "edit"
                                            )
                                        }
                                        IconButton(onClick = {
                                            viewModel.updateIsEditing( member.id, ReviewMemberState.NotAdded)
                                            viewModel.removeMemberReview(member.id)
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Cancel,
                                                contentDescription = "delete"
                                            )
                                        }
                                    }
                                }
                                if((isEditing[member.id]?:ReviewMemberState.NotAdded)==ReviewMemberState.Editing) {
                                    // Rating
                                    val valErrorRating = if (error.ratings[member.id]?:false)
                                        "Rating required"
                                    else
                                        ""
                                    Rating(
                                        rating = viewModel.memberReviews[member.id]?.rating ?: 0,
                                        onRatingChange = {
                                            viewModel.updateMemberRating(member.id, it)
                                        },
                                        validationError = valErrorRating
                                    )

                                    val valErrorComment = if (error.comments[member.id]?:false)
                                        "Comment required"
                                    else
                                        ""
                                    // Review Text
                                    MemberReviewText(
                                        userId = member.id,
                                        userName = member.firstName,
                                        reviewText = viewModel.memberReviews[member.id]?.body ?: "",
                                        onReviewChange = {
                                            viewModel.updateMemberReview(
                                                member.id,
                                                it
                                            )
                                        },
                                        validationError = valErrorComment
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                            if (generalValidation) {
                                Text(
                                    text = "At least one review is required",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }


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
                                        viewModel.resetAllFields()
                                        navController.popBackStack()
                                        viewModel.initialized = false
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.elevatedButtonColors(
                                        containerColor = Color.Red,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text("Cancel")
                                }
                                // Confirm Button
                                Button(
                                    //"turn back in the navigation"
                                    // TODO: deleting hardcoded user
                                    onClick = {
                                        if (viewModel.submitMemberReviews()) {
                                            viewModel.resetAllFields()
                                            navController.popBackStack()
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
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