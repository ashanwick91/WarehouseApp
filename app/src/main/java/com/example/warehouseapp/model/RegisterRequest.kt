package com.example.warehouseapp.model

data class RegisterRequest(
    val email: String,
    val password: String,
    val role: String
)
