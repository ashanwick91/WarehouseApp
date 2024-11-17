package com.example.warehouseapp.customer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.auth0.android.jwt.JWT
import com.example.warehouseapp.OnProductItemClickListener
import com.example.warehouseapp.R
import com.example.warehouseapp.adapter.ProductAdapter
import com.example.warehouseapp.api.ApiService
import com.example.warehouseapp.api.RetrofitClient
import com.example.warehouseapp.databinding.FragmentCustomerHomeBinding
import com.example.warehouseapp.model.OrderRequest
import com.example.warehouseapp.model.OrderItemRequest
import com.example.warehouseapp.model.Product
import com.example.warehouseapp.util.readBaseUrl
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.util.Locale


class CustomerHomeFragment : Fragment(), OnProductItemClickListener {

    private lateinit var binding: FragmentCustomerHomeBinding
    private lateinit var apiService: ApiService
    private lateinit var productRecyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private var customerId: String = ""
    private lateinit var order: OrderRequest

    private val cartItems = mutableListOf<OrderItemRequest>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // If necessary, initialize variables or load data that requires a context here
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCustomerHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        val baseUrl = readBaseUrl(requireContext())
        apiService = RetrofitClient.getRetrofitInstance(baseUrl).create(ApiService::class.java)

        productRecyclerView = binding.recyclerViewProducts
        productRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        productAdapter = ProductAdapter(emptyList(), this)
        productRecyclerView.adapter = productAdapter
        Log.d("testing view 1", "customerId")
        val cartIcon = view.findViewById<ImageView>(R.id.cart_icon)
        fetchAllProducts()
        loadOrderFromPreferences()
        productAdapter.notifyDataSetChanged()

