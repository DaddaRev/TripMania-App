    package com.example.travelapplicationv5

    import android.net.Uri
    import android.os.Parcelable
    import android.util.Log
    import androidx.compose.ui.graphics.Color
    import com.example.travelapplicationv5.Factory.notificationModel
    import com.example.travelapplicationv5.UserProfile
    import com.example.travelapplicationv5.Utility.generateUsers
    import com.google.firebase.firestore.FirebaseFirestore
    import kotlinx.android.parcel.Parcelize
    import kotlinx.coroutines.CoroutineScope
    import kotlinx.coroutines.Dispatchers
    import kotlinx.coroutines.SupervisorJob
    import kotlinx.coroutines.flow.MutableStateFlow
    import kotlinx.coroutines.flow.StateFlow
    import kotlinx.coroutines.flow.collectLatest
    import kotlinx.coroutines.flow.filter
    import kotlinx.coroutines.launch
    import java.time.LocalDate
    import java.time.ZoneId
    import java.time.format.DateTimeFormatter
    import java.time.format.DateTimeParseException
    import com.google.firebase.Timestamp
    import com.google.firebase.Firebase
    import com.google.firebase.auth.auth
    import com.google.firebase.firestore.Exclude
    import com.google.firebase.firestore.FieldValue
    import com.google.firebase.firestore.FirebaseFirestoreSettings
    import com.google.firebase.firestore.firestore
    import kotlinx.coroutines.channels.awaitClose
    import kotlinx.coroutines.flow.Flow
    import kotlinx.coroutines.flow.callbackFlow
    import com.google.firebase.firestore.Query
    import java.time.temporal.ChronoUnit
    import java.util.Date
    import kotlin.collections.filter
    import kotlin.collections.map

    //###########################################FIREBASE

    private fun stringToLocalDate(dateStr: String): LocalDate? {
        val italianFormatter = DateTimeFormatter.ofPattern("d/M/yyyy")
        val americanFormatter = DateTimeFormatter.ofPattern("M/d/yyyy")

        return try {
            LocalDate.parse(dateStr, italianFormatter)
        } catch (e: DateTimeParseException) {
            try {
                LocalDate.parse(dateStr, americanFormatter)
            } catch (e: DateTimeParseException) {
                null
            }
        }
    }

    fun LocalDate.toTimestamp(): Timestamp {
        return Timestamp(Date.from(this.atStartOfDay(ZoneId.systemDefault()).toInstant()))
    }

    fun userProfileFromFirebase(data: Map<String, Any?>): UserProfile {
        println("Data Firestore: $data")

        val timestamp = data["dateOfBirth"] as? Timestamp
        val dateOfBirth = timestamp?.toDate()?.toInstant()
            ?.atZone(ZoneId.systemDefault())
            ?.toLocalDate() ?: LocalDate.now()


        return UserProfile(
            id = (data["id"] as Long).toInt(),
            firstName = data["firstName"] as? String ?: "",
            lastName = data["lastName"] as? String ?: "",
            nickname = data["nickname"] as? String ?: "",
            image = (data["image"] as? String)?.let { Uri.parse(it) },
            imageId = (data["imageId"] as? Long)?.toInt(),
            imageMonogram = data["imageMonogram"] as? Boolean ?: false,
            phoneNumber = data["phoneNumber"] as? String ?: "",
            email = data["email"] as? String ?: "",
            dateOfBirth = dateOfBirth,
            preferences = (data["preferences"] as? Map<String, List<String>>) ?: emptyMap(),
            tripsCreated = (data["tripsCreated"] as? Long)?.toInt() ?: 0,
            currentBadge = data["currentBadge"] as? String,
            saved = (data["saved"] as? List<Int>) ?: emptyList()
        )
    }

    fun memberReviewFromFirebase(data: Map<String, Any?>, users: List<UserProfile>): MemberReview {
        val authorId = data["author"] as? Long
        val author = users.find { it.id == authorId?.toInt() } ?: UserProfile()

        return MemberReview(
            author = author,
            reviewed = (data["reviewed"] as Long).toInt(),
            rating = (data["rating"] as Long).toInt(),
            body = data["body"] as? String ?: "",
        )
    }


    fun reviewFromFirebase(data: Map<String, Any?>, users: List<UserProfile>): Review {
        val authorId = data["author"] as? Long
        val author = users.find { it.id == authorId?.toInt() } ?: UserProfile()

        return Review(
            author = author,
            rating = (data["rating"] as Long).toInt(),
            body = data["body"] as? String ?: "",
            tips = data["tips"] as? String ?: "",
            images = data["images"] as? List<String> ?: emptyList()
        )
    }


    fun requestFromFirebase(data: Map<String, Any?>, users: List<UserProfile>): Request {
        val userId = data["user"] as? Long
        val user = users.find { it.id == userId?.toInt() } ?: UserProfile()

        val statusStr = data["status"] as? String ?: "Pending"
        val status = RequestStatus.valueOf(statusStr)

        return Request(
            user = user,
            status = status,
            companion = (data["companion"] as? Long)?.toInt() ?: 0
        )
    }


    fun stopFromFirebase(data: Map<String, Any?>): Stop {
        val timestamp = data["date"] as? Timestamp
        val date = timestamp?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
            ?: LocalDate.now()  // fallback se null

        return Stop(
            title = data["title"] as? String ?: "",
            date = date,
            location = data["location"] as? String ?: "",
            free = data["free"] as? Boolean ?: true,
            activities = data["activities"] as? String ?: "",
            //activities = (data["activities"] as? List<String>)?.joinToString(", ") ?: "",
            latitude = (data["latitude"] as? Double),
            longitude = (data["longitude"] as? Double)
        )
    }


    fun tripFromFirebase(data: Map<String, Any?>, users: List<UserProfile>): Trip {
        println("Data Firestore: $data")
        val authorId = data["author"] as? Long
        val author = users.find { it.id == authorId?.toInt() } ?: UserProfile()

        return Trip(
            id = (data["id"] as Long).toInt(),
            author = author,
            title = data["title"] as? String ?: "",
            description = data["description"] as? String ?: "",
            countries = data["countries"] as? List<String> ?: emptyList(),
            spotsTotal = (data["spotsTotal"] as Long).toInt(),
            price = run {
                val priceMap = data["price"] as? Map<String, Any?> ?: emptyMap()
                Pair(
                    (priceMap["min"] as? Long)?.toInt() ?: 0,
                    (priceMap["max"] as? Long)?.toInt() ?: 0
                )
            },
            images = data["images"] as? List<String> ?: emptyList(),
            date = run {
                // Assuming data["date"] is a Map with startDate and endDate as Timestamp
                val dateMap = data["date"] as? Map<String, Any?> ?: emptyMap()
                val startTimestamp = dateMap["startDate"] as? Timestamp
                val endTimestamp = dateMap["endDate"] as? Timestamp
                val startDate =
                    startTimestamp?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
                        ?: LocalDate.now()
                val endDate =
                    endTimestamp?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
                Pair(startDate, endDate)
            },
            itinerary = (data["itinerary"] as? List<Map<String, Any?>>)?.map { stopFromFirebase(it) }
                ?: emptyList(),
            requests = (data["requests"] as? List<Map<String, Any?>>)?.map {
                requestFromFirebase(
                    it,
                    users
                )
            } ?: emptyList(),
            reviews = (data["reviews"] as? List<Map<String, Any?>>)?.map {
                reviewFromFirebase(
                    it,
                    users
                )
            } ?: emptyList(),
            memberReviews = (data["memberReviews"] as? List<Map<String, Any?>>)?.map {
                memberReviewFromFirebase(
                    it,
                    users
                )
            } ?: emptyList(),
            telegramLink = data["telegramLink"] as? String
        )
    }

    fun userToFirebase(user: UserProfile): Map<String, Any?> {
        return mapOf(
            "id" to user.id.toLong(),
            "firstName" to user.firstName,
            "lastName" to user.lastName,
            "nickname" to user.nickname,
            "image" to user.image?.toString(),
            "imageId" to user.imageId?.toLong(),
            "imageMonogram" to user.imageMonogram,
            "phoneNumber" to user.phoneNumber,
            "email" to user.email,
            "dateOfBirth" to user.dateOfBirth.toTimestamp(),
            "preferences" to user.preferences,
            "tripsCreated" to user.tripsCreated,
            "currentBadge" to user.currentBadge,
            "saved" to user.saved
        )
    }

    fun tripToFirebase(trip: Trip): Map<String, Any?> {
        return mapOf(
            "id" to trip.id.toLong(),
            "author" to trip.author.id.toLong(),
            "title" to trip.title,
            "description" to trip.description,
            "countries" to trip.countries,
            "spotsTotal" to trip.spotsTotal.toLong(),
            "price" to mapOf("min" to trip.price.first.toLong(), "max" to trip.price.second.toLong()),
            "images" to trip.images,
            "date" to mapOf(
                "startDate" to trip.date.first.toTimestamp(),
                "endDate" to trip.date.second?.toTimestamp()
            ),
            "itinerary" to trip.itinerary.map { stop ->
                mapOf(
                    "title" to stop.title,
                    "date" to stop.date.toTimestamp(),
                    "location" to stop.location,
                    "free" to stop.free,
                    "activities" to stop.activities,
                    "latitude" to stop.latitude,
                    "longitude" to stop.longitude
                )
            },
            "requests" to trip.requests.map { req ->
                mapOf(
                    "user" to req.user.id.toLong(),
                    "status" to req.status.name,
                    "companion" to req.companion.toLong()
                )
            },
            "reviews" to trip.reviews.map { rev ->
                mapOf(
                    "author" to rev.author.id.toLong(),
                    "rating" to rev.rating.toLong(),
                    "body" to rev.body,
                    "tips" to rev.tips,
                    "images" to rev.images
                )
            },
            "memberReviews" to trip.memberReviews.map { rev ->
                mapOf(
                    "author" to rev.author.id.toLong(),
                    "reviewed" to rev.reviewed.toLong(),
                    "rating" to rev.rating.toLong(),
                    "body" to rev.body
                )
            },
            "telegramLink" to trip.telegramLink
        )
    }


    object Collections{
        private const val C_NOTIFICATIONS = "notifications"
        private const val C_TRIPS = "trips"
        private const val C_USERS = "users"

        private val db: FirebaseFirestore
            get() = Firebase.firestore

        init {
            db.firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true) //false to Disable LocalChaching
                .build()
        }

        val notifications = db.collection(C_NOTIFICATIONS)
        val trips = db.collection(C_TRIPS)
        val users = db.collection(C_USERS)
    }


    //############################################USER##########################################
    // data class to store user information's
    @Parcelize
    data class UserProfile(
        var id: Int,
        var firstName: String,
        var lastName: String,
        var nickname: String,
        var image: Uri?,
        var imageId: Int?,
        var imageMonogram: Boolean,
        var phoneNumber: String,
        var email: String,
        var dateOfBirth: LocalDate,
        var preferences: Map<String, List<String>>,
        var tripsCreated: Int,
        var currentBadge: String?,
        var saved: List<Int>
    ) : Parcelable {
        constructor() : this(0, "", "", "", null, null, false, "", "", LocalDate.now(), emptyMap(), 0, null, emptyList())
    }
    enum class UserBadge(
        val displayName: String,
        val iconResId: Int,
        val minTrips: Int,
        val color: Color
    ) {
        NOVICE("Novice Traveler", R.drawable.badge_novice, 1, Color(0xFFB3E5FC)),
        EXPLORER("Explorer", R.drawable.badge_explorer, 3, Color(0xFF4CAF50)),
        TRAVEL_GURU("Travel Guru", R.drawable.badge_guru, 5, Color(0xFF2196F3)),
        TRAVEL_LEGEND("Travel legend", R.drawable.badge_legend, 10, Color(0xFFFF9800));
        companion object {
            fun fromString(name: String?): UserBadge {
                return values().find { it.name == name } ?: NOVICE
            }
        }
    }
    //User model class
    class UserModel {
        //private val _usersList = MutableStateFlow<List<UserProfile>>(Utility.generateUsers())
        private val _usersList = MutableStateFlow<List<UserProfile>>(emptyList())
        val usersList: StateFlow<List<UserProfile>> = _usersList

        // Expose a MutableStateFlow to be observed from the user
        private val _isUserLoggedIn = MutableStateFlow<Boolean>(false)
        val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn

        // Returns a user given his id
        fun getUserById(userId: Int): UserProfile? {
            return usersList.value.find { it.id == userId }
        }

        private val _loggedUser = MutableStateFlow<Int?>(null)
        val loggedUser: StateFlow<Int?> = _loggedUser

        fun isRegistered(email: String): UserProfile?{
            if (email=="")
                return null
            return _usersList.value.find { it.email==email }
        }

        private fun addUserProfile(newUser: UserProfile) {
            val user = userToFirebase(newUser)
            Collections.users
                .add(user)
                .addOnSuccessListener {
                    Log.e("Firestore", "User added")
                }
                .addOnFailureListener {
                    Log.e("Firestore", "Failure")
                }
        }

        fun userRegistration(userProfile: UserProfileEditable, preferences: Map<String, Map<String, Boolean>>){
            val nextId = (_usersList.value.map { it.id }.maxOrNull() ?: 0) + 1
            val newPreferences = preferences.mapValues { section ->
                section.value.filter { it.value }.map { it.key }
            }.filter { it.value.isNotEmpty() }

            val newUser = UserProfile(
                nextId,
                userProfile.firstName,
                userProfile.lastName,
                userProfile.nickname,
                userProfile.image,
                userProfile.imageId,
                userProfile.imageMonogram,
                userProfile.phoneNumber,
                userProfile.email,
                stringToLocalDate(userProfile.dateOfBirth)!!,
                newPreferences,
                tripsCreated = 0,
                currentBadge = UserBadge.NOVICE.name,
                emptyList()
            )
            addUserProfile(newUser)
            _isUserLoggedIn.value = true
            _loggedUser.value = nextId
        }

        fun userLogIn(user: UserProfile) {
            _loggedUser.value = user.id
            _isUserLoggedIn.value = true
        }

        fun userLogOut() {
            Firebase.auth.signOut()   // Close the session on firebase
            _isUserLoggedIn.value = false
            _loggedUser.value = null
        }

        fun getAllUsers() {
            Collections.users
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        println("Listen failed: $e")
                        return@addSnapshotListener
                    }

                    if (snapshots != null) {
                        val users = snapshots.mapNotNull { doc ->
                            try {
                                val user = userProfileFromFirebase(doc.data)
                                println("Utente caricato: ${user.id} - ${user.firstName} ${user.lastName}")
                                user
                            } catch (ex: Exception) {
                                println("Errore parsing utente")
                                null
                            }
                        }
                        _usersList.value = users
                        println("Totale utenti caricati: ${users.size}")
                    }
                }
        }

        init {
            getAllUsers()
        }

        // Updates the usersList with the new informations related to the user who has been modified
        // from the GUI
        fun updateUserProfile(
            userId: Int,
            newUserProfile: UserProfileEditable
        ) {

            val userDocQuery = Collections.users.whereEqualTo("id", userId).limit(1)

            userDocQuery.get().addOnSuccessListener { querySnapshot ->
                val doc = querySnapshot.documents.firstOrNull()
                if (doc != null) {
                    val data = stringToLocalDate(newUserProfile.dateOfBirth)
                    val timestamp = data!!.toTimestamp()
                    val updatedData = mapOf(
                        "firstName" to newUserProfile.firstName,
                        "lastName" to newUserProfile.lastName,
                        "nickname" to newUserProfile.nickname,
                        "image" to newUserProfile.image?.toString(),
                        "imageId" to newUserProfile.imageId?.toLong(),
                        "imageMonogram" to newUserProfile.imageMonogram,
                        "email" to newUserProfile.email,
                        "phoneNumber" to newUserProfile.phoneNumber,
                        "dateOfBirth" to timestamp
                    )

                    doc.reference.update(updatedData)
                        .addOnSuccessListener {
                            Log.d("UserModel", "User profile aggiornato con successo su Firebase")
                        }
                        .addOnFailureListener { e ->
                            Log.e("UserModel", "Errore aggiornamento user profile su Firebase: ${e.message}")
                        }
                }
            }.addOnFailureListener {
                Log.e("UserModel", "Errore ricerca utente per aggiornare user profile: ${it.message}")
            }
        }

        fun removeUser() {
            Collections.users
                .whereEqualTo("id", loggedUser.value)
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        Collections.users.document(document.id)
                            .delete()
                            .addOnSuccessListener {
                                Log.d("Firebase", "Utente eliminato con successo: ${document.id}")
                            }
                            .addOnFailureListener { e ->
                                Log.e("Firebase", "Errore nell'eliminazione del documento", e)
                            }
                    }
                    if (result.isEmpty) {
                        Log.d("Firebase", "Nessun utente trovato")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firebase", "Errore nella ricerca del documento", e)
                }
            userLogOut()
        }

        fun isRemoved(userId: Int): Boolean {
            return !_usersList.value.any { it.id == userId }
        }

        fun updatePreferences(newPreferences: Map<String, Map<String, Boolean>>) {
            val userDocQuery = Collections.users.whereEqualTo("id", loggedUser.value).limit(1)

            userDocQuery.get().addOnSuccessListener { querySnapshot ->
                val doc = querySnapshot.documents.firstOrNull()
                if (doc != null) {
                    // Trasforma newPreferences nel formato che vuoi salvare su Firestore
                    val prefsToSave = newPreferences.mapValues { section ->
                        section.value.filter { it.value }.map { it.key }
                    }.filter { it.value.isNotEmpty() }

                    doc.reference.update("preferences", prefsToSave)
                        .addOnSuccessListener {
                            Log.d("UserModel", "Preferences aggiornate con successo su Firebase")
                        }
                        .addOnFailureListener { e ->
                            Log.e("UserModel", "Errore aggiornamento preferences su Firebase: ${e.message}")
                        }
                }
            }.addOnFailureListener {
                Log.e("UserModel", "Errore ricerca utente per aggiornare preferences: ${it.message}")
            }
        }

        fun addSavedTrip(tripId: Int) {
            val userDocQuery = Collections.users.whereEqualTo("id", loggedUser.value).limit(1)

            userDocQuery.get().addOnSuccessListener { querySnapshot ->
                val doc = querySnapshot.documents.firstOrNull()
                if (doc != null) {
                    doc.reference.update("saved", FieldValue.arrayUnion(tripId))
                        .addOnSuccessListener {
                            Log.d("UserModel", "Trip saved aggiunto con successo su Firebase")
                        }
                        .addOnFailureListener { e ->
                            Log.e("UserModel", "Errore aggiornamento saved trip su Firebase: ${e.message}")
                        }
                }
            }.addOnFailureListener {
                Log.e("UserModel", "Errore ricerca utente per aggiornare preferences: ${it.message}")
            }
        }

        fun removeSavedTrip(tripId: Int) {
            val userDocQuery = Collections.users.whereEqualTo("id", loggedUser.value).limit(1)

            userDocQuery.get().addOnSuccessListener { querySnapshot ->
                val doc = querySnapshot.documents.firstOrNull()
                if (doc != null) {
                    doc.reference.update("saved", FieldValue.arrayRemove(tripId))
                        .addOnSuccessListener {
                            Log.d("UserModel", "Trip saved rimosso con successo su Firebase")
                        }
                        .addOnFailureListener { e ->
                            Log.e("UserModel", "Errore aggiornamento saved trip su Firebase: ${e.message}")
                        }
                }
            }.addOnFailureListener {
                Log.e("UserModel", "Errore ricerca utente per aggiornare preferences: ${it.message}")
            }
        }

    }

    //############################################NOTIFICATION##########################################
    enum class NotificationType {
        LAST_MINUTE_PROPOSAL,
        NEW_APPLICATION,
        APPLICATION_ACCEPTED,
        APPLICATION_REFUSED,
        APPLICATION_REMOVED,
        USER_REVIEW_RECEIVED,
        TRIP_REVIEW_RECEIVED,
        RECOMMENDED_TRIP
    }

    data class Notification(
        val userId: Long = 0L,
        val type: NotificationType = NotificationType.NEW_APPLICATION,
        val timestamp: Long = System.currentTimeMillis(),
        val read: Boolean = false,
        val relatedTripId: Long = 0L,
        val relatedUserId: Long = 0L,
        @get:Exclude val id: String = ""
    )


    class NotificationModel(userModel: UserModel){

        val loggedUser = userModel.loggedUser
        val isLogged = userModel.isUserLoggedIn

        fun getNotifications(userId: Int?): Flow<List<Notification>> = callbackFlow {
            val listener = Collections.notifications
                .whereEqualTo("userId", userId?.toLong())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (snapshot != null) {
                        val notifications = snapshot.documents.map { not ->
                            not.toObject(Notification::class.java)!!.copy(id = not.id)
                        }
                        trySend(notifications)
                    } else {
                        Log.e("Firestore", "Error: ${error?.message}")
                        trySend(emptyList())
                    }
                }
            awaitClose { listener.remove() }
        }

        fun addNotification(notification: Notification) {
            Collections.notifications
                .add(notification)
                .addOnSuccessListener {
                    Log.e("Firestore", "Notification added")
                }
                .addOnFailureListener {
                    Log.e("Firestore", "Failure")
                }
        }

        fun markNotificationAsRead(notificationId: String) {
            Collections.notifications
                .document(notificationId)
                .update("read", true)
                .addOnSuccessListener {
                    Log.e("Firestore", "Notification read")
                }
                .addOnFailureListener {
                    Log.e("Firestore", "Failure")
                }
        }

        fun checkTripsNotification(allTrips: List<Trip>, user: UserProfile, isJoined: (Int, Int) -> Boolean) {
            val lastMinuteTrips = checkLastMinuteTrips(allTrips, user.id, isJoined)
            val recommendedTrips = checkRecommendedTrips(allTrips, user, isJoined)

            lastMinuteTrips.forEach { trip ->
                val query = Collections.notifications
                    .whereEqualTo("userId", user.id?.toLong())
                    .whereEqualTo("relatedTripId", trip.id.toLong())
                    .whereEqualTo("type", NotificationType.LAST_MINUTE_PROPOSAL)

                query.get().addOnSuccessListener { snapshot ->
                    if (snapshot.isEmpty) {
                        val newNotification = Notification(userId = user.id?.toLong() ?: 0, relatedTripId = trip.id.toLong(), type = NotificationType.LAST_MINUTE_PROPOSAL, )
                        addNotification(newNotification)
                    }
                }
            }

            recommendedTrips.forEach { trip ->
                val query = Collections.notifications
                    .whereEqualTo("userId", user.id?.toLong())
                    .whereEqualTo("relatedTripId", trip.id.toLong())
                    .whereEqualTo("type", NotificationType.RECOMMENDED_TRIP)

                query.get().addOnSuccessListener { snapshot ->
                    if (snapshot.isEmpty) {
                        val newNotification = Notification(userId = user.id?.toLong() ?: 0, relatedTripId = trip.id.toLong(), type = NotificationType.RECOMMENDED_TRIP)
                        addNotification(newNotification)
                    }
                }
            }
        }

        private fun checkLastMinuteTrips(trips: List<Trip>, userId: Int?, isJoined: (Int, Int) -> Boolean) : List<Trip> {
            val now = LocalDate.now()
            return trips.filter { trip ->
                val daysUntil = ChronoUnit.DAYS.between(now, trip.date.first)
                daysUntil in 0..5 && computeSpotsLeft(trip)>0 && trip.author.id!=userId && !isJoined(trip.id, userId!!)
            }
        }

        private fun checkRecommendedTrips(trips: List<Trip>, user: UserProfile, isJoined: (Int, Int) -> Boolean) : List<Trip> {
            val allPrefs = user.preferences
                .filterKeys { it != "Group dimension" }
                .values
                .flatten()
                .map { it.lowercase() }

            val groupPrefs = user.preferences["Group dimension"] ?: emptyList()

            return trips.filter { trip ->

                val groupMatch = groupPrefs.any { groupPref ->
                    when (groupPref) {
                        "2-3 people" -> trip.spotsTotal in 2..3
                        "4-10 people" -> trip.spotsTotal in 4..10
                        "10+" -> trip.spotsTotal > 10
                        else -> false
                    }
                }

                val textFields = listOf(
                    trip.title.lowercase(),
                    trip.description.lowercase()
                ) + trip.itinerary.map { it.activities.lowercase() }

                val contentMatch = allPrefs.any { pref ->
                    textFields.any { field -> field.contains(pref) }
                }

                groupMatch && contentMatch && computeSpotsLeft(trip)>0 && trip.author.id!=user.id && !isJoined(trip.id, user.id)
            }
        }

    }

    //############################################TRIP##########################################
    // Data class to store single stop information's
    @Parcelize
    data class Stop(
        var title: String,
        var date: LocalDate,
        var location: String,
        var latitude: Double? = null,
        var longitude: Double? = null,
        var free: Boolean,
        var activities: String,
    ) : Parcelable {
        constructor(title: String, date: LocalDate, location: String) : this(
            title = title,
            date = date,
            location = location,
            latitude = null,
            longitude = null,
            free = true,
            activities = "",
        )
    }

    // Data class to store a single review
    @Parcelize
    data class Review(
        val author: UserProfile,
        var rating: Int,
        var body: String,
        var tips: String,
        var images: List<String> = emptyList()
    ) : Parcelable {
        constructor(author: UserProfile, rating: Int) : this(
            author = author,
            rating = rating,
            body = "",
            tips = "",
            images = emptyList()
        )
    }

    // Data class to store a single member review
    @Parcelize
    data class MemberReview(
        var author: UserProfile,
        val reviewed: Int,
        val tripId: Int = 0,
        var rating: Int,
        var body: String,
    ) : Parcelable {
        constructor(authorId: Int, reviewdId: Int) : this(
            author = generateUsers().first { it.id == authorId },
            reviewed = reviewdId,
            rating = 0,
            body = ""
        )
    }

    enum class RequestStatus {
        Pending,
        Refused,
        Accepted
    }

    @Parcelize
    data class Request(
        var user: UserProfile,
        var status: RequestStatus,
        var companion: Int,
    ) : Parcelable {
        constructor() : this(user = UserProfile(), status = RequestStatus.Pending, companion = 0)
    }

    // Data class to store trip information's
    @Parcelize
    data class Trip(
        var id: Int,
        var author: UserProfile,
        var title: String,
        var description: String,
        var countries: List<String>,
        var spotsTotal: Int,
        var price: Pair<Int, Int>,
        var images: List<String>,
        var date: Pair<LocalDate, LocalDate?>,
        var itinerary: List<Stop>,
        var requests: List<Request>,
        var reviews: List<Review>,
        var memberReviews: List<MemberReview>,
        val telegramLink: String? = null
    ) : Parcelable {
        constructor() : this(
            id = 0,
            author = UserProfile(),
            title = "",
            description = "",
            countries = emptyList(),
            spotsTotal = 0,
            price = Pair(0, 0),
            images = emptyList(),
            date = Pair(LocalDate.now(), null),
            itinerary = emptyList(),
            requests = emptyList(),
            reviews = emptyList(),
            memberReviews = emptyList()
        )
    }

    // Trip class
    class TripModel(userModel: UserModel) {

        //private val _travelProposalsList = MutableStateFlow(Utility.generateTrips())
        private val _travelProposalsList = MutableStateFlow<List<Trip>>(emptyList())
        val travelProposalsList: StateFlow<List<Trip>> = _travelProposalsList

        private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val db = FirebaseFirestore.getInstance()

        val userId: StateFlow<Int?> = userModel.loggedUser
        val isLoggedIn: StateFlow<Boolean> = userModel.isUserLoggedIn

        init {
            // Osserva gli utenti finché non sono disponibili
            scope.launch {
                userModel.usersList
                    .filter { it.isNotEmpty() } // Aspetta che ci siano utenti
                    .collectLatest { users ->
                        getAllTrips(users)
                    }
            }
        }

        fun getAllTrips(users: List<UserProfile>) {
            Collections.trips.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("Errore nel recupero viaggi: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val trips = snapshot.mapNotNull { doc ->
                        try {
                            val trip = tripFromFirebase(doc.data, users)
                            println("Viaggio caricato: ${trip.title} (ID: ${trip.id})")
                            trip
                        } catch (e: Exception) {
                            println("Errore parsing viaggio per documento ID=${doc.id}: ${e.message}")
                            null
                        }
                    }
                    val profile = users.filter { it.id==userId.value }.firstOrNull()

                    _travelProposalsList.value = trips
                    if (isLoggedIn.value)
                        notificationModel.checkTripsNotification(trips, profile!!, ::isJoined)
                    println("Totale viaggi aggiornati: ${trips.size}")
                }
            }
        }

        fun calculateRatingAverage(trip: Trip): Double {
            return if (trip.reviews.isNotEmpty()) {
                trip.reviews.map { it.rating }.average()
            } else {
                0.0
            }
        }

        fun isJoined(tripId: Int, memberId: Int): Boolean {
            val trip = _travelProposalsList.value.find { it.id == tripId } ?: Trip()
            val request =
                trip.requests.find { it.user.id == memberId && (it.status == RequestStatus.Pending || it.status == RequestStatus.Accepted) }
            Log.d("atomic", (request != null).toString())
            return request != null
        }

        fun isRefused(tripId: Int, memberId: Int): Boolean {
            val trip = _travelProposalsList.value.find { it.id == tripId } ?: Trip()
            val request =
                trip.requests.find { it.user.id == memberId && it.status == RequestStatus.Refused }
            return request != null
        }

        fun denyRequest(tripId: Int, userId: Int) {
            val tripDocQuery = Collections.trips.whereEqualTo("id", tripId).limit(1)
            tripDocQuery.get().addOnSuccessListener { querySnapshot ->
                val doc = querySnapshot.documents.firstOrNull()
                if (doc != null) {
                    val currentRequests = (doc.get("requests") as? List<Map<String, Any?>>) ?: emptyList()

                    val updatedRequests = currentRequests.map { reqMap ->
                        val userIdInMap = reqMap["user"] as? Long

                        if (userIdInMap?.toInt() == userId) {
                            reqMap.toMutableMap().apply {
                                this["status"] = RequestStatus.Refused.name
                            }
                        } else {
                            reqMap
                        }
                    }

                    doc.reference.update("requests", updatedRequests)
                        .addOnSuccessListener {
                            Log.d("TripModel", "Request rifiutata aggiornata con successo su Firebase")
                        }
                        .addOnFailureListener { e ->
                            Log.e("TripModel", "Errore aggiornamento request su Firebase: ${e.message}")
                        }
                }
            }.addOnFailureListener {
                Log.e("TripModel", "Errore ricerca viaggio per aggiornare request: ${it.message}")
            }
        }

        fun acceptRequest(tripId: Int, memberId: Int) {
            val tripDocQuery = Collections.trips.whereEqualTo("id", tripId).limit(1)
            tripDocQuery.get().addOnSuccessListener { querySnapshot ->
                val doc = querySnapshot.documents.firstOrNull()

                if (doc != null) {
                    val currentRequests = (doc.get("requests") as? List<Map<String, Any?>>) ?: emptyList()

                    // Aggiorna la lista delle requests cambiando lo status del membro indicato
                    val updatedRequests = currentRequests.map { reqMap ->
                        val userId = reqMap["user"] as? Long

                        if (userId?.toInt() == memberId) {
                            reqMap.toMutableMap().apply {
                                this["status"] = RequestStatus.Accepted.name
                            }
                        } else {
                            reqMap
                        }
                    }

                    doc.reference.update("requests", updatedRequests)
                        .addOnSuccessListener {
                            Log.d("TripModel", "Request accettata aggiornata con successo su Firebase")
                        }
                        .addOnFailureListener { e ->
                            Log.e("TripModel", "Errore aggiornamento request su Firebase: ${e.message}")
                        }
                }
            }.addOnFailureListener {
                Log.e("TripModel", "Errore ricerca viaggio per aggiornare request: ${it.message}")
            }
        }

        fun addNewTrip(trip: Trip) {
            val nextId = (_travelProposalsList.value.map { it.id }.maxOrNull() ?: 0) + 1
            val newTrip = trip.copy(id = nextId)

            val tripData = tripToFirebase(newTrip)
            Collections.trips.add(tripData)
                .addOnSuccessListener {
                        docRef ->
                    // Incrementa il contatore dei viaggi creati dall'autore
                    updateUserTripCount(newTrip.author.id)
                    println("Viaggio aggiunto con successo a Firebase con ID: ${docRef.id}")
                }
                .addOnFailureListener { e ->
                    println("Errore nell'aggiunta del viaggio a Firebase: ${e.message}")
                }
        }
        private fun updateUserTripCount(userId: Int) {
            val userDocQuery = Collections.users.whereEqualTo("id", userId).limit(1)
            userDocQuery.get().addOnSuccessListener { querySnapshot ->
                val doc = querySnapshot.documents.firstOrNull()
                if (doc != null) {
                    val currentCount = (doc.get("tripsCreated") as? Long)?.toInt() ?: 0
                    val newCount = currentCount + 1
                    val newBadge = determineBadge(newCount).name

                    doc.reference.update(
                        mapOf(
                            "tripsCreated" to newCount,
                            "currentBadge" to newBadge
                        )
                    ).addOnSuccessListener {
                        Log.d("TripModel", "Contatore viaggi aggiornato per utente $userId")
                    }
                }
            }
        }
        private fun determineBadge(tripsCount: Int): UserBadge {
            return when {
                tripsCount >= UserBadge.TRAVEL_LEGEND.minTrips -> UserBadge.TRAVEL_LEGEND
                tripsCount >= UserBadge.TRAVEL_GURU.minTrips -> UserBadge.TRAVEL_GURU
                tripsCount >= UserBadge.EXPLORER.minTrips -> UserBadge.EXPLORER
                else -> UserBadge.NOVICE
            }
        }
        fun updateTrip(trip: Trip) {
            Collections.trips
                .whereEqualTo("id", trip.id)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val doc = querySnapshot.documents[0]
                        doc.reference.update(tripToFirebase(trip))
                            .addOnSuccessListener {
                            }
                    }
                }
        }

        fun removeTrip(toRemove: Trip) {
            val tripDocQuery = Collections.trips.whereEqualTo("id", toRemove.id).limit(1)
            tripDocQuery.get().addOnSuccessListener { querySnapshot ->
                val doc = querySnapshot.documents.firstOrNull()
                if (doc != null) {
                    doc.reference.delete()
                        .addOnSuccessListener {
                            Log.d("TripModel", "Viaggio rimosso con successo da Firebase")
                        }
                        .addOnFailureListener { e ->
                            Log.e("TripModel", "Errore rimozione viaggio da Firebase: ${e.message}")
                        }
                } else {
                    Log.w("TripModel", "Documento viaggio non trovato per la rimozione")
                }
            }.addOnFailureListener {
                Log.e("TripModel", "Errore ricerca viaggio per rimozione: ${it.message}")
            }
        }

        fun addRequest(tripId: Int, userId: Int, companion: Int) {
            Collections.trips
                .whereEqualTo("id", tripId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val doc = querySnapshot.documents[0]
                        val currentRequests = doc.get("requests") as? List<Map<String, Any>> ?: emptyList()

                        val newRequest = mapOf(
                            "user" to userId.toLong(),
                            "status" to RequestStatus.Pending.name,
                            "companion" to companion.toLong()
                        )

                        doc.reference.update("requests", currentRequests + newRequest)
                    }
                }
        }


        fun removeRequest(tripId: Int, userId: Int) {
            val tripDocQuery = Collections.trips.whereEqualTo("id", tripId).limit(1)
            tripDocQuery.get().addOnSuccessListener { querySnapshot ->
                val doc = querySnapshot.documents.firstOrNull()
                if (doc != null) {
                    // Filtra la lista attuale di requests eliminando quella da rimuovere
                    val currentRequests = (doc.get("requests") as? List<Map<String, Any?>>) ?: emptyList()

                    val updatedRequests = currentRequests.filter {
                        val user = it["user"] as? Long
                        user?.toInt() != userId
                    }

                    doc.reference.update("requests", updatedRequests)
                        .addOnSuccessListener {
                            Log.d("TripModel", "Request rimossa con successo su Firebase")
                        }
                        .addOnFailureListener { e ->
                            Log.e("TripModel", "Errore rimozione request su Firebase: ${e.message}")
                        }
                }
            }.addOnFailureListener {
                Log.e("TripModel", "Errore ricerca viaggio per rimuovere request: ${it.message}")
            }
        }

        fun addReview(tripId: Int, userId: Int?, rating: Int, body: String, tips: String, images: List<String>) {

            val tripDocQuery = Collections.trips.whereEqualTo("id", tripId).limit(1)
            tripDocQuery.get().addOnSuccessListener { querySnapshot ->
                val doc = querySnapshot.documents.firstOrNull()
                if (doc != null) {
                    // Otteniamo la lista attuale delle recensioni dal DB, o la creiamo se nulla
                    val currentReviews = (doc.get("reviews") as? List<Map<String, Any?>>) ?: emptyList()

                    // Convertiamo la review da Kotlin a Map
                    val reviewMap = mapOf(
                        "author" to userId?.toLong(),
                        "rating" to rating.toLong(),
                        "body" to body,
                        "tips" to tips,
                        "images" to images
                    )

                    // Nuova lista con la review aggiunta
                    val updatedReviews = currentReviews + reviewMap

                    // Aggiorna il campo reviews
                    doc.reference.update("reviews", updatedReviews)
                        .addOnSuccessListener {
                            Log.d("TripModel", "Review aggiunta con successo su Firebase")
                        }
                        .addOnFailureListener { e ->
                            Log.e("TripModel", "Errore aggiunta review su Firebase: ${e.message}")
                        }
                }
            }.addOnFailureListener {
                Log.e("TripModel", "Errore ricerca viaggio per aggiungere review: ${it.message}")
            }
        }

        fun addMemberReview(tripId: Int, authorId: Int?, reviewedId: Int, rating: Int, body: String) {
            val tripDocQuery = Collections.trips.whereEqualTo("id", tripId).limit(1)
            tripDocQuery.get().addOnSuccessListener { querySnapshot ->
                val doc = querySnapshot.documents.firstOrNull()
                if (doc != null) {
                    val currentMemberReviews =
                        (doc.get("memberReviews") as? List<Map<String, Any?>>) ?: emptyList()

                    val memberReviewMap = mapOf(
                        "author" to authorId?.toLong(),
                        "reviewed" to reviewedId.toLong(),
                        "rating" to rating.toLong(),
                        "body" to body
                    )

                    val updatedMemberReviews = currentMemberReviews + memberReviewMap

                    doc.reference.update("memberReviews", updatedMemberReviews)
                        .addOnSuccessListener {
                            Log.d("TripModel", "Member review aggiunta con successo su Firebase")
                        }
                        .addOnFailureListener { e ->
                            Log.e(
                                "TripModel",
                                "Errore aggiunta member review su Firebase: ${e.message}"
                            )
                        }
                }
            }.addOnFailureListener {
                Log.e("TripModel", "Errore ricerca viaggio per aggiungere member review: ${it.message}")
            }
        }

        fun removeUser(toRemove: Int) {
            _travelProposalsList.value.forEach { trip ->
                val tripDocQuery = Collections.trips.whereEqualTo("id", trip.id).limit(1)
                tripDocQuery.get().addOnSuccessListener { querySnapshot ->
                    val doc = querySnapshot.documents.firstOrNull()
                    if (doc != null) {
                        if (trip.author.id == toRemove) {
                            // Se il viaggio è di questo author, cancellalo
                            doc.reference.delete()
                                .addOnSuccessListener {
                                    Log.d("TripModel", "Viaggio rimosso da Firebase: ${trip.id}")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("TripModel", "Errore rimozione viaggio: ${e.message}")
                                }
                        } else {
                            // Altrimenti aggiorna la lista requests filtrata
                            val updatedRequests = trip.requests.filter { req ->
                                req.user.id!=toRemove
                            }
                            doc.reference.update("requests", updatedRequests)
                                .addOnSuccessListener {
                                    Log.d("TripModel", "Requests aggiornate per viaggio ${trip.id}")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("TripModel", "Errore aggiornamento requests: ${e.message}")
                                }
                        }
                    }
                }.addOnFailureListener {
                    Log.e("TripModel", "Errore ricerca viaggio per aggiornare utente: ${it.message}")
                }
            }
        }
    }

