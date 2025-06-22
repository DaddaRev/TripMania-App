package com.example.travelapplicationv5

import java.time.LocalDate
import java.util.Locale

// utility functions to generate hard coded example of trips and users
// will be removed after db implementation
object Utility {

    val allCountries = listOf(
        "Afghanistan",
        "Albania",
        "Algeria",
        "Andorra",
        "Angola",
        "Antigua and Barbuda",
        "Argentina",
        "Armenia",
        "Australia",
        "Austria",
        "Azerbaijan",
        "Bahamas",
        "Bahrain",
        "Bangladesh",
        "Barbados",
        "Belarus",
        "Belgium",
        "Belize",
        "Benin",
        "Bhutan",
        "Bolivia",
        "Bosnia and Herzegovina",
        "Botswana",
        "Brazil",
        "Brunei",
        "Bulgaria",
        "Burkina Faso",
        "Burundi",
        "Cabo Verde",
        "Cambodia",
        "Cameroon",
        "Canada",
        "Central African Republic",
        "Chad",
        "Chile",
        "China",
        "Colombia",
        "Comoros",
        "Congo (Congo-Brazzaville)",
        "Costa Rica",
        "Croatia",
        "Cuba",
        "Cyprus",
        "Czech Republic",
        "Democratic Republic of the Congo",
        "Denmark",
        "Djibouti",
        "Dominica",
        "Dominican Republic",
        "Ecuador",
        "Egypt",
        "El Salvador",
        "Equatorial Guinea",
        "Eritrea",
        "Estonia",
        "Eswatini",
        "Ethiopia",
        "Fiji",
        "Finland",
        "France",
        "Gabon",
        "Gambia",
        "Georgia",
        "Germany",
        "Ghana",
        "Greece",
        "Grenada",
        "Guatemala",
        "Guinea",
        "Guinea-Bissau",
        "Guyana",
        "Haiti",
        "Honduras",
        "Hungary",
        "Iceland",
        "India",
        "Indonesia",
        "Iran",
        "Iraq",
        "Ireland",
        "Israel",
        "Italy",
        "Ivory Coast",
        "Jamaica",
        "Japan",
        "Jordan",
        "Kazakhstan",
        "Kenya",
        "Kiribati",
        "Kuwait",
        "Kyrgyzstan",
        "Laos",
        "Latvia",
        "Lebanon",
        "Lesotho",
        "Liberia",
        "Libya",
        "Liechtenstein",
        "Lithuania",
        "Luxembourg",
        "Madagascar",
        "Malawi",
        "Malaysia",
        "Maldives",
        "Mali",
        "Malta",
        "Marshall Islands",
        "Mauritania",
        "Mauritius",
        "Mexico",
        "Micronesia",
        "Moldova",
        "Monaco",
        "Mongolia",
        "Montenegro",
        "Morocco",
        "Mozambique",
        "Myanmar",
        "Namibia",
        "Nauru",
        "Nepal",
        "Netherlands",
        "New Zealand",
        "Nicaragua",
        "Niger",
        "Nigeria",
        "North Korea",
        "North Macedonia",
        "Norway",
        "Oman",
        "Pakistan",
        "Palau",
        "Palestine",
        "Panama",
        "Papua New Guinea",
        "Paraguay",
        "Peru",
        "Philippines",
        "Poland",
        "Portugal",
        "Qatar",
        "Romania",
        "Russia",
        "Rwanda",
        "Saint Kitts and Nevis",
        "Saint Lucia",
        "Saint Vincent and the Grenadines",
        "Samoa",
        "San Marino",
        "Sao Tome and Principe",
        "Saudi Arabia",
        "Senegal",
        "Serbia",
        "Seychelles",
        "Sierra Leone",
        "Singapore",
        "Slovakia",
        "Slovenia",
        "Solomon Islands",
        "Somalia",
        "South Africa",
        "South Korea",
        "South Sudan",
        "Spain",
        "Sri Lanka",
        "Sudan",
        "Suriname",
        "Sweden",
        "Switzerland",
        "Syria",
        "Taiwan",
        "Tajikistan",
        "Tanzania",
        "Thailand",
        "Timor-Leste",
        "Togo",
        "Tonga",
        "Trinidad and Tobago",
        "Tunisia",
        "Turkey",
        "Turkmenistan",
        "Tuvalu",
        "Uganda",
        "Ukraine",
        "United Arab Emirates",
        "United Kingdom",
        "United States",
        "Uruguay",
        "Uzbekistan",
        "Vanuatu",
        "Vatican City",
        "Venezuela",
        "Vietnam",
        "Yemen",
        "Zambia",
        "Zimbabwe"
    )

    val annaSmith = UserProfile(
        id = 1,
        firstName = "Anna",
        lastName = "Smith",
        nickname = "Anna_Smith",
        image = null,
        imageId = null,
        imageMonogram = true,
        phoneNumber = "1234567890",
        email = "anna.smith@example.com",
        dateOfBirth = LocalDate.of(1995, 6,15 ),
        preferences = mapOf(
            "Group dimension" to listOf("2-3 people"),
            "Destinations" to listOf("sea", "city"),
            "Activity" to listOf("shopping"),
            "Transport" to listOf("train"),
            "Accomodation" to listOf("hotel")
        ),
        tripsCreated = 5,
        currentBadge = UserBadge.TRAVEL_GURU.name,
        saved = emptyList()
    )