        // On clicking the cart icon, create an Order and add OrderItems to it
        cartIcon.setOnClickListener {
            if (cartItems.isEmpty()) {
                Toast.makeText(requireContext(), "No items in cart", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val orderTotal = cartItems.sumOf { it.price * it.quantity }
            val order = OrderRequest(
                customerId = "",
                items = cartItems.toList(),
                orderTotal = orderTotal,
                orderDate = OffsetDateTime.now(),
                status = "Pending",
                createdAt = OffsetDateTime.now()
            )

            saveOrderToPreferences(order)

            val intent = Intent(requireContext(), CustomerCartActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    private fun fetchAllProducts() {
        apiService.getProducts().enqueue(object : Callback<List<Product>> {
            override fun onResponse(call: Call<List<Product>>, response: Response<List<Product>>) {
                if (response.isSuccessful && response.body() != null) {
                    val products = response.body()!!
                    val order = loadOrderFromPreferences()

                    products.forEach { product ->
                        val matchingCartItem = order?.items?.find { it.productId == product.id }
                        if (matchingCartItem != null) {
                            product.cartQuantity = matchingCartItem.quantity
                        }
                    }

                    productAdapter.updateProductList(products)
                } else {
                    Toast.makeText(requireContext(), "Failed to load products", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(call: Call<List<Product>>, t: Throwable) {
                Log.e("CustomerHomeFragment", "Error fetching products", t)
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onAddToCartClick(product: Product, quantity: Int) {
        val existingItem = cartItems.find { it.productId == product.id }
        if (existingItem != null) {
            existingItem.quantity += quantity
            existingItem.salesAmount = existingItem.price * existingItem.quantity
        } else {
            // Add a new item if it doesn't exist in the cart
            val orderItem = OrderItemRequest(
                productId = product.id!!,
                productName = product.name,
                category = product.category,
                salesAmount = product.price * quantity,
                profitAmount = 0.0,
                quantitySold = quantity,
                transactionDate = OffsetDateTime.now(),
                price = product.price,
                quantity = quantity
            )
            cartItems.add(orderItem)
            updateOrderTotal(orderItem.salesAmount)
        }

        productAdapter.notifyDataSetChanged()

        Toast.makeText(
            requireContext(),
            "${product.name} quantity updated to ${existingItem?.quantity ?: quantity}",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun updateOrderTotal(amountChange: Double) {
        val sharedPref = requireContext().getSharedPreferences("order_prefs", Context.MODE_PRIVATE)
        val currentTotal = sharedPref.getFloat("order_total", 0.0f).toDouble()
        val newTotal = currentTotal + amountChange
        val editor = sharedPref.edit()
        editor.putFloat("order_total", newTotal.toFloat())
        editor.apply()


    }

    private fun saveOrderToPreferences(order: OrderRequest) {
        val sharedPref = requireContext().getSharedPreferences("order_prefs", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("customer_id", customerId)
        editor.putFloat("order_total", order.orderTotal.toFloat())
        editor.putString("status", order.status)
        val createdDate = OffsetDateTime.now()
        editor.putString("created_at", createdDate.toString())
        val transactionDate = OffsetDateTime.now()
        order.items.forEachIndexed { index, item ->
            editor.putString("item_${index}_product_id", item.productId)
            editor.putString("item_${index}_product_name", item.productName)
            editor.putString("item_${index}_category", item.category)
            editor.putFloat("item_${index}_sales_amount", item.salesAmount.toFloat())
            editor.putInt("item_${index}_quantity_sold", item.quantitySold)
            editor.putString(
                "item_${index}_transaction_date",
                transactionDate.toString()
            ) // Format transactionDate
            editor.putFloat("item_${index}_price", item.price.toFloat())
            editor.putInt("item_${index}_quantity", item.quantity)
        }
        editor.apply()
    }

    private fun loadOrderFromPreferences(): OrderRequest? {
        val sharedPref = requireContext().getSharedPreferences("order_prefs", Context.MODE_PRIVATE)
        val orderItems = mutableListOf<OrderItemRequest>()
        var index = 0

        while (sharedPref.contains("item_${index}_product_id")) {
            val productId = sharedPref.getString("item_${index}_product_id", null)
            val productName = sharedPref.getString("item_${index}_product_name", null)
            val category = sharedPref.getString("item_${index}_category", null)
            val salesAmount = sharedPref.getFloat("item_${index}_sales_amount", 0.0f).toDouble()
            val quantitySold = sharedPref.getInt("item_${index}_quantity_sold", 0)
            val price = sharedPref.getFloat("item_${index}_price", 0.0f).toDouble()
            val quantity = sharedPref.getInt("item_${index}_quantity", 0)
            val transactionDate = sharedPref.getString("item_${index}_transaction_date", null)

            if (productId != null && productName != null && category != null) {
                orderItems.add(
                    OrderItemRequest(
                        productId = productId,
                        productName = productName,
                        category = category,
                        salesAmount = salesAmount,
                        profitAmount = 0.0, // Adjust if necessary
                        quantitySold = quantitySold,
                        price = price,
                        quantity = quantity,
                        transactionDate = transactionDate ?: OffsetDateTime.now().toString()
                    )
                )
            }
            index++
        }

        // Initialize the order object
        val orderTotal = orderItems.sumOf { it.salesAmount }
        order = OrderRequest(
            customerId = customerId,
            items = orderItems,
            orderTotal = orderTotal,
            orderDate = OffsetDateTime.now().toString(),
            status = "Pending",
            createdAt = OffsetDateTime.now().toString()
        )

        // Sync with the cart items
        cartItems.clear()
        cartItems.addAll(orderItems)

        productAdapter.notifyDataSetChanged()
        return order
    }
    override fun onRemoveFromCartClick(productId: String) {
        val existingItem = cartItems.find { it.productId == productId }
        if (existingItem != null) {
            updateOrderTotal(-existingItem.salesAmount)
            cartItems.remove(existingItem)
        }
        Toast.makeText(requireContext(), "Item removed from cart", Toast.LENGTH_SHORT).show()
    }


}