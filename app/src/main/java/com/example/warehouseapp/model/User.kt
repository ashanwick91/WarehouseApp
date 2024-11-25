package com.example.warehouseapp.model

import java.time.OffsetDateTime

data class User(
    val id: String,  // MongoDB ObjectId as a string
    val email: String,
    val role: String,  // Either "admin" or "customer"
    val isApproved: Boolean,
    val profile: Profile?,
    val createdAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?
)

data class Profile(
    val firstName: String,
    val lastName: String,
    val address: String,
    val phoneNumber: String
)