package com.example.warehouseapp.model

import java.util.Date

data class Transaction(
    val id: String,  // MongoDB ObjectId as a string
    val customerId: String,  // ID from the Users collection
    val items: List<TransactionItem>,
    val transactionTotal: Double,
    val transactionDate: Date,
    val createdAt: Date
)

data class TransactionItem(
    val productId: String,  // MongoDB ObjectId as a string
    val productName: String,
    val price: Double,
    val quantity: Int
)
