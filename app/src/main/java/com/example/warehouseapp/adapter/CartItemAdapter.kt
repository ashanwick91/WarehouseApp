package com.example.warehouseapp.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.warehouseapp.R
import com.example.warehouseapp.api.ApiService
import com.example.warehouseapp.databinding.CustomerItemCartBinding
import com.example.warehouseapp.model.OrderItemRequest
import com.example.warehouseapp.model.OrderRequest
import com.example.warehouseapp.model.Product
import com.example.warehouseapp.util.loadImageFromFirebase
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class CartItemAdapter(
    private val context: Context,
    private var cart: OrderRequest,
    private var apiService: ApiService,// Mutable cart to update directly
    private val onQuantityChanged: (OrderRequest
            ) -> Unit // Callback to notify total price updates
) : RecyclerView.Adapter<CartItemAdapter.CartItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartItemViewHolder {
        val binding =
            CustomerItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartItemViewHolder(binding);
    }

    override fun onBindViewHolder(holder: CartItemViewHolder, position: Int) {
        val item = cart.items[position]
        holder.bind(item)

    }

    override fun getItemCount(): Int = cart.items.size

    inner class CartItemViewHolder(private val binding:  CustomerItemCartBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: OrderItemRequest) {
            binding.itemName.text = item.productName
            binding.itemPrice.text = String.format("$%.2f", item.price)
            binding.itemQuantity.text = item.quantity.toString()

            //item.imageUrl?.let { loadImageFromFirebase(it, binding.ivProductAdmin) }
            // Decrease quantity
            binding.btnDecrease.setOnClickListener {
                if (item.quantity > 1) {
                    updateQuantity(item, item.quantity - 1)
                }
            }

            // Increase quantity
            binding.btnIncrease.setOnClickListener {
                updateQuantity(item, item.quantity + 1)
            }

            apiService.getProducts().enqueue(object : Callback<List<Product>> {
                override fun onResponse(call: Call<List<Product>>, response: Response<List<Product>>) {
                    if (response.isSuccessful && response.body() != null) {
                        val products = response.body()!!
                        Log.d("productsproductsproducts", "productproducts: $products")
                        // Filter and process only the product matching the productId
                        val matchingProduct = products.find { it.id == item.productId }
                        if (matchingProduct != null) {
                            Log.e("matchingProductmatchingProduct", matchingProduct.imageUrl.toString())
                        }
                        if (matchingProduct != null) {
                            Glide.with(binding.itemImage.context)
                                .load(matchingProduct.imageUrl?.let { loadImageFromFirebase(it, binding.itemImage) }) // The URL of the image
                                .placeholder(R.drawable.placeholder_image) // Optional placeholder
                                .error(R.drawable.loading) // Optional error image
                                .into(binding.itemImage) // Target ImageView

                        } else {
                            Log.e("CustomerOrderHistrory", "Product not found for ID: ${item.productId}")
                        }
                    } else {
                        Log.e("CustomerOrderHistrory", "Failed to fetch products: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<List<Product>>, t: Throwable) {
                    Log.e("CustomerOrderHistrory", "Error fetching products", t)
                }
            })
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
                binding.itemQuantity.text = newQuantity.toString()
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