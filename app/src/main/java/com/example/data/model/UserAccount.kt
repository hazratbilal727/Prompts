package com.example.data.model

/**
 * Represents the signed-in user's Google account profile.
 */
data class UserAccount(
    val id: String,
    val email: String,
    val displayName: String,
    val profilePictureUrl: String? = null,
    val idToken: String? = null,
    val signedInAt: Long = System.currentTimeMillis()
)
