package com.example.warehouseapp.model

import java.util.Date

data class Order(
    val id: String,  // MongoDB ObjectId as a string
    val customerId: String,  // ID from the Users collection
    val items: List<OrderItem>,
    val orderTotal: Double,
    val orderDate: Date,
    val status: String,  // Example: "Pending", "Shipped", "Delivered"
    val transactionId: String,  // Reference to a transaction
    val createdAt: Date
)

data class OrderItem(
    val productId: String,  // MongoDB ObjectId as a string
    val productName: String,
    val price: Double,
    val quantity: Int
)