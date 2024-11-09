package com.example.warehouseapp.model

import java.util.Date

data class FinancialReport(
    val id: String,  // MongoDB ObjectId as a string
    val reportType: String,  // Example: "Daily", "Monthly"
    val salesData: List<SalesData>,
    val totalSales: Double,
    val totalProfit: Double,
    val reportGeneratedAt: Date
)

data class SalesData(
    val transactionId: String,  // MongoDB ObjectId as a string
    val transactionTotal: Double,
    val transactionDate: Date
)