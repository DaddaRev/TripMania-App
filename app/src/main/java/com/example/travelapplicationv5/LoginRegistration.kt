package com.example.travelapplicationv5

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.runtime.getValue



@Composable
fun LogInScreen(
    navController: NavController,
    viewModel: UserProfileScreenViewModel = viewModel(factory = Factory),
    phase: String
) {
    val context = LocalContext.current
    val authPhase by viewModel.authPhase.collectAsState()
    val authError by viewModel.authError.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val userId = result.data?.getStringExtra("userId")?: ""
            val email = result.data?.getStringExtra("userEmail")?: ""
            val phone = result.data?.getStringExtra("userPhone")?: ""
            val name = result.data?.getStringExtra("userName")?: ""

            val isRegistered = viewModel.isRegistered(email) //null or userprofile

            if (authPhase=="login") {
                if (isRegistered == null) {
                    viewModel.setAuthError("No account found. Sign up required")
                } else {
                    viewModel.userLogIn(isRegistered)
                    navController.navigate("list")
                }
            }
            else if (authPhase=="registration"){
                if (isRegistered == null) {
                    viewModel.loadDataForRegistration(email, phone, name)
                    Log.d("DEBUGG", "No REG")
                    navController.navigate("registration/2")
                } else {
                    viewModel.setLoginPhase()
                    viewModel.setAuthError("An account with this email already exists. Please log in instead")
                }
            }
        } else {
            viewModel.setAuthError("Authentication failed")
        }
    }

    TopBar(navController)

    if (phase=="login") {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(150.dp))
            Text(
                text = "Sign in",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(150.dp))
            Button(
                onClick = {
                    viewModel.setLoginPhase()
                    val intent = Intent(context, SignInUpActivity::class.java)
                    launcher.launch(intent)
                },
                modifier = Modifier
                    .height(50.dp),
            ) {
                Text("Login with Google", color = Color.White)
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row {
                Text("Not registered yet?")
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Sign up here with Google",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable {
                        viewModel.setRegistrationPhase()
                        val intent = Intent(context, SignInUpActivity::class.java)
                        launcher.launch(intent)
                    }
                )
            }
            if (authError!=""){
                Spacer(modifier = Modifier.height(120.dp))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = authError,
                        color = Color.Red,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

    }
    else if(phase=="registration2"){
        EditProfileScreen(navController, viewModel, true)
    }
    else if(phase=="registration3"){
        EditableTravelPreferences(navController, viewModel, true)
    }
}