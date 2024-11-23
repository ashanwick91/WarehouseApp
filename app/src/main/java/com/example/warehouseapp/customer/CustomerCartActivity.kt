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
import com.example.warehouseapp.model.Product
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
    private var _binding: ActivityCustomerCartBinding? = null
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
        saveOrderToPreferences(order)

        backImageView.setOnClickListener {

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
                            transactionDate =Date()
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
                orderDate = Date(),
                createdAt = Date()
            )

            saveOrderToPreferences(order)
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
                            transactionDate = Date()
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
                orderDate =  Date(),
                createdAt =  Date()
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
                        transactionDate =  Date(),
                        price = price,
                        quantity = quantity
                    )
                )
            }
            index++
        }

        // Initialize the order object
        order = OrderRequest(
            customerId = customerId,
            items = orderItems,
            orderTotal = orderTotal,
            orderDate =  Date(),
            status = status ?: "Pending",
            createdAt =  Date()
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
        adapter = CartItemAdapter(this ,  order, apiService) { updatedOrder ->
            updateTotalPrice(updatedOrder)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupLeftSwipeToDelete() {
        val deleteIcon: Drawable? = ContextCompat.getDrawable(this, R.drawable.baseline_delete_24)
        val iconMargin = resources.getDimension(R.dimen.icon_margin).toInt()
        val backgroundColor = ContextCompat.getColor(this, R.color.swipe_background_gray)

        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition

                // Remove the item from the cart and SharedPreferences
                val removedItem = order.items[position]
                order.items = order.items.toMutableList().apply { removeAt(position) }

                // Update the adapter
                adapter.notifyItemRemoved(position)
                updateTotalPrice(order)

                // Remove the item from SharedPreferences
                removeItemFromPreferences(position)

                // Show Snackbar with Undo option
                Snackbar.make(recyclerView, "${removedItem.productName} removed from  your cart", Snackbar.LENGTH_LONG).show()
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

                // Draw background
                val backgroundPaint = android.graphics.Paint().apply {
                    color = backgroundColor
                }
                c.drawRect(
                    itemView.right + dX,
                    itemView.top.toFloat(),
                    itemView.right.toFloat(),
                    itemView.bottom.toFloat(),
                    backgroundPaint
                )

                // Draw delete icon
                deleteIcon?.let {
                    val iconTop = itemView.top + (itemHeight - it.intrinsicHeight) / 2
                    val iconLeft = itemView.right - iconMargin - it.intrinsicWidth
                    val iconRight = itemView.right - iconMargin
                    val iconBottom = iconTop + it.intrinsicHeight

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

    private fun saveOrderToPreferences(order: OrderRequest) {
        val sharedPref = this.getSharedPreferences("order_prefs", Context.MODE_PRIVATE)
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

    private fun removeItemFromPreferences(position: Int) {
        val sharedPref = getSharedPreferences("order_prefs", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        // Remove specific item keys
        editor.remove("item_${position}_product_id")
        editor.remove("item_${position}_product_name")
        editor.remove("item_${position}_category")
        editor.remove("item_${position}_sales_amount")
        editor.remove("item_${position}_quantity")
        editor.remove("item_${position}_price")
        editor.remove("item_${position}_transaction_date")

        // Adjust subsequent items' keys if needed
        for (i in position + 1 until order.items.size + 1) {
            val productId = sharedPref.getString("item_${i}_product_id", null)
            val productName = sharedPref.getString("item_${i}_product_name", null)
            val category = sharedPref.getString("item_${i}_category", null)
            val salesAmount = sharedPref.getFloat("item_${i}_sales_amount", 0.0f)
            val quantity = sharedPref.getInt("item_${i}_quantity", 0)
            val price = sharedPref.getFloat("item_${i}_price", 0.0f)
            val transactionDate = sharedPref.getString("item_${i}_transaction_date", null)

            if (productId != null && productName != null && category != null) {
                editor.putString("item_${i - 1}_product_id", productId)
                editor.putString("item_${i - 1}_product_name", productName)
                editor.putString("item_${i - 1}_category", category)
                editor.putFloat("item_${i - 1}_sales_amount", salesAmount)
                editor.putInt("item_${i - 1}_quantity", quantity)
                editor.putFloat("item_${i - 1}_price", price)
                editor.putString("item_${i - 1}_transaction_date", transactionDate)
            }
        }

        editor.apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clear the binding reference if you want to explicitly clean up resources
        _binding = null
    }
}