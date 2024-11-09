package com.example.warehouseapp.model

import java.util.Date

data class AdminActivityLog(
    val id: String,  // MongoDB ObjectId as a string
    val adminId: String,  // ID of the admin performing the action
    val action: String,  // Example: "Updated Product", "Deleted User"
    val actionDetails: String,  // More descriptive log
    val createdAt: Date
)