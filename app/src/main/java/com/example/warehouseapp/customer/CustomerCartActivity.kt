package com.example.warehouseapp.customer

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.auth0.android.jwt.JWT
import com.example.warehouseapp.R
import com.example.warehouseapp.adapter.CartItemAdapter
import com.example.warehouseapp.api.ApiService
import com.example.warehouseapp.api.RetrofitClient
import com.example.warehouseapp.databinding.ActivityCustomerCartBinding
import com.example.warehouseapp.model.OrderRequest
import com.example.warehouseapp.model.OrderItemRequest
import com.example.warehouseapp.model.Product
import com.example.warehouseapp.util.CartPreferences
import com.example.warehouseapp.util.readBaseUrl
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.util.Date
import java.util.Locale


class CustomerCartActivity : AppCompatActivity() {
    private lateinit var cartTotalTextView: TextView
    private lateinit var totalTaxTextView: TextView
    private lateinit var totalAmountTextView: TextView
    private lateinit var recyclerView: RecyclerView
    private val taxRate = 0.05 // 5% tax rate
    private var orderTotal = 0.0
    private lateinit var backImageView: ImageView
    private lateinit var adapter: CartItemAdapter
    private lateinit var checkoutButton: Button
    private lateinit var apiService: ApiService
    private lateinit var order: OrderRequest
    private var customerId: String = ""

    private lateinit var binding: ActivityCustomerCartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerCartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        recyclerView = findViewById(R.id.cart_items_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        checkoutButton = binding.checkoutButton
        // Setup Retrofit API
        val baseUrl = readBaseUrl(this)
        apiService = RetrofitClient.getRetrofitInstance(baseUrl).create(ApiService::class.java)
        // Decode token to get customer ID
        val sharedPref2 = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val token = sharedPref2.getString("jwt_token", null)

        if (token != null) {
            val jwt = JWT(token)
            customerId = jwt.getClaim("_id").asString() ?: ""
        }


        adapter = CartItemAdapter(mutableListOf(), this, apiService){
            updateCartTotal() // Update cart total whenever cart changes
        }
        binding.cartItemsRecyclerView.adapter = adapter
        backImageView = binding.backButton
        backImageView.setOnClickListener {
            val intent = Intent(this, CustomerActivity::class.java)
            startActivity(intent)
        }

        checkoutButton.setOnClickListener {
            // Step 1: Retrieve cart items from shared preferences
            val cartItems =
                CartPreferences.getCart(this) // Retrieve items as a list of `ItemDetails`
            val orderItems = cartItems.map { cartItem ->
                OrderItemRequest(
                    productId = cartItem.productId,
                    productName = cartItem.productName,
                    category = cartItem.category,
                    salesAmount = cartItem.price * cartItem.quantity,
                    profitAmount = 0.0, // Adjust as necessary
                    quantitySold = cartItem.quantity, // Assuming this is the sold quantity
                    price = cartItem.price,
                    quantity = cartItem.quantity,
                    transactionDate = Date() // Add current date as the transaction date
                )
            }

            // Step 2: Create the OrderRequest object
            val order = OrderRequest(
                customerId = customerId, // Populate this with the logged-in customer's ID
                items = orderItems,
                orderTotal = orderItems.sumOf { it.salesAmount }
                    .toBigDecimal()
                    .setScale(2, java.math.RoundingMode.HALF_UP)
                    .toDouble(), // Calculate total sales amount
                status = "Pending", // Default status
                orderDate = Date(), // Add the current date
                createdAt = Date() // Add creation timestamp
            )

            // Step 3: Save the order to the database using the API service
            apiService.placeOrder(order).enqueue(object : Callback<OrderRequest> {
                override fun onResponse(
                    call: Call<OrderRequest>,
                    response: Response<OrderRequest>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            onSuccess(it)
                        }
                        Toast.makeText(
                            this@CustomerCartActivity,
                            "Order placed successfully!",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Navigate to success activity
                        val intent = Intent(
                            this@CustomerCartActivity,
                            CustomerPurchaseSucessActivity::class.java
                        )
                        startActivity(intent)

                        // Optionally clear the cart after checkout
                        CartPreferences.saveCart(this@CustomerCartActivity, emptyList())
                    } else {
                        onError("Error: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<OrderRequest>, t: Throwable) {
                    onError(t.message ?: "Unknown error")
                }
            })
        }


        leftSwipeDelete()
        loadCart()
    }

    private fun leftSwipeDelete() {
        // Add ItemTouchHelper for swipe-to-delete
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false // No movement support
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                deleteCartItem(position) // Remove the item from the cart
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                // Add a background color and delete icon while swiping
                val itemView = viewHolder.itemView
                val background = ContextCompat.getDrawable(this@CustomerCartActivity, R.drawable.swipe_background)
                val deleteIcon = ContextCompat.getDrawable(this@CustomerCartActivity, R.drawable.baseline_delete_24)

                background?.setBounds(
                    itemView.right + dX.toInt(),
                    itemView.top,
                    itemView.right,
                    itemView.bottom
                )
                background?.draw(c)

                val iconMargin = (itemView.height - (deleteIcon?.intrinsicHeight ?: 0)) / 2
                deleteIcon?.setBounds(
                    itemView.right - iconMargin - (deleteIcon.intrinsicWidth ?: 0),
                    itemView.top + iconMargin,
                    itemView.right - iconMargin,
                    itemView.bottom - iconMargin
                )
                deleteIcon?.draw(c)

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.cartItemsRecyclerView)
    }

    private fun loadCart() {
        val cartItems = CartPreferences.getCart(this)
        adapter.updateCartItems(cartItems)
        updateCartTotal()
    }

// Success callback
private fun onSuccess(order: OrderRequest) {
    Log.d("Checkout", "Order placed successfully: $order")
}

// Error callback
private fun onError(message: String) {
    Log.e("Checkout", "Order placement failed: $message")
    Toast.makeText(this, "Failed to place order: $message", Toast.LENGTH_SHORT).show()
}
    private fun updateCartTotal() {
        val cartItems = CartPreferences.getCart(this)
        val subtotal = cartItems.sumOf { it.quantity * it.price }
        val tax = subtotal * 0.05
        val total = subtotal + tax

        binding.subtotalAmount.text = "$%.2f".format(subtotal)
        binding.taxAmount.text = "$%.2f".format(tax)
        binding.totalAmount.text = "$%.2f".format(total)
    }

    private fun deleteCartItem(position: Int) {
        val mutableCartItems = CartPreferences.getCart(this).toMutableList()
        val removedItem = mutableCartItems.removeAt(position)

        // Save the updated cart back to preferences
        CartPreferences.saveCart(this, mutableCartItems)

        // Update the adapter and total
        adapter.updateCartItems(mutableCartItems)
        updateCartTotal()

        // Show Snackbar with Undo option
        Snackbar.make(binding.root, "${removedItem.productName} removed from cart", Snackbar.LENGTH_LONG)
            .setAction("UNDO") {
                // Re-add the item when Undo is clicked
                mutableCartItems.add(position, removedItem)
                CartPreferences.saveCart(this, mutableCartItems)
                adapter.updateCartItems(mutableCartItems)
                updateCartTotal()
            }
            .show()

        // Disable checkout if the cart is empty
        if (mutableCartItems.isEmpty()) {
            checkoutButton.isEnabled = false
        }
    }
}