package com.example.warehouseapp.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class Product(
    @SerializedName("_id")
    val id: String,  // MongoDB ObjectId as a string
    val name: String,
    val description: String,
    val price: Double,
    val category: String,  // Example: "Electronics", "Furniture"
    val image: String,  // URL to the product image
    val quantity: Int,  // Current stock
    val createdAt: Date,
    val updatedAt: Date,
    //val transactions: List<Transaction> = listOf()
    var cartQuantity: Int = 0 // The quantity added to the cart
)
