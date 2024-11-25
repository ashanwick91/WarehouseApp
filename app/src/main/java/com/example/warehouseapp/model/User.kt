package com.example.warehouseapp.model

import java.util.Date

data class User(
    val id: String,  // MongoDB ObjectId as a string
    val username: String,
    val email: String,
    val passwordHash: String,  // Hashed password for security
    val role: String,  // Either "admin" or "customer"
    val profile: Profile?,
    val createdAt: Date,
    val updatedAt: Date
)

data class Profile(
    val firstName: String,
    val lastName: String,
    val address: String,
    val phoneNumber: String
)