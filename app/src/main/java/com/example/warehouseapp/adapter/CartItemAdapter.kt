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
import com.example.warehouseapp.model.Item
import com.example.warehouseapp.model.ItemDetails
import com.example.warehouseapp.model.OrderItemRequest
import com.example.warehouseapp.model.OrderRequest
import com.example.warehouseapp.model.Product
import com.example.warehouseapp.util.CartPreferences
import com.example.warehouseapp.util.loadImageFromFirebase
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class CartItemAdapter(
    private var cartItems: List<ItemDetails>,
    private val context: Context,
    private var apiService: ApiService,
    private val onCartUpdated: () -> Unit
) : RecyclerView.Adapter<CartItemAdapter.CartItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartItemViewHolder {
        val binding = CustomerItemCartBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CartItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartItemViewHolder, position: Int) {
        val item = cartItems[position]
        holder.binding.itemQuantity.text = item.quantity.toString()
        holder.binding.itemName.text = item.productName
        holder.binding.itemPrice.text = "$${item.price}"

        holder.binding.btnIncrease.setOnClickListener {
            item.quantity++
            updateCart(item)
            notifyItemChanged(position)
            onCartUpdated() // Notify the activity
        }

        holder.binding.btnDecrease.setOnClickListener {
            if (item.quantity > 0) {
                item.quantity--
                updateCart(item)
                if (item.quantity == 0) {
                    // Convert to mutable list, remove the item, and save
                    val mutableCartItems = cartItems.toMutableList()
                    mutableCartItems.removeAt(position)
                    cartItems = mutableCartItems // Update the cartItems list
                    CartPreferences.saveCart(context, cartItems) // Save updated cart
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, cartItems.size)
                } else {
                    notifyItemChanged(position)
                }
                onCartUpdated() // Notify the activity
            }
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
                        Glide.with(holder.binding.itemImage.context)
                            .load(matchingProduct.imageUrl?.let { loadImageFromFirebase(it, holder.binding.itemImage) }) // The URL of the image
                            .placeholder(R.drawable.placeholder_image) // Optional placeholder
                            .error(R.drawable.loading) // Optional error image
                            .into(holder.binding.itemImage) // Target ImageView

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

    override fun getItemCount(): Int = cartItems.size

    fun updateCartItems(newCartItems: List<ItemDetails>) {
        this.cartItems = newCartItems
        notifyDataSetChanged()
    }

    private fun updateCart(orderItem: ItemDetails) {
        val cartItems = CartPreferences.getCart(context).toMutableList()
        val index = cartItems.indexOfFirst { it.productId == orderItem.productId }

        if (index != -1) {
            if (orderItem.quantity > 0) {
                cartItems[index].quantity = orderItem.quantity
            } else {
                cartItems.removeAt(index)
            }
        } else {
            if (orderItem.quantity > 0) {
                cartItems.add(orderItem)
            }
        }

        CartPreferences.saveCart(context, cartItems)
    }

    inner class CartItemViewHolder(val binding: CustomerItemCartBinding) :
        RecyclerView.ViewHolder(binding.root)
}
