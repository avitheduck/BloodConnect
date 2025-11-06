package com.duckco.bloodconnect

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Data class representing a single emergency blood request.
 * The default values are necessary for Firebase to be able to deserialize documents back into objects.
 */
data class EmergencyRequest(
    val patientName: String = "",
    val hospitalName: String = "",
    val location: String = "", // Added location field
    val bloodType: String = "",
    val contactNumber: String = "",
    val additionalInfo: String = "",

    @ServerTimestamp
    val timestamp: Date? = null // Automatically capture the time of creation
)
