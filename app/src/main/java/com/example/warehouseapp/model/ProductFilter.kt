package com.example.warehouseapp.model

data class ProductFilter(
    val category: String? = null,  // Optional category filter
    val minPrice: Double? = null,  // Optional minimum price
    val maxPrice: Double? = null,  // Optional maximum price
    val isAvailable: Boolean = true  // Defaults to true to filter by availability
)
