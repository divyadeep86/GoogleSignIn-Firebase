package com.vh.googlesigninfirebase.signIn

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInRequest.GoogleIdTokenRequestOptions
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.vh.googlesigninfirebase.R
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
/**
 * GoogleAuthUiClient is a class designed to handle Google sign-in operations within an application.
 * It utilizes Firebase Authentication and the Google One Tap sign-in client to provide an easy way for users to sign in or sign out.
 *
 * @property context The application's current context, used for accessing resources and services.
 * @property oneTapClient An instance of SignInClient, specifically for managing Google's One Tap sign-in process.
 */
class GoogleAuthUiClient(
    private val context: Context,
    private val oneTapClient: SignInClient
) {
    private val auth = Firebase.auth // Firebase Authentication instance for handling sign-in and sign-out operations.
    
    
    /**
     * Initiates the sign-in process by creating a sign-in intent that can be used to start the Google One Tap sign-in flow.
     *
     * @return An IntentSender for launching the Google One Tap sign-in UI, or null if the process fails.
     */
    suspend fun signIn(): IntentSender? {
        val result = try {
            oneTapClient.beginSignIn(
                buildSignInRequest()
            ).await()
        } catch(e: Exception) {
            e.printStackTrace()
            if(e is CancellationException) throw e
            null
        }
        return result?.pendingIntent?.intentSender
    }
    
    
    /**
     * Handles the sign-in process after receiving the result from the Google One Tap sign-in UI.
     *
     * @param intent The intent containing the sign-in result data.
     * @return A SignInResult object containing the user data on successful sign-in or an error message on failure.
     */
    suspend fun signInWithIntent(intent: Intent): SignInResult {
        val credential = oneTapClient.getSignInCredentialFromIntent(intent)
        val googleIdToken = credential.googleIdToken
        val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken, null)
        return try {
            val user = auth.signInWithCredential(googleCredentials).await().user
            SignInResult(
                data = user?.run {
                    UserData(
                        userId = uid,
                        username = displayName,
                        profilePictureUrl = photoUrl?.toString()
                    )
                },
                errorMessage = null
            )
        } catch(e: Exception) {
            e.printStackTrace()
            if(e is CancellationException) throw e
            SignInResult(
                data = null,
                errorMessage = e.message
            )
        }
    }
    
    /**
     * Signs out the current user from both the Google One Tap client and Firebase Authentication.
     */
    suspend fun signOut() {
        try {
            oneTapClient.signOut().await()
            auth.signOut()
        } catch(e: Exception) {
            e.printStackTrace()
            if(e is CancellationException) throw e
        }
    }
    
    
    /**
     * Retrieves the currently signed-in user's data if available.
     *
     * @return UserData containing the user's information or null if no user is currently signed in.
     */
    fun getSignedInUser(): UserData? = auth.currentUser?.run {
        UserData(
            userId = uid,
            username = displayName,
            profilePictureUrl = photoUrl?.toString()
        )
    }
    
    
    /**
     * Builds and returns a BeginSignInRequest object configured with Google ID token request options.
     *
     * @return A BeginSignInRequest instance for initiating the sign-in process.
     */
    private fun buildSignInRequest(): BeginSignInRequest {
        return BeginSignInRequest.Builder()
            .setGoogleIdTokenRequestOptions(
                GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(context.getString(R.string.web_client_id))
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()
    }
}