    val user2 = UserProfile(
        id = 2,
        firstName = "Laura",
        lastName = "Bianchi",
        nickname = "Laura_Bianchi",
        image = null,
        imageId = null,
        imageMonogram = true,
        phoneNumber = "0987654321",
        email = "laura.bianchi@example.com",
        dateOfBirth = LocalDate.of(1994, 9,23 ),
        preferences = mapOf(
            "Group dimension" to listOf("10+"),
            "Destinations" to listOf("nature", "mountain"),
            "Activity" to listOf("adventure", "relax"),
            "Transport" to listOf("bus", "car"),
            "Accomodation" to listOf("campsite", "hostel")
        ),
        tripsCreated = 1,
        currentBadge = UserBadge.NOVICE.name,
        saved = emptyList()
    )

    val user3 = UserProfile(
        id = 3,
        firstName = "Emma",
        lastName = "Foster",
        nickname = "Emma_Foster",
        image = null,
        imageId = null,
        imageMonogram = true,
        phoneNumber = "1122334455",
        email = "emma.foster@example.com",
        dateOfBirth = LocalDate.of(1996, 2,10 ),
        preferences = mapOf(
            "Group dimension" to listOf("4-10 people"),
            "Destinations" to listOf("city"),
            "Activity" to listOf("culture"),
            "Transport" to listOf("plane"),
            "Accomodation" to listOf("resort")
        ),
        tripsCreated = 2,
        currentBadge = UserBadge.EXPLORER.name,
        saved = emptyList()
    )

    val user4 = UserProfile(
        4,
        "Luca",
        "Costa",
        "Lucky",
        null,
        null,
        true,
        "+39 334 7654321",
        "luca.costa@example.com",
        LocalDate.of(1995, 11,20 ),
        mapOf(
            "Group dimension" to listOf("2-3 people"),
            "Destinations" to listOf("mountain", "nature"),
            "Activity" to listOf("relax"),
            "Transport" to listOf("car"),
            "Accomodation" to listOf("apartment", "hostel")
        ),
        tripsCreated = 0,
        currentBadge = UserBadge.NOVICE.name,
        saved = emptyList()
    )

    val user5 = UserProfile(
        5,
        "Giulia",
        "Verdi",
        "Jules",
        null,
        null,
        true,
        "+39 345 9876543",
        "giulia.verdi@example.com",
        LocalDate.of(2000, 3,15 ),
        mapOf(
            "Group dimension" to listOf("10+"),
            "Destinations" to listOf("sea"),
            "Activity" to listOf("shopping", "adventure"),
            "Transport" to listOf("bus"),
            "Accomodation" to listOf("resort", "hotel")
        ),
        tripsCreated = 0,
        currentBadge = UserBadge.NOVICE.name,
        saved = emptyList()
    )

    val user6 = UserProfile(
        6,
        "Matteo",
        "Neri",
        "Matt",
        null,
        null,
        true,
        "+39 329 1112233",
        "matteo.neri@example.com",
        LocalDate.of(1992, 8,5 ),
        mapOf(
            "Group dimension" to listOf("4-10 people"),
            "Destinations" to listOf("city", "sea"),
            "Activity" to listOf("culture", "relax"),
            "Transport" to listOf("train", "car"),
            "Accomodation" to listOf("apartment")
        ),
        tripsCreated = 0,
        currentBadge = UserBadge.NOVICE.name,
        saved = emptyList()
    )

    val user7 = UserProfile(
        7,
        "Alessandro",
        "Rossi",
        "Alex",
        null,
        null,
        true,
        "+39 333 1234567",
        "alessandro.rossi@example.com",
        LocalDate.of(1988, 6,10 ),
        mapOf(
            "Group dimension" to listOf("2-3 people"),
            "Destinations" to listOf("nature"),
            "Activity" to listOf("adventure"),
            "Transport" to listOf("car"),
            "Accomodation" to listOf("campsite")
        ),
        tripsCreated = 0,
        currentBadge = UserBadge.NOVICE.name,
        saved = emptyList()
    )

    val user8 = UserProfile(
        8,
        "Federica",
        "Carlucci",
        "Fede",
        null,
        null,
        true,
        "+39 320 6549870",
        "federica.carlucci@example.com",
        LocalDate.of(1990, 1,25 ),
        mapOf(
            "Group dimension" to listOf("10+"),
            "Destinations" to listOf("city", "mountain"),
            "Activity" to listOf("culture", "shopping"),
            "Transport" to listOf("plane", "bus"),
            "Accomodation" to listOf("hotel", "resort")
        ),
        tripsCreated = 1,
        currentBadge = UserBadge.NOVICE.name,
        saved = emptyList()
    )

    val user9 = UserProfile(
        9,
        "Simone",
        "Monge",
        "Simo05",
        null,
        null,
        true,
        "+39 320 6512367",
        "simone.monge@example.com",
        LocalDate.of(2005, 1,30),
        mapOf(
            "Group dimension" to listOf("4-10 people"),
            "Destinations" to listOf("sea", "nature"),
            "Activity" to listOf("relax", "shopping"),
            "Transport" to listOf("train"),
            "Accomodation" to listOf("resort", "apartment")
        ),
        tripsCreated = 0,
        currentBadge = UserBadge.NOVICE.name,
        saved = emptyList()
    )

    val user10 = UserProfile(
        id = 10,
        firstName = "Fabrizio",
        lastName = "Giannini",
        nickname = "FabGian9",
        image = null,
        imageId = null,
        imageMonogram = false,
        phoneNumber = "+393339819",
        email = "prova@gmail.com",
        dateOfBirth = LocalDate.of(1999, 8,4 ),
        preferences = mapOf(
            "Group dimension" to listOf("2-3 people"),
            "Destinations" to listOf("mountain"),
            "Activity" to listOf("adventure", "culture"),
            "Transport" to listOf("car"),
            "Accomodation" to listOf("hostel")
        ),
        tripsCreated = 0,
        currentBadge = UserBadge.NOVICE.name,
        saved = emptyList()
    )

