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
import com.example.warehouseapp.util.readBaseUrl
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
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
        val view = binding.root
        setContentView(view)
        cartTotalTextView = binding.subtotalAmount
        totalTaxTextView = binding.taxAmount
        totalAmountTextView = binding.totalAmount
        backImageView = binding.backButton
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

        loadOrderFromPreferences()
        setupRecyclerView(order)
        updateTotalPrice(order)
        setupRecyclerView(order)
        updateTotalPrice(order)
        setupLeftSwipeToDelete()

        backImageView.setOnClickListener {
            val intent = Intent(this, CustomerActivity::class.java)
            startActivity(intent)
        }

        checkoutButton.setOnClickListener {
            val orderItems = mutableListOf<OrderItemRequest>()
            val sharedPref = getSharedPreferences("order_prefs", Context.MODE_PRIVATE)
            var index = 0
            while (sharedPref.contains("item_${index}_product_id")) {
                val productId = sharedPref.getString("item_${index}_product_id", null)
                val productName = sharedPref.getString("item_${index}_product_name", null)
                val category = sharedPref.getString("item_${index}_category", null)
                val salesAmount = sharedPref.getFloat("item_${index}_sales_amount", 0.0f).toDouble()
                val quantitySold = sharedPref.getInt("item_${index}_quantity_sold", 0)
                val price = sharedPref.getFloat("item_${index}_price", 0.0f).toDouble()
                val quantity = sharedPref.getInt("item_${index}_quantity", 0)

                if (productId != null && productName != null && category != null) {
                    orderItems.add(
                        OrderItemRequest(
                            productId = productId,
                            productName = productName,
                            category = category,
                            salesAmount = salesAmount,
                            profitAmount = 0.0,
                            quantitySold = quantitySold,
                            price = price,
                            quantity = quantity,
                            transactionDate = OffsetDateTime.now().toString()
                        )
                    )
                }
                index++
            }

            val order = OrderRequest(
                customerId = customerId,
                items = orderItems,
                orderTotal = orderTotal,
                status = "Pending",
                orderDate = OffsetDateTime.now().toString(),
                createdAt = OffsetDateTime.now().toString()
            )

            // Send OrderRequest via API
            apiService.placeOrder(order).enqueue(object : Callback<OrderRequest> {
                override fun onResponse(call: Call<OrderRequest>, response: Response<OrderRequest>) {
                    if (response.isSuccessful) {
                        response.body()?.let { onSuccess(it) }
                    } else {
                        onError("Error: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<OrderRequest>, t: Throwable) {
                    onError(t.message ?: "Unknown error")
                }
            })

            val intent = Intent(this, CustomerPurchaseSucessActivity::class.java)
            startActivity(intent)
        }
    }


    private fun loadOrderFromPreferences() {
        val sharedPref = getSharedPreferences("order_prefs", Context.MODE_PRIVATE)
        val status = sharedPref.getString("status", "Pending")
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

            if (productId != null && productName != null && category != null) {
                orderItems.add(
                    OrderItemRequest(
                        productId = productId,
                        productName = productName,
                        category = category,
                        salesAmount = salesAmount,
                        profitAmount = 0.0,
                        quantitySold = quantitySold,
                        transactionDate = OffsetDateTime.now(),
                        price = price,
                        quantity = quantity
                    )
                )
            }
            index++
        }

        // Initialize the order object
        order = OrderRequest(
            customerId = "67297169bf8d65d8a9983a8c",
            items = orderItems,
            orderTotal = orderTotal,
            orderDate = OffsetDateTime.now(),
            status = status ?: "Pending",
            createdAt = OffsetDateTime.now()
        )
    }

    private fun onSuccess(orderResponse: OrderRequest) {
        Snackbar.make(recyclerView, "Order placed successfully!", Snackbar.LENGTH_LONG).show()
    }

    private fun onError(message: String) {
        Snackbar.make(recyclerView, message, Snackbar.LENGTH_LONG).show()
    }

    private fun setupRecyclerView(order: OrderRequest) {
        recyclerView = findViewById(R.id.cart_items_recycler_view)
        adapter = CartItemAdapter(this, order) { updatedOrder ->
            updateTotalPrice(updatedOrder)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupLeftSwipeToDelete() {
        val deleteIcon: Drawable? = ContextCompat.getDrawable(this, R.drawable.baseline_delete_24)
        val iconMargin = resources.getDimension(R.dimen.icon_margin).toInt()
        val backgroundColor = ContextCompat.getColor(this, R.color.swipe_background_gray) // Define a red color in colors.xml

        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val updatedItems = order.items.toMutableList()
                updatedItems.removeAt(position)

                val updatedOrder = order.copy(items = updatedItems)
                adapter.notifyItemRemoved(position)
                updateTotalPrice(updatedOrder)
                Snackbar.make(recyclerView, "${order.items[position].productName} removed", Snackbar.LENGTH_LONG).setAction("Undo") {
                    updatedItems.add(position, order.items[position])
                    adapter.notifyItemInserted(position)
                    updateTotalPrice(updatedOrder)
                }.show()
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
                val itemView = viewHolder.itemView
                val itemHeight = itemView.bottom - itemView.top
                val cornerRadius = 40f // Adjust this for the desired roundness

                // Draw red background with rounded corners
                val backgroundPath = android.graphics.Path().apply {
                    addRoundRect(
                        itemView.right + dX, // Left side of the background rectangle
                        itemView.top.toFloat(), // Top side
                        itemView.right.toFloat(), // Right side
                        itemView.bottom.toFloat(), // Bottom side
                        floatArrayOf(
                            cornerRadius, cornerRadius, // Top left radius
                            cornerRadius, cornerRadius, // Top right radius
                            cornerRadius, cornerRadius, // Bottom right radius
                            cornerRadius, cornerRadius  // Bottom left radius
                        ),
                        android.graphics.Path.Direction.CW
                    )
                }
                val backgroundPaint = android.graphics.Paint().apply {
                    color = backgroundColor // Set to the background color defined earlier
                    isAntiAlias = true // Smooth the edges
                }
                c.drawPath(backgroundPath, backgroundPaint)

                // Calculate position of delete icon
                deleteIcon?.let {
                    val iconTop = itemView.top + (itemHeight - it.intrinsicHeight) / 2
                    val iconLeft = itemView.right - iconMargin - it.intrinsicWidth
                    val iconRight = itemView.right - iconMargin
                    val iconBottom = iconTop + it.intrinsicHeight

                    // Draw delete icon
                    it.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    it.draw(c)
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun updateTotalPrice(order: OrderRequest) {
        val total = order.items.sumOf { it.price * it.quantity }.let { value ->
            "%.2f".format(value).toDouble()
        }
        val tax = (total * taxRate).let { value ->
            "%.2f".format(value).toDouble()
        }
        orderTotal = (total + tax).let { value ->
            "%.2f".format(value).toDouble()
        }
        cartTotalTextView.text = "$${"%.2f".format(total)}"
        totalTaxTextView.text = "$${"%.2f".format(tax)}"
        totalAmountTextView.text = "$${"%.2f".format(orderTotal)}"
    }

}