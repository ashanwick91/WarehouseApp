package com.example.warehouseapp.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.warehouseapp.model.Item
import com.example.warehouseapp.model.ItemDetails
import com.example.warehouseapp.model.OrderItemRequest

object CartPreferences {
    private const val CART_PREF = "cart_pref"
    private const val CART_ITEMS_KEY = "cart_items"

    fun getCart(context: Context): List<ItemDetails> {
        val sharedPreferences = context.getSharedPreferences(CART_PREF, Context.MODE_PRIVATE)
        val json = sharedPreferences.getString(CART_ITEMS_KEY, null)
        return if (json != null) {
            val type = object : com.google.gson.reflect.TypeToken<List<ItemDetails>>() {}.type
            com.google.gson.Gson().fromJson(json, type)
        } else {
            emptyList()
        }
    }

    fun saveCart(context: Context, cartItems: List<ItemDetails>) {
        val sharedPreferences = context.getSharedPreferences(CART_PREF, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val json = com.google.gson.Gson().toJson(cartItems)
        editor.putString(CART_ITEMS_KEY, json)
        editor.apply()
    }
}