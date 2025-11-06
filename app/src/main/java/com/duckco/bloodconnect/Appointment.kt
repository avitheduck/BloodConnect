package com.duckco.bloodconnect

/**
 * Represents a single scheduled appointment.
 * The empty constructor and default values are required for Firebase's toObject() deserialization.
 */
data class Appointment(
    val userId: String = "",
    val centerId: String = "",
    val centerName: String = "",
    val date: Long = 0
)
