package com.example.travelapplicationv5

import android.app.Activity
import android.content.Intent
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import android.os.Bundle
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class SignInUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var credentialManager: CredentialManager

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        auth = Firebase.auth
        credentialManager = CredentialManager.create(baseContext)

        launchCredentialManager()
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
    }

    private fun launchCredentialManager() {

        // Instantiate a Google sign-in request
        val googleIdOption = GetGoogleIdOption.Builder()
            // Your server's client ID, not your Android client ID.
            .setServerClientId(getString(R.string.default_web_client_id))
            // Only show accounts previously used to sign in.
            .setFilterByAuthorizedAccounts(true)
            .build()

        // Create the Credential Manager request
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                // Launch Credential Manager UI
                val result = credentialManager.getCredential(
                    context = this@SignInUpActivity,
                    request = request
                )
                // Extract credential from the result returned by Credential Manager
                handleSignIn(result.credential)
            } catch (e: GetCredentialException) {
                Log.e("google", "Couldn't retrieve user's credentials: ${e.localizedMessage}")

                // ********** Registration **************
                // if there are no credentials, just sign up the user for the first time
                // using a new google account
                val signUpOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)  // Allow new accounts
                    .setServerClientId(getString(R.string.default_web_client_id))
                    .setAutoSelectEnabled(false)
                    .build()

                val signUpRequest = GetCredentialRequest.Builder()
                    .addCredentialOption(signUpOption)
                    .build()

                try {
                    val signUpResult = credentialManager.getCredential(
                        context = baseContext,
                        request = signUpRequest
                    )
                    handleSignIn(signUpResult.credential)
                } catch (signupError: GetCredentialException) {
                    Log.e("google", "Sign-up failed: ${signupError.localizedMessage}", signupError)
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }

            }
        }
    }

    private fun handleSignIn(credential: Credential) {
        // Check if credential is of type Google ID
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            // Create Google ID Token
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val idToken = googleIdTokenCredential.idToken
            if (idToken == null) {
                Log.e("google", "Received null ID token")
                setResult(Activity.RESULT_CANCELED)
                finish()
                return
            }
            // Sign in to Firebase with using the token
            firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
        } else {
            Log.w("google", "Credential is not of type Google ID!")
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    // Distinction between new user or not
                    val isNewUser = task.result?.additionalUserInfo?.isNewUser ?: false
                    val user = auth.currentUser

                    val resultIntent = Intent().apply {
                        putExtra("isNewUser", isNewUser)
                        putExtra("userId", user?.uid.toString())
                        putExtra("userEmail", user?.email)
                        putExtra("userPhone", user?.phoneNumber)
                        putExtra("userName", user?.displayName)
                    }

                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()

                } else {
                    // If sign in fails, display a message to the user
                    Log.w("google", "signInWithCredential:failure", task.exception)
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
            }
    }
}


