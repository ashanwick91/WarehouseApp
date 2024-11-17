package com.example.warehouseapp.listener

import com.example.warehouseapp.model.Product

interface AdminProductItemClickListener {
    fun onDeleteClick(productId: String)
    fun onEditClick(product: Product)
}