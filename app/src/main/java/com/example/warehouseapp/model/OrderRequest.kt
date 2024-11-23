package com.example.warehouseapp.model

import java.io.Serializable
import java.time.OffsetDateTime
import java.util.Date

data class OrderRequest( // MongoDB ObjectId as a string
    val customerId: String,  // ID from the Users collection
    var items: List<OrderItemRequest>,
    val orderTotal: Double,
    val orderDate: Date,
    val status: String,  // Example: "Pending", "Shipped", "Delivered"
    val createdAt: Date,
)

data class OrderItemRequest(
    val productId: String,  // MongoDB ObjectId as a string
    val productName: String,  // Name of the product
    val category: String,  // Product category
    var salesAmount: Double,  // Total sales amount
    val profitAmount: Double,  // Profit from the transaction
    val quantitySold: Int,  // Quantity sold in this order
    val transactionDate: Date,  // Date of the transaction
    val price: Double,  // Price per unit (if needed)
    var quantity: Int  // Quantity ordered
)