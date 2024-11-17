package com.example.warehouseapp.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.warehouseapp.R
import com.example.warehouseapp.model.Item
import com.example.warehouseapp.model.OrderItem
import com.example.warehouseapp.model.OrderItemRequest
import com.example.warehouseapp.model.OrderRequest
import com.google.android.material.button.MaterialButton


class CartItemAdapter(
    private val context: Context,
    private var cart: OrderRequest, // Mutable cart to update directly
    private val onQuantityChanged: (OrderRequest
            ) -> Unit // Callback to notify total price updates
) : RecyclerView.Adapter<CartItemAdapter.CartItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartItemViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.customer_item_cart, parent, false)
        return CartItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartItemViewHolder, position: Int) {
        val item = cart.items[position]
        holder.bind(item)

    }

    override fun getItemCount(): Int = cart.items.size

    inner class CartItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val itemName: TextView = itemView.findViewById(R.id.item_name)
        private val itemPrice: TextView = itemView.findViewById(R.id.item_price)
        private val itemQuantity: TextView = itemView.findViewById(R.id.item_quantity)
        private val btnDecrease: MaterialButton = itemView.findViewById(R.id.btn_decrease)
        private val btnIncrease: MaterialButton = itemView.findViewById(R.id.btn_increase)

        fun bind(item: OrderItemRequest) {
            itemName.text = item.productName
            itemPrice.text = "$${item.price}"
            itemQuantity.text = item.quantity.toString()

            btnDecrease.setOnClickListener {
                if (item.quantity > 1) {
                    val newQuantity = item.quantity - 1
                    updateQuantity(item, newQuantity)
                }
            }

            btnIncrease.setOnClickListener {
                val newQuantity = item.quantity + 1
                updateQuantity(item, newQuantity)
            }
        }

        private fun updateQuantity(item: OrderItemRequest, newQuantity: Int) {
            val itemIndex = cart.items.indexOfFirst { it.productId == item.productId }
            if (itemIndex != -1) {
                val updatedItem = item.copy(quantity = newQuantity)
                val updatedItems = cart.items.toMutableList()
                updatedItems[itemIndex] = updatedItem
                // Save the updated cart item to SharedPreferences
                saveItemToPreferences(updatedItem, itemIndex)
                cart = cart.copy(items = updatedItems)
                onQuantityChanged(cart)
                itemQuantity.text = newQuantity.toString()
                notifyItemChanged(adapterPosition)
            }
        }
        private fun saveItemToPreferences(item: OrderItemRequest, index: Int) {
            val sharedPref = context.getSharedPreferences("order_prefs", Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putString("item_${index}_product_id", item.productId)
            editor.putString("item_${index}_product_name", item.productName)
            editor.putString("item_${index}_category", item.category)
            editor.putFloat("item_${index}_sales_amount", item.salesAmount.toFloat())
            editor.putInt("item_${index}_quantity", item.quantity)
            editor.putFloat("item_${index}_price", item.price.toFloat())
            editor.putString("item_${index}_transaction_date", item.transactionDate.toString())
            editor.apply()
        }

    }
}