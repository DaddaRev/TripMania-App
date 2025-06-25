package com.example.travelapplicationv5

import android.R.attr.maxHeight
import android.R.attr.maxWidth
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration

@Composable
fun DisplayUserReviewScreen(
    navController: NavController,
    userId: Int,
    viewModel: UserProfileScreenViewModel = viewModel(factory = Factory)
) {
    LaunchedEffect(Unit) {
        viewModel.loadUserReviews(userId)
    }

    val context = LocalContext.current
    val reviews by viewModel.userReviews.collectAsState()

    Column(Modifier.fillMaxSize()) {
        TopBar(navController)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            val portrait = maxWidth < maxHeight

            LazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Spacer(Modifier.height(15.dp))

                    if (portrait) {
                        //Image:
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(top = 10.dp)) {
                                ProfileImage(context, viewModel, false)
                            }
                            Spacer(Modifier.width(10.dp))
                            UserReviewColumn(navController, viewModel)
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
                                UserReviewColumn(navController, viewModel)
                            }
                        }
                    }

                    Column {
                        Spacer(Modifier.height(20.dp))
                        Text("Reviews", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))
                        Spacer(Modifier.height(10.dp))
                    }
                }
                if (reviews.size==0){
                    item{
                        Text(
                            text = "No reviews have been submitted yet",
                            style = MaterialTheme.typography.bodyMedium
                        )}
                    }
                items(reviews.size) { index ->
                    val review = reviews[index]
                    val removed = viewModel.isRemoved(review.author.id)
                    val trip = viewModel.getTripById(review.tripId)

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UserData(navController, user = review.author, removed = removed)
                        Spacer(modifier = Modifier.weight(1f))
                        RatingStars(review.rating.toDouble())
                    }
                    Row(modifier = Modifier
                        .fillMaxWidth().padding(top = 7.dp, bottom = 5.dp),
                    ) {
                        Text(
                            text = "about ",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = trip.title,
                            modifier = Modifier.padding(top = 2.dp).clickable {
                                navController.navigate("detail/${review.tripId}")
                            },
                            style = TextStyle(
                                textDecoration = TextDecoration.Underline
                            )
                        )
                        Text(
                            text = " :",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    Row(modifier = Modifier
                        .fillMaxWidth().padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = review.body,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                item {
                    Spacer(Modifier.height(30.dp))
                }
            }
        }
    }
}

@Composable
fun UserReviewColumn(
    navController: NavController,
    viewModel: UserProfileScreenViewModel = viewModel(factory = Factory),
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val travelProposalsList by viewModel.travelProposalsList.collectAsState()

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
            IconButton(
                onClick = {
                    //navController.navigate("profile/${userProfile.id}")
                },
                modifier = Modifier.size(32.dp).alpha(0f),
            ) {
                Icon(Icons.Default.Cancel, contentDescription = "Back")
            }
        }

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

        Row(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            RatingStars(computeAverageRating(collectUserReviews(userProfile.id, travelProposalsList)))
        }
        Spacer(Modifier.height(10.dp))
    }
}