    fun generateUsers(): List<UserProfile> {
        val exampleUsers = listOf(
            annaSmith, user2, user3, user4, user5, user6, user7, user8, user9, user10
        )
        return exampleUsers
    }

    fun generateTrips(): List<Trip> {

        val exampleTrips = listOf(
            Trip(
                id = 102,
                author = user8,
                title = "Luxury Trip to Dubai",
                description = "5 Days Luxury in Dubai is an exclusive tour that takes you on an unforgettable five-day journey through the wonders of Dubai. You’ll visit the iconic Burj Khalifa, explore the Dubai Mall, and experience an exciting desert safari. Discover the luxury of Palm Jumeirah and enjoy stunning views of Dubai Marina. Each day combines elegance, adventure, and local culture for a truly exceptional experience.",
                countries = listOf("United Arab Emirates"),
                spotsTotal = 9,
                price = Pair(2500, 3500),
                images = listOf(
                    "data:image/jpeg;base64,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",
                    "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQntBLvO0eWa6hdldMVuyjIvpit7tNIXtX_nA&s",
                    "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQjIX68yBWMI0TToFDSUvpWh9jAWiO7PSeU6A&s"
                ),
                date = Pair(LocalDate.of(2025, 9, 1), LocalDate.of(2025, 9, 5)),
                itinerary = listOf(
                    Stop(
                        title = "Day 1 - Desert Safari",
                        date = LocalDate.of(2025, 9, 1),
                        location = "Dubai",
                        free = true,
                        activities = "Dune bashing, camel ride, BBQ dinner"
                    ),
                    Stop(
                        title = "Day 2 - Burj Khalifa Tour",
                        date = LocalDate.of(2025, 9, 2),
                        location = "Dubai Downtown",
                        free = false,
                        activities = "Observation deck visit, photography"
                    ),
                    Stop(
                        title = "Day 3 - Beach Relax",
                        date = LocalDate.of(2025, 9, 3),
                        location = "Jumeirah Beach",
                        free = true,
                        activities = "Swimming, sunbathing"
                    ),
                    Stop(
                        title = "Day 4 - Dubai Mall & Fountain Show",
                        date = LocalDate.of(2025, 9, 4),
                        location = "Dubai Mall",
                        free = false,
                        activities = "Shopping, aquarium visit, fountain show"
                    ),
                    Stop(
                        title = "Day 5 - Museum of the Future",
                        date = LocalDate.of(2025, 9, 5),
                        location = "Dubai",
                        free = false,
                        activities = "Interactive exhibits, future innovations"
                    )
                ),
                requests = listOf(
                    Request(
                        user = annaSmith,
                        status = RequestStatus.Accepted,
                        companion = 0,
                    ),
                    Request(
                        user = user5,
                        status = RequestStatus.Accepted,
                        companion = 0
                    ),
                    Request(
                        user = user3,
                        status = RequestStatus.Accepted,
                        companion = 0
                    ),
                    Request(
                        user = user6,
                        status = RequestStatus.Accepted,
                        companion = 1
                    ),
                    Request(
                        user = user7,
                        status = RequestStatus.Pending,
                        companion = 2
                    ),
                    Request(
                        user = user8,
                        status = RequestStatus.Pending,
                        companion = 0
                    ),
                    Request(
                        user = user9,
                        status = RequestStatus.Refused,
                        companion = 0
                    )
                ),
                reviews = listOf(
                    Review(
                        user5,
                        5,
                        "A trip of a lifetime! Dubai is a city like no other.",
                        "Make sure to book your tickets to Burj Khalifa early."
                    ),
                    Review(
                        user6,
                        4,
                        "Fantastic experience, but the desert safari could have been longer.",
                        "Definitely worth visiting during the cooler months."
                    )
                ),
                memberReviews = listOf(
                    MemberReview(
                        author = annaSmith,
                        reviewed = 5,
                        rating = 5,
                        body = "Giulia was incredibly helpful and always brought great energy to the group. She was a joy to travel with, especially during the mall visit."
                    ),
                    MemberReview(
                        author = user5,
                        reviewed = 1,
                        rating = 4,
                        body = "Anna was kind and respectful. She always showed interest in every activity, especially during cultural stops like the Museum of the Future."
                    ),
                    MemberReview(
                        author = user5,
                        reviewed = 6,
                        rating = 4,
                        body = "Matteo was relaxed and friendly throughout the trip. He and his companion were well-prepared and easygoing."
                    ),
                    MemberReview(
                        author = user6,
                        reviewed = 5,
                        rating = 5,
                        body = "Giulia was adventurous and excited about everything. She took amazing photos and helped coordinate some of the group activities."
                    ),
                    MemberReview(
                        author = user6,
                        reviewed = 1,
                        rating = 4,
                        body = "Anna was very sweet and thoughtful. She really appreciated the cultural parts of the trip and was always polite with everyone."
                    )
                )

            ),
            Trip(
                id = 101,
                author = annaSmith,
                title = "Travel 101", //Adventure in New Zealand
                description = "Explore the stunning landscapes of New Zealand with activities ranging from bungee jumping to relaxing hot springs.",
                countries = listOf("New Zealand"),
                spotsTotal = 10,
                price = Pair(1800, 2600),
                images = listOf(
                    "https://cdn.pixabay.com/photo/2014/12/29/18/44/beach-583172_1280.jpg",
                    "https://cdn.pixabay.com/photo/2017/03/14/19/09/wellington-2144119_1280.jpg",
                    "https://cdn.pixabay.com/photo/2014/12/29/18/44/beach-583172_1280.jpg"
                ),
                date = Pair(LocalDate.of(2025, 11, 5), LocalDate.of(2025, 11, 15)),
                itinerary = listOf(
                    Stop(
                        "Arrival and Welcome Dinner",
                        LocalDate.of(2025, 11, 5),
                        "Auckland",
                        true,
                        "Traditional Maori feast"
                    ),
                    Stop(
                        "Rotorua Adventure",
                        LocalDate.of(2025, 11, 6),
                        "Rotorua",
                        false,
                        "Geothermal parks, Maori villages"
                    ),
                    Stop(
                        "Queenstown Thrills",
                        LocalDate.of(2025, 11, 8),
                        "Queenstown",
                        false,
                        "Bungee jumping, skydiving"
                    ),
                    Stop(
                        "Relax in Hot Springs",
                        LocalDate.of(2025, 11, 10),
                        "Hanmer Springs",
                        true,
                        "Thermal pools"
                    )
                ),
                requests = listOf(
                    Request(
                        user = user3,
                        status = RequestStatus.Pending,
                        companion = 1
                    ),
                    Request(
                        user = user6,
                        status = RequestStatus.Accepted,
                        companion = 0
                    ),
                    Request(
                        user = user4,
                        status = RequestStatus.Accepted,
                        companion = 1
                    ),
                    Request(
                        user = user7,
                        status = RequestStatus.Refused,
                        companion = 1
                    )
                ),
                reviews = listOf(
                    Review(user3, 5, "Best trip of my life!", "Don't miss Queenstown!"),
                    Review(
                        user5,
                        4,
                        "Incredible landscapes, perfect for adventurers.",
                        "Pack warm clothes!"
                    )
                ),
                memberReviews = listOf(
                    MemberReview(
                        author = user6,
                        reviewed = 4,
                        rating = 5,
                        body = "Luca brought so much enthusiasm to the group. Always up for a new experience!"
                    ),
                    MemberReview(
                        author = user4,
                        reviewed = 6,
                        rating = 4,
                        body = "Matteo was organized and helped coordinate during the longer transfers. Great presence."
                    ),
                    MemberReview(
                        author = user6,
                        reviewed = 1,
                        rating = 5,
                        body = "Anna did an excellent job planning everything. The itinerary was well-balanced and smooth."
                    ),
                    MemberReview(
                        author = user4,
                        reviewed = 1,
                        rating = 4,
                        body = "Anna was kind and made sure everyone felt included, especially during the welcome dinner."
                    ),
                    MemberReview(
                        author = annaSmith,
                        reviewed = 6,
                        rating = 5,
                        body = "Matteo was always punctual and helpful. He added a calm, reliable presence to the group that everyone appreciated."
                    )
                )
            ),
            Trip(
                id = 103,
                author = user3,
                title = "Cultural Escape in Morocco",
                description = "Discover the rich culture, cuisine, and landscapes of Morocco. From bustling souks to tranquil deserts.",
                countries = listOf("Morocco"),
                spotsTotal = 8,
                price = Pair(900, 1400),
                images = listOf(
                    "https://cdn.pixabay.com/photo/2020/01/07/21/07/morocco-4748657_1280.jpg",
                    "https://cdn.pixabay.com/photo/2019/12/31/11/57/ajt-bin-haddu-4731740_1280.jpg",
                    "https://cdn.pixabay.com/photo/2019/12/30/18/36/morocco-4730279_1280.jpg"
                ),
                date = Pair(LocalDate.of(2023, 3, 10), LocalDate.of(2023, 3, 20)),
                itinerary = listOf(
                    Stop(
                        "Arrival in Marrakech",
                        LocalDate.of(2023, 3, 10),
                        "Marrakech",
                        true,
                        "Welcome dinner and traditional music show"
                    ),
                    Stop(
                        "Explore Fes Medina",
                        LocalDate.of(2023, 3, 12),
                        "Fes",
                        false,
                        "Walking tour of the old city and tanneries"
                    ),
                    Stop(
                        "Sahara Desert Experience",
                        LocalDate.of(2023, 3, 15),
                        "Merzouga",
                        false,
                        "Camel trek and night under the stars in Berber tents"
                    ),
                    Stop(
                        "Relax in Essaouira",
                        LocalDate.of(2023, 3, 18),
                        "Essaouira",
                        true,
                        "Seafood tasting and coastal walk"
                    )
                ),
                requests = listOf(
                    Request(
                        user = annaSmith,
                        status = RequestStatus.Accepted,
                        companion = 1
                    ),
                    Request(
                        user = user2,
                        status = RequestStatus.Accepted,
                        companion = 0
                    ),
                    Request(
                        user = user6,
                        status = RequestStatus.Refused,
                        companion = 1
                    )
                ),
                reviews = listOf(
                    Review(
                        annaSmith,
                        5,
                        "An unforgettable cultural immersion. The Sahara was magical.",
                        "Bring sunscreen and a good camera!"
                    ),
                    Review(
                        user2,
                        4,
                        "Loved the food and the people. Marrakech is chaotic but fun.",
                        "Be ready to negotiate prices everywhere."
                    )
                ),
                memberReviews = listOf(
                    MemberReview(
                        author = annaSmith,
                        reviewed = 2,
                        rating = 5,
                        body = "Laura was an amazing travel companion! Always enthusiastic and super helpful when navigating the souks."
                    ),
                    MemberReview(
                        author = user2,
                        reviewed = 1,
                        rating = 5,
                        body = "Anna brought such great energy. We shared so many laughs during the camel trek!"
                    ),
                    MemberReview(
                        author = user3,
                        reviewed = 1,
                        rating = 5,
                        body = "Anna’s positive attitude made the whole experience better for everyone. I really appreciated her kindness throughout the trip."
                    )
                )
            ),
            Trip(
                id = 104,
                author = annaSmith,
                title = "Cultural Tour of Japan",
                description = "Dive into Japan's rich culture with visits to ancient temples, bustling cities, and serene countryside.",
                countries = listOf("Japan"),
                spotsTotal = 12,
                price = Pair(3000, 4500),
                images = listOf(
                    "https://cdn.pixabay.com/photo/2020/08/10/01/28/castle-5476737_1280.jpg",
                    "https://cdn.pixabay.com/photo/2020/09/14/22/27/river-5572289_1280.jpg",
                    "https://cdn.pixabay.com/photo/2020/08/10/01/28/castle-5476737_1280.jpg"
                ),
                date = Pair(LocalDate.of(2025, 10, 20), LocalDate.of(2025, 10, 31)),
                itinerary = listOf(
                    Stop(
                        "Arrival in Tokyo",
                        LocalDate.of(2025, 3, 20),
                        "Tokyo",
                        true,
                        "Explore Shibuya and Asakusa"
                    ),
                    Stop(
                        "Kyoto Temples",
                        LocalDate.of(2025, 3, 22),
                        "Kyoto",
                        false,
                        "Kinkaku-ji, Fushimi Inari Shrine"
                    ),
                    Stop(
                        "Nara Deer Park",
                        LocalDate.of(2025, 3, 24),
                        "Nara",
                        true,
                        "Feed the sacred deer"
                    ),
                    Stop(
                        "Mt. Fuji Excursion",
                        LocalDate.of(2025, 3, 26),
                        "Fuji Five Lakes",
                        false,
                        "Hiking and sightseeing"
                    )
                ),
                requests = listOf(
                    Request(
                        user = user5,
                        status = RequestStatus.Pending,
                        companion = 0
                    ),
                    Request(
                        user = user6,
                        status = RequestStatus.Accepted,
                        companion = 5
                    ),
                    Request(
                        user = user7,
                        status = RequestStatus.Accepted,
                        companion = 2
                    ),
                    Request(
                        user = user4,
                        status = RequestStatus.Refused,
                        companion = 1
                    )
                ),
                reviews = listOf(
                    Review(
                        user6,
                        5,
                        "The cherry blossoms were breathtaking!",
                        "Visit during Sakura season!"
                    ),
                    Review(
                        user4,
                        4,
                        "Amazing cultural experience.",
                        "Learn basic Japanese phrases!"
                    )
                ),
                memberReviews = listOf(
                    MemberReview(
                        author = user6,
                        reviewed = 7,
                        rating = 5,
                        body = "Alessandro was a great travel partner—always punctual and very respectful of local customs."
                    ),
                    MemberReview(
                        author = user7,
                        reviewed = 6,
                        rating = 4,
                        body = "Matteo was knowledgeable and helped us navigate Japan with ease. A real team player."
                    ),
                    MemberReview(
                        author = annaSmith,
                        reviewed = 7,
                        rating = 5,
                        body = "Alessandro brought great energy to the trip. His curiosity and kindness made the journey even more enjoyable!"
                    )
                )
            ),
            Trip(
                id = 105,
                author = user2,
                title = "Romantic Escape in Italy",
                description = "Discover the art, cuisine, and romance of Italy with this 10-day journey across iconic cities and hidden gems.",
                countries = listOf("Italy"),
                spotsTotal = 8,
                price = Pair(2200, 3300),
                images = listOf(
                    "https://cdn.pixabay.com/photo/2019/08/21/09/38/pisa-4420684_1280.jpg",
                    "https://cdn.pixabay.com/photo/2019/08/21/09/38/pisa-4420684_1280.jpg",
                    "https://cdn.pixabay.com/photo/2018/02/26/14/22/venice-3183168_1280.jpg"
                ),
                date = Pair(LocalDate.of(2025, 9, 10), LocalDate.of(2025, 9, 20)),
                itinerary = listOf(
                    Stop(
                        "Arrival in Rome",
                        LocalDate.of(2025, 5, 10),
                        "Rome",
                        true,
                        "Visit the Colosseum and Roman Forum"
                    ),
                    Stop(
                        "Florence Art Tour",
                        LocalDate.of(2025, 5, 12),
                        "Florence",
                        false,
                        "Uffizi Gallery, Duomo Cathedral"
                    ),
                    Stop(
                        "Venetian Canals",
                        LocalDate.of(2025, 5, 15),
                        "Venice",
                        true,
                        "Gondola ride, Piazza San Marco"
                    ),
                    Stop(
                        "Tuscany Wine Tasting",
                        LocalDate.of(2025, 5, 17),
                        "Chianti Region",
                        false,
                        "Vineyard tour and tasting"
                    )
                ),
                requests = listOf(
                    Request(
                        user = user7,
                        status = RequestStatus.Pending,
                        companion = 1
                    ),
                    Request(
                        user = user6,
                        status = RequestStatus.Accepted,
                        companion = 0
                    ),
                    Request(
                        user = annaSmith,
                        status = RequestStatus.Accepted,
                        companion = 1
                    ),
                    Request(
                        user = user3,
                        status = RequestStatus.Refused,
                        companion = 0
                    )
                ),
                reviews = listOf(
                    Review(
                        annaSmith,
                        5,
                        "A dream come true for food and art lovers!",
                        "Comfortable walking shoes are a must!"
                    ),
                    Review(
                        user8,
                        4,
                        "Romantic and unforgettable experience.",
                        "Try local trattorias instead of tourist spots."
                    )
                ),
                memberReviews = listOf(
                    MemberReview(
                        author = user6,
                        reviewed = 1,
                        rating = 5,
                        body = "Anna was amazing to travel with! Always enthusiastic and shared great knowledge about the places we visited."
                    ),
                    MemberReview(
                        author = annaSmith,
                        reviewed = 6,
                        rating = 4,
                        body = "Matteo was super organized and helped plan our evenings. We made a great team!"
                    ),
                    MemberReview(
                        author = user2,
                        reviewed = 1,
                        rating = 5,
                        body = "Anna brought so much joy to the group. Her positivity and energy made every stop even more enjoyable!"
                    )
                )
            ),
            Trip(
                id = 106,
                author = annaSmith,
                title = "Safari Adventure in Kenya",
                description = "Experience breathtaking safaris and witness Africa's majestic wildlife across Kenya’s top national parks.",
                countries = listOf("Kenya"),
                spotsTotal = 6,
                price = Pair(3500, 5000),
                images = listOf(
                    "https://cdn.pixabay.com/photo/2022/06/02/19/25/giraffes-7238815_1280.jpg",
                    "https://cdn.pixabay.com/photo/2019/06/15/14/40/elephants-4275741_1280.jpg",
                    "https://cdn.pixabay.com/photo/2022/06/02/19/25/giraffes-7238815_1280.jpg"
                ),
                date = Pair(LocalDate.of(2025, 7, 2), LocalDate.of(2025, 7, 12)),
                itinerary = listOf(
                    Stop(
                        "Arrival in Nairobi",
                        LocalDate.of(2025, 7, 2),
                        "Nairobi",
                        true,
                        "Explore local markets and museums"
                    ),
                    Stop(
                        "Maasai Mara Safari",
                        LocalDate.of(2025, 7, 4),
                        "Maasai Mara",
                        false,
                        "Game drives, big five sightings"
                    ),
                    Stop(
                        "Lake Nakuru Visit",
                        LocalDate.of(2025, 7, 7),
                        "Lake Nakuru",
                        true,
                        "Flamingo watching, rhino sanctuary"
                    ),
                    Stop(
                        "Amboseli National Park",
                        LocalDate.of(2025, 7, 9),
                        "Amboseli",
                        false,
                        "Mt. Kilimanjaro backdrop photos"
                    )
                ),
                requests = listOf(
                    Request(
                        user = user4,
                        status = RequestStatus.Pending,
                        companion = 0
                    ),
                    Request(
                        user = user2,
                        status = RequestStatus.Accepted,
                        companion = 1
                    ),
                    Request(
                        user = user3,
                        status = RequestStatus.Accepted,
                        companion = 1
                    ),
                    Request(
                        user = user5,
                        status = RequestStatus.Refused,
                        companion = 0
                    )
                ),
                reviews = listOf(
                    Review(
                        user7,
                        5,
                        "Absolutely incredible wildlife sightings!",
                        "Pack a good camera and binoculars."
                    ),
                    Review(
                        user4,
                        5,
                        "Bucket-list trip, highly recommended!",
                        "Bring layered clothing for cold mornings."
                    )
                ),
                memberReviews = listOf(
                    MemberReview(
                        author = user2,
                        reviewed = 3,
                        rating = 5,
                        body = "Emma was so curious and respectful throughout the whole trip. Her enthusiasm for nature made every stop more meaningful."
                    ),
                    MemberReview(
                        author = user3,
                        reviewed = 2,
                        rating = 4,
                        body = "Laura was super friendly and always ready to help. She also captured amazing photos we all appreciated!"
                    ),
                    MemberReview(
                        author = annaSmith,
                        reviewed = 2,
                        rating = 5,
                        body = "Laura brought great energy to the group. Always punctual and fun to be around!"
                    )
                )
            ),
            Trip(
                id = 107,
                author = annaSmith,
                title = "Road Trip through the USA West Coast",
                description = "Hit the road for an epic USA west coast journey from San Francisco to Las Vegas, passing national parks and beaches.",
                countries = listOf("United States"),
                spotsTotal = 15,
                price = Pair(2000, 3200),
                images = listOf(
                    "https://cdn.pixabay.com/photo/2021/12/09/11/53/empire-state-building-6858030_1280.jpg",
                    "https://cdn.pixabay.com/photo/2023/02/10/16/07/new-york-7781184_1280.jpg",
                    "https://cdn.pixabay.com/photo/2021/12/09/11/53/empire-state-building-6858030_1280.jpg"
                ),
                date = Pair(LocalDate.of(2025, 8, 1), LocalDate.of(2025, 8, 20)),
                itinerary = listOf(
                    Stop(
                        "Start in San Francisco",
                        LocalDate.of(2025, 8, 1),
                        "San Francisco",
                        true,
                        "Golden Gate Bridge, Alcatraz"
                    ),
                    Stop(
                        "Yosemite National Park",
                        LocalDate.of(2025, 8, 4),
                        "Yosemite",
                        false,
                        "Hiking and sightseeing"
                    ),
                    Stop(
                        "Los Angeles Exploration",
                        LocalDate.of(2025, 8, 8),
                        "Los Angeles",
                        true,
                        "Hollywood, Santa Monica"
                    ),
                    Stop(
                        "Grand Canyon Visit",
                        LocalDate.of(2025, 8, 14),
                        "Grand Canyon",
                        false,
                        "Sunset at the South Rim"
                    )
                ),
                requests = listOf(
                    Request(
                        user = user3,
                        status = RequestStatus.Pending,
                        companion = 0
                    ),
                    Request(
                        user = user5,
                        status = RequestStatus.Accepted,
                        companion = 0
                    ),
                    Request(
                        user = user6,
                        status = RequestStatus.Accepted,
                        companion = 2
                    ),
                    Request(
                        user = user2,
                        status = RequestStatus.Accepted,
                        companion = 1
                    ),
                    Request(
                        user = user4,
                        status = RequestStatus.Refused,
                        companion = 0
                    )
                ),
                reviews = listOf(
                    Review(
                        user2,
                        5,
                        "Every stop was iconic and unforgettable!",
                        "Plan extra time for Yosemite trails."
                    ),
                    Review(
                        user5,
                        4,
                        "Perfect mix of cities and nature.",
                        "Book Grand Canyon tickets early!"
                    )
                ),
                memberReviews = listOf(
                    MemberReview(
                        author = user5,
                        reviewed = 6,
                        rating = 5,
                        body = "Matteo was super helpful and always organized. His road trip playlists were the best!"
                    ),
                    MemberReview(
                        author = user6,
                        reviewed = 2,
                        rating = 4,
                        body = "Laura was very fun to travel with and always ready to explore. Great vibe!"
                    ),
                    MemberReview(
                        author = user2,
                        reviewed = 5,
                        rating = 5,
                        body = "Giulia kept the spirits high throughout the trip. Always smiling and adventurous!"
                    ),
                    MemberReview(
                        author = annaSmith,
                        reviewed = 6,
                        rating = 5,
                        body = "Matteo was fantastic on the road, super reliable and great company at every stop!"
                    )
                )
            ),
            Trip(
                id = 108,
                author = annaSmith,
                title = "Tropical Escape to Maldives",
                description = "Relax in paradise with this luxurious trip to the Maldives, featuring overwater villas, snorkeling, and spa treatments.",
                countries = listOf("Maldives"),
                spotsTotal = 4,
                price = Pair(4000, 6000),
                images = listOf(
                    "https://cdn.pixabay.com/photo/2015/03/09/18/34/beach-666122_1280.jpg",
                    "https://cdn.pixabay.com/photo/2017/02/20/19/29/four-seasons-2083682_1280.jpg",
                    "https://cdn.pixabay.com/photo/2015/03/09/18/34/beach-666122_1280.jpg"
                ),
                date = Pair(LocalDate.of(2025, 12, 5), LocalDate.of(2025, 12, 12)),
                itinerary = listOf(
                    Stop(
                        "Arrival at Resort",
                        LocalDate.of(2025, 12, 5),
                        "Male",
                        true,
                        "Relaxation and welcome drinks"
                    ),
                    Stop(
                        "Snorkeling Adventure",
                        LocalDate.of(2025, 12, 6),
                        "Coral Reefs",
                        false,
                        "Snorkel among tropical fish"
                    ),
                    Stop(
                        "Spa and Wellness Day",
                        LocalDate.of(2025, 12, 8),
                        "Resort Spa",
                        true,
                        "Massages and wellness treatments"
                    ),
                    Stop(
                        "Island Hopping",
                        LocalDate.of(2025, 12, 10),
                        "Various Islands",
                        false,
                        "Explore nearby islands by boat"
                    )
                ),
                requests = listOf(
                    Request(
                        user = user2,
                        status = RequestStatus.Pending,
                        companion = 0
                    ),
                    Request(
                        user = user3,
                        status = RequestStatus.Accepted,
                        companion = 2
                    ),
                    Request(
                        user = user5,
                        status = RequestStatus.Refused,
                        companion = 1
                    )
                ),
                reviews = listOf(
                    Review(
                        user2,
                        5,
                        "The most relaxing vacation ever!",
                        "Bring reef-safe sunscreen!"
                    ),
                    Review(
                        user8,
                        5,
                        "Pure paradise, unforgettable sunsets!",
                        "Try a traditional Maldivian dinner."
                    )
                ),
                memberReviews = listOf(
                    MemberReview(
                        author = user3,
                        reviewed = 1,
                        rating = 5,
                        body = "Anna was a wonderful host. Everything was perfectly organized and relaxing from start to finish."
                    ),
                    MemberReview(
                        author = annaSmith,
                        reviewed = 3,
                        rating = 5,
                        body = "Emma was a great travel companion—always kind and genuinely amazed by every sunset!"
                    )
                )
            ),
            Trip(
                id = 109,
                author = annaSmith,
                title = "South America Discovery: Peru & Bolivia",
                description = "An adventurous journey across Peru and Bolivia, exploring Machu Picchu, Lake Titicaca, and the Uyuni Salt Flats.",
                countries = listOf("Peru", "Bolivia"),
                spotsTotal = 10,
                price = Pair(2800, 4000),
                images = listOf(
                    "https://cdn.pixabay.com/photo/2017/06/01/13/19/peru-2363507_1280.jpg",
                    "https://cdn.pixabay.com/photo/2020/03/10/13/53/flamingos-4919079_1280.jpg",
                    "https://cdn.pixabay.com/photo/2017/06/01/13/19/peru-2363507_1280.jpg"
                ),
                date = Pair(LocalDate.of(2025, 9, 10), LocalDate.of(2025, 9, 25)),
                itinerary = listOf(
                    Stop(
                        "Arrival in Lima",
                        LocalDate.of(2025, 9, 10),
                        "Lima",
                        true,
                        "City tour and local food tasting"
                    ),
                    Stop(
                        "Cusco & Sacred Valley",
                        LocalDate.of(2025, 9, 12),
                        "Cusco",
                        false,
                        "Visit ruins and local markets"
                    ),
                    Stop(
                        "Machu Picchu Visit",
                        LocalDate.of(2025, 9, 15),
                        "Machu Picchu",
                        false,
                        "Guided tour of the ancient city"
                    ),
                    Stop(
                        "Uyuni Salt Flats",
                        LocalDate.of(2025, 9, 20),
                        "Uyuni",
                        false,
                        "4x4 tour across the salt flats"
                    )
                ),
                requests = listOf(
                    Request(
                        user = user7,
                        status = RequestStatus.Pending,
                        companion = 1
                    ),
                    Request(
                        user = user6,
                        status = RequestStatus.Accepted,
                        companion = 1
                    ),
                    Request(
                        user = user5,
                        status = RequestStatus.Accepted,
                        companion = 1
                    ),
                    Request(
                        user = user3,
                        status = RequestStatus.Refused,
                        companion = 0
                    )
                ),
                reviews = listOf(
                    Review(
                        user6,
                        5,
                        "Machu Picchu was breathtaking and Uyuni surreal!",
                        "Acclimate for altitude first!"
                    ),
                    Review(
                        user7,
                        4,
                        "Spectacular views, but very tiring trip.",
                        "Bring good hiking shoes!"
                    )
                ),
                memberReviews = listOf(
                    MemberReview(
                        author = user6,
                        reviewed = 5,
                        rating = 5,
                        body = "Giulia brought great energy to the group—always cheerful and ready to explore!"
                    ),
                    MemberReview(
                        author = user5,
                        reviewed = 6,
                        rating = 5,
                        body = "Matteo was the perfect travel companion—super organized and helpful throughout the journey."
                    ),
                    MemberReview(
                        author = annaSmith,
                        reviewed = 6,
                        rating = 5,
                        body = "Matteo was dependable and easy to travel with—especially during the high-altitude hikes!"
                    ),
                    MemberReview(
                        author = annaSmith,
                        reviewed = 5,
                        rating = 5,
                        body = "Giulia was curious, engaged, and added so much joy to the experience!"
                    )
                )
            ),
            Trip(
                id = 110,
                author = user3,
                title = "Nordic Lights Tour: Norway & Sweden",
                description = "Chase the northern lights while discovering the beauty of Scandinavian cities and snowy landscapes.",
                countries = listOf("Norway", "Sweden"),
                spotsTotal = 8,
                price = Pair(3200, 4700),
                images = listOf(
                    "https://cdn.pixabay.com/photo/2020/03/26/10/58/norway-4970080_1280.jpg",
                    "https://cdn.pixabay.com/photo/2022/08/21/17/47/color-7401750_1280.jpg",
                    "https://cdn.pixabay.com/photo/2020/03/26/10/58/norway-4970080_1280.jpg"
                ),
                date = Pair(LocalDate.of(2025, 2, 5), LocalDate.of(2025, 2, 15)),
                itinerary = listOf(
                    Stop(
                        "Arrival in Oslo",
                        LocalDate.of(2025, 2, 5),
                        "Oslo",
                        true,
                        "Viking Museum and city sightseeing"
                    ),
                    Stop(
                        "Northern Lights in Tromsø",
                        LocalDate.of(2025, 2, 7),
                        "Tromsø",
                        false,
                        "Northern lights safari"
                    ),
                    Stop(
                        "Train to Sweden",
                        LocalDate.of(2025, 2, 10),
                        "Kiruna",
                        true,
                        "Ice hotel visit and dog sledding"
                    ),
                    Stop(
                        "Stockholm Exploration",
                        LocalDate.of(2025, 2, 13),
                        "Stockholm",
                        false,
                        "Gamla Stan and Vasa Museum"
                    )
                ),
                requests = listOf(
                    Request(
                        user = user2,
                        status = RequestStatus.Pending,
                        companion = 1
                    ),
                    Request(
                        user = user4,
                        status = RequestStatus.Accepted,
                        companion = 1
                    ),
                    Request(
                        user = user7,
                        status = RequestStatus.Accepted,
                        companion = 1
                    ),
                    Request(
                        user = user6,
                        status = RequestStatus.Refused,
                        companion = 1
                    )
                ),
                reviews = listOf(
                    Review(
                        user4,
                        5,
                        "Seeing the Aurora Borealis was magical!",
                        "Dress extremely warmly, it's freezing!"
                    ),
                    Review(
                        user2,
                        4,
                        "Perfect winter wonderland trip.",
                        "Tromsø is the best for northern lights!"
                    )
                ),
                memberReviews = listOf(
                    MemberReview(
                        author = user3,
                        reviewed = 4,
                        rating = 5,
                        body = "Luca was enthusiastic and always ready to explore—even during the coldest nights!"
                    ),
                    MemberReview(
                        author = user3,
                        reviewed = 7,
                        rating = 4,
                        body = "Alessandro was fun to travel with, especially during the dog sledding adventure."
                    ),
                    MemberReview(
                        author = user4,
                        reviewed = 7,
                        rating = 4,
                        body = "Alessandro was laid-back and made the long train ride enjoyable with great stories."
                    ),
                    MemberReview(
                        author = user7,
                        reviewed = 4,
                        rating = 5,
                        body = "Luca brought great vibes and took amazing photos of the aurora for everyone."
                    ),
                    MemberReview(
                        author = user4,
                        reviewed = 3,
                        rating = 5,
                        body = "Emma was incredibly well organized and made sure no one missed a stop!"
                    ),
                    MemberReview(
                        author = user7,
                        reviewed = 3,
                        rating = 5,
                        body = "Emma created a smooth and enjoyable experience, and handled everything gracefully."
                    )
                )
            )
        )

        val sortedTrips = exampleTrips.sortedBy { it.id }
        return sortedTrips
    }
}

fun computeSpotsLeft(trip: Trip): Int {
    var takenSpots = trip.requests
        .filter { it.status == RequestStatus.Accepted }
        .sumOf { 1 + it.companion }
    takenSpots += 1 // including the author

    return trip.spotsTotal - takenSpots
}

fun collectUserReviews(userId: Int, allTrips: List<Trip>): List<MemberReview> {
    return allTrips
        .flatMap { trip ->
            trip.memberReviews
                .filter { review -> review.reviewed == userId }
                .map { review ->
                    review.copy(tripId = trip.id)
                }
        }
}

fun computeAverageRating(userReviews: List<MemberReview>): Double {
    if (userReviews.isNotEmpty()) {
        val average = userReviews.map { it.rating }.average()
        return String.format(Locale.US, "%.2f", average).toDouble()
    } else {
        return 0.0
    }
}

fun Double.round(decimals: Int): Double =
    String.format(Locale.US, "%.${decimals}f", this).toDouble()
