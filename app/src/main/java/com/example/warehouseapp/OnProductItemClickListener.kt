package com.example.warehouseapp

import com.example.warehouseapp.model.Product

interface OnProductItemClickListener {
    fun onAddToCartClick(product: Product,quantity: Int)
    fun onRemoveFromCartClick(productId: String)
    abstract fun onShowMessage(message: String)
}