package com.example.util

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.data.model.UserAccount
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GoogleAuthHelper(private val context: Context) {

    private val credentialManager: CredentialManager = CredentialManager.create(context)

    suspend fun signInWithGoogle(configuredClientId: String): Result<UserAccount> = withContext(Dispatchers.IO) {
        val serverClientId = configuredClientId.ifBlank {
            "729739919148-6iu7fi150qbtuaaaso8g163l687igtdv.apps.googleusercontent.com"
        }

        try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response = credentialManager.getCredential(
                request = request,
                context = context
            )

            val credential = response.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val account = UserAccount(
                    id = googleIdTokenCredential.id,
                    email = googleIdTokenCredential.id,
                    displayName = googleIdTokenCredential.displayName ?: googleIdTokenCredential.givenName ?: "Google User",
                    profilePictureUrl = googleIdTokenCredential.profilePictureUri?.toString(),
                    idToken = googleIdTokenCredential.idToken,
                    signedInAt = System.currentTimeMillis()
                )
                Result.success(account)
            } else {
                Result.failure(Exception("Unsupported credential type returned: ${credential.type}"))
            }
        } catch (e: GetCredentialCancellationException) {
            Result.failure(Exception("Sign in was cancelled by user."))
        } catch (e: NoCredentialException) {
            Result.failure(Exception("No Google accounts found on device. Please add a Google account in Android device settings."))
        } catch (e: GetCredentialException) {
            val message = e.message ?: "Authentication error"
            Result.failure(Exception("Google Sign-In: $message"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
