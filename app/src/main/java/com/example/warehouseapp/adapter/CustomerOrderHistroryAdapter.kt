package com.example.warehouseapp.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.warehouseapp.R
import com.example.warehouseapp.api.ApiService
import com.example.warehouseapp.model.ItemDetails
import com.example.warehouseapp.model.OrderItem
import com.example.warehouseapp.model.Product
import com.example.warehouseapp.util.MyAppGlideModule
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CustomerOrderHistroryAdapter(
    private var orderList: List<OrderItem>,
    private val apiService: ApiService
    ) : RecyclerView.Adapter<CustomerOrderHistroryAdapter.CustomerOrderHistroryViewHolder>(){

    fun updateOrderList(newOrder: List<OrderItem>) {
        orderList = newOrder
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerOrderHistroryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_customer_order_history_details, parent, false)
        return CustomerOrderHistroryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomerOrderHistroryViewHolder, position: Int) {
        val item = orderList[position]
        var productId = item.productId

        // Pass the product ID of the current item to the API service
        // Fetch the product details for the given productId
        apiService.getProducts().enqueue(object : Callback<List<Product>> {
            override fun onResponse(call: Call<List<Product>>, response: Response<List<Product>>) {
                if (response.isSuccessful && response.body() != null) {
                    val products = response.body()!!
                    Log.d("productsproductsproducts", "productproducts: $products")
                    // Filter and process only the product matching the productId
                    val matchingProduct = products.find { it.id == productId }
                    if (matchingProduct != null) {
                        Log.e("matchingProductmatchingProduct", matchingProduct.imageUrl.toString())
                    }
                    if (matchingProduct != null) {
                        // Load the image URL into the ImageView using Glide

                        Glide.with(holder.itemImage.context)
                            .load(matchingProduct.imageUrl) // The URL of the image
                            .placeholder(R.drawable.placeholder_image) // Optional placeholder image
                            .error(R.drawable.shoes1) // Optional error image
                            .into(holder.itemImage) // The target ImageView


                        // Update other UI fields if needed
                        holder.itemName.text = matchingProduct.name
                    } else {
                        Log.e("CustomerOrderHistrory", "Product not found for ID: $productId")
                    }
                } else {
                    Log.e("CustomerOrderHistrory", "Failed to fetch products: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<Product>>, t: Throwable) {
                Log.e("CustomerOrderHistrory", "Error fetching products", t)
            }
        })

        // Fallback UI values while waiting for the API response

        holder.itemName.text = item.productName
        holder.itemCount.text = item.quantity.toString()
        holder.itemPrice.text = String.format("$%.2f", item.price)
    }
    override fun getItemCount() = orderList.size

    inner class CustomerOrderHistroryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemImage: ImageView = view.findViewById(R.id.imageViewItem)
        val itemName: TextView = view.findViewById(R.id.itemName)
        val itemCount: TextView = view.findViewById(R.id.itemCount)
        val itemPrice: TextView = view.findViewById(R.id.itemPrice)
    }
}