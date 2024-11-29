package com.example.warehouseapp.model

import java.time.OffsetDateTime

data class ActivityLog (
    val userId: String, // ID of the user performing the action
    val action: String, // Description of the activity (e.g., "Login", "View Product")
    val details: String, // Additional details about the activity
    val timestamp: OffsetDateTime, // Timestamp of when the activity occurred
    val metadata: Metadata // Metadata related to the activity
)

data class Metadata(
    val device: String,
    val ip: String,
    val location: String,
    val role: String
)