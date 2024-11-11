package com.example.warehouseapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Item(
    val productId: String,
    val productName: String,
    val price: Double,
    var quantity: Int
) : Parcelable
