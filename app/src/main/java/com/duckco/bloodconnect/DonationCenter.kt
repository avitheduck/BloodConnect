package com.duckco.bloodconnect

import com.google.firebase.firestore.GeoPoint

/**
 * Represents a single donation center location.
 * The empty constructor and default values are required for Firebase's toObject() deserialization.
 */
data class DonationCenter(
    val name: String = "",
    val address: String = "",
    val location: GeoPoint? = null
)
