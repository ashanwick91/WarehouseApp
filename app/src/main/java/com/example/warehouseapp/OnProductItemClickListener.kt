package com.example.warehouseapp

import com.example.warehouseapp.model.Product

interface OnProductItemClickListener {
    abstract fun onShowMessage(message: String)
}