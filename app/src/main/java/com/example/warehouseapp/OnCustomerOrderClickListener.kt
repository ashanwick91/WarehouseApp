package com.example.warehouseapp

import com.example.warehouseapp.model.OrderDetails
import com.example.warehouseapp.model.OrdersResponse


interface OnCustomerOrderClickListener {
    fun onViewOrderDetails(order: OrderDetails)
}