package com.example.warehouseapp.admin

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.warehouseapp.R
import com.example.warehouseapp.api.ApiService
import com.example.warehouseapp.api.RetrofitClient
import com.example.warehouseapp.auth.LoginActivity
import com.example.warehouseapp.databinding.ActivityAdminBinding
import com.example.warehouseapp.model.Product
import com.example.warehouseapp.util.readBaseUrl
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAdminBinding.inflate(layoutInflater)
        val view = binding.root

        val baseUrl = readBaseUrl(this)
        apiService = RetrofitClient.getRetrofitInstance(baseUrl).create(ApiService::class.java)

        setContentView(view)

        // Load the default fragment
        loadFragment(AdminInventoryFragment())

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            } else {
                checkProductStock()
            }
        } else {
            checkProductStock()
        }


        // Set up bottom navigation listener
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            supportFragmentManager.popBackStack()
            when (item.itemId) {
                R.id.bottom_reports -> {
                    loadFragment(AdminReportsFragment())
                    true
                }

                R.id.bottom_inventory -> {
                    loadFragment(AdminInventoryFragment())
                    true
                }

                R.id.bottom_profile -> {
                    loadFragment(AdminProfileFragment())
                    true
                }

                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .commit()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
                checkProductStock()
            } else {
                // Permission denied
                Toast.makeText(
                    this,
                    "Notification permission denied",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    private fun checkProductStock() {
        apiService.getProducts().enqueue(object : Callback<List<Product>> {
            override fun onResponse(call: Call<List<Product>>, response: Response<List<Product>>) {
                if (response.isSuccessful && response.body() != null) {
                    val products = response.body()!!

                    products.forEach { product ->
                        // Check if the product quantity is less than 2
                        if (product.quantity < 10) {
                            // Log the product or take action
                            Log.w(
                                "LowStockWarning",
                                "Product '${product.name}' has low stock: ${product.quantity}"
                            )

                            // Optionally send a notification
                            sendNotification(product.name, product.quantity)
                        }
                    }
                } else {
                    Toast.makeText(this@AdminActivity, "Failed to load products", Toast.LENGTH_SHORT)
                        .show()
                }
            }


            override fun onFailure(call: Call<List<Product>>, t: Throwable) {
                Toast.makeText(this@AdminActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun sendNotification(name: String, quantity: Int) {
        val notificationManager =
            this@AdminActivity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Notification Channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "low_stock_channel"
            val channelName = "Low Stock Notifications"
            val channelDescription = "Notifies when stock is low for a product"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel =
                NotificationChannel(channelId, channelName, importance).apply {
                    description = channelDescription
                    enableLights(true)
                    lightColor = Color.RED
                    enableVibration(true)
                }
            notificationManager.createNotificationChannel(notificationChannel)
        }

        // Intent to open the app when notification is clicked
        val intent =
            Intent(this@AdminActivity, LoginActivity::class.java) // Replace with the desired activity
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(
            this@AdminActivity,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification
        val notificationBuilder = NotificationCompat.Builder(this@AdminActivity, "low_stock_channel")
            .setSmallIcon(R.drawable.baseline_notifications_24) // Replace with your app's icon
            .setContentTitle("Low Stock Alert")
            .setContentText("Product '$name' has low stock: $quantity left!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Show the notification
        notificationManager.notify(name.hashCode(), notificationBuilder.build())
    }
}
