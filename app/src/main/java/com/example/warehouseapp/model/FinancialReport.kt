package com.example.warehouseapp.model

import java.util.Date

data class FinancialReport(
    val reportType: String,
    val salesDataCategory: List<SalesDataCategory>?,
    val salesDataProduct: List<SalesDataProduct>?,
    val reportGeneratedAt: String
)

data class SalesDataCategory(
    val category: String,
    val totalSalesCategory: Double,
    val totalProfitCategory: Double
)

data class SalesDataProduct(
    val product: String,
    val totalSalesProduct: Double,
    val totalProfitProduct: Double
)