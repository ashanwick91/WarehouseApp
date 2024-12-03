package com.example.warehouseapp.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class Product(
    @SerializedName("_id")
    val id: String? = null,  // MongoDB ObjectId as a string
    val name: String,
    val description: String,
    val price: Double,
    val originalPrice: Double,
    val category: String,  // Example: "Electronics", "Furniture"
    val imageUrl: String?,  // URL to the product image
    var quantity: Int,  // Current stock
    val createdAt: Date? = null,
    val updatedAt: Date? = null,
    //val transactions: List<Transaction> = listOf()
    @Transient
    var cartQuantity: Int = 0 // The quantity added to the cart
)