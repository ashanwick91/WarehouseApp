package com.example.warehouseapp.model

import java.util.Date

data class OrdersResponse(
    val orders: List<OrderDetails>
)
data class OrderDetails(
    val _id: String,
    val createdAt: String,
    val customerId: String,
    val items: List<ItemDetails>,
    val orderDate: String,
    val orderTotal: Double,
    val status: String
)

data class ItemDetails(
    val category: String,
    val price: Double,
    val productId: String,
    val productName: String,
    val quantity: Int,
    val quantitySold: Int,
    val salesAmount: Double,
    val transactionDate: String
)