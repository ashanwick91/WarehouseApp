package com.example.warehouseapp.customer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.warehouseapp.auth.LoginActivity
import com.example.warehouseapp.databinding.ActivityCustomerBinding

class CustomerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustomerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCustomerBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)

        binding.customerBtnLogout.setOnClickListener {
            // Clear the JWT token from SharedPreferences
            clearTokenFromPreferences()

            // Redirect to login screen
            val intent = Intent(this@CustomerActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun clearTokenFromPreferences() {
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.remove("jwt_token")
        editor.apply()
    }
}