package com.example.warehouseapp.model

import java.io.Serializable
import java.time.OffsetDateTime

data class Order(
    val id: String,  // MongoDB ObjectId as a string
    val customerId: String,  // ID from the Users collection
    val items: List<OrderItem>,
    val orderTotal: Double,
    val orderDate: String,
    val status: String,  // Example: "Pending", "Shipped", "Delivered"
    val createdAt: String
)

data class OrderItem(
    val productId: String,  // MongoDB ObjectId as a string
    val productName: String,  // Name of the product
    val category: String,  // Product category
    var salesAmount: Double,  // Total sales amount
    val profitAmount: Double,  // Profit from the transaction
    val quantitySold: Int,  // Quantity sold in this order
    val transactionDate: String,  // Date of the transaction
    val price: Double,  // Price per unit (if needed)
    var quantity: Int  // Quantity ordered
)