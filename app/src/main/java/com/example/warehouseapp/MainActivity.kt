package com.example.warehouseapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.auth0.android.jwt.JWT
import com.example.warehouseapp.admin.AdminActivity
import com.example.warehouseapp.auth.LoginActivity
import com.example.warehouseapp.customer.CustomerActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if the JWT token is saved in SharedPreferences
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("jwt_token", null)

        if (token != null) {
            // Decode the token to get the role
            val jwt = JWT(token)
            val userRole = jwt.getClaim("role").asString()

            // Redirect based on user role
            when (userRole) {
                "admin" -> {
                    val intent = Intent(this@MainActivity, AdminActivity::class.java)
                    startActivity(intent)
                    finish()
                }

                "customer" -> {
                    val intent = Intent(this@MainActivity, CustomerActivity::class.java)
                    startActivity(intent)
                    finish()
                }

                else -> {
                    // Unknown role, redirect to login
                    redirectToLogin()
                }
            }
        } else {
            // No token, redirect to login
            redirectToLogin()
        }
    }

    private fun redirectToLogin() {
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}