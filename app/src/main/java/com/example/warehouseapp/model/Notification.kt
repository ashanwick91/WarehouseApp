package com.example.warehouseapp.model

import java.util.Date

data class Notification(
    val id: String,  // MongoDB ObjectId as a string
    val adminId: String,  // Admin ID to receive the notification
    val productId: String,  // Product that triggered the notification
    val productName: String,  // Name of the product for context
    val message: String,  // Notification message (e.g., "Stock below threshold")
    val isRead: Boolean = false,  // Mark as read/unread
    val createdAt: Date
)