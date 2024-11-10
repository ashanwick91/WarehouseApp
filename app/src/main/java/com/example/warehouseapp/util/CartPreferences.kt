package com.example.warehouseapp.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.warehouseapp.model.Item

object CartPreferences {

    private const val PREFS_NAME = "cart_prefs"
    private const val CART_KEY = "cart_items"

    // Add an item to the cart
    fun addToCart(context: Context, item: Item) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()

        // Get current cart items
        val cartItems = getCartItems(context).toMutableList()

        // Check if item already exists in the cart and update quantity
        val existingItem = cartItems.find { it.productId == item.productId }
        if (existingItem != null) {
            existingItem.quantity += item.quantity
        } else {
            cartItems.add(item)
        }

        // Save updated cart items
        val jsonCartItems = gson.toJson(cartItems)
        editor.putString(CART_KEY, jsonCartItems)
        editor.apply()
    }

    // Remove an item from the cart
    fun removeFromCart(context: Context, productId: String) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()

        // Get current cart items
        val cartItems = getCartItems(context).toMutableList()

        Log.d("CartPreferences", "Cart items before operation: $cartItems")
        Log.d("CartPreferences", "Decreasing quantity for productId: $productId")

        // Find the item in the cart and decrease the quantity
        val itemToModify = cartItems.find { it.productId == productId }
        if (itemToModify != null) {
            if (itemToModify.quantity > 0) {
                itemToModify.quantity -= 1
                Log.d("CartPreferences", "Decreased quantity for productId: $productId, new quantity: ${itemToModify.quantity}")
            }

            // Ensure the quantity does not go below zero
            if (itemToModify.quantity < 0) {
                itemToModify.quantity = 0
            }
        }

        Log.d("CartPreferences", "Cart items after operation: $cartItems")

        // Save updated cart items
        val jsonCartItems = gson.toJson(cartItems)
        editor.putString(CART_KEY, jsonCartItems)
        editor.apply()
    }

    // Get all items currently in the cart
    fun getCartItems(context: Context): List<Item> {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val jsonCartItems = sharedPreferences.getString(CART_KEY, null)

        return if (jsonCartItems != null) {
            val type = object : TypeToken<List<Item>>() {}.type
            gson.fromJson(jsonCartItems, type)
        } else {
            emptyList()
        }
    }

    // Get a summary of the cart for displaying in the UI
    fun getCartSummary(context: Context): String {
        val cartItems = getCartItems(context)
        val totalItems = cartItems.sumOf { it.quantity }
        val totalPrice = cartItems.sumOf { it.price * it.quantity }
        val productNames = cartItems.joinToString(", ") { it.productName }

        return "Total items: $totalItems, Total price: $${"%.2f".format(totalPrice)}, Products: $productNames"
    }

    // Clear all items in the cart
    fun clearCart(context: Context) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove(CART_KEY)
        editor.apply()
    }
}
