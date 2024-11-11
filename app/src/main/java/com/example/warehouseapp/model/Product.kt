package com.example.warehouseapp.model

import java.util.Date

data class Product(
    val id: String,  // MongoDB ObjectId as a string
    val name: String,
    val description: String,
    val price: Double,
    val category: String,  // Example: "Electronics", "Furniture"
    val image: String,  // URL to the product image
    var quantity: Int,  // Current stock
    val createdAt: Date,
    val updatedAt: Date,
    //val transactions: List<Transaction> = listOf()
)
