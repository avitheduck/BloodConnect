package com.duckco.bloodconnect

/**
 * Data class representing a user in Firestore.
 * The default values are necessary for Firebase to deserialize documents into User objects.
 */
data class User(
    val name: String = "",
    val email: String = "",
    val bloodType: String = ""
)
