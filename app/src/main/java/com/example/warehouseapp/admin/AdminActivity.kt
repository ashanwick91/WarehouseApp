package com.example.warehouseapp.admin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.warehouseapp.auth.LoginActivity
import com.example.warehouseapp.databinding.ActivityAdminBinding

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAdminBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)

        binding.adminBtnLogout.setOnClickListener {
            // Clear the JWT token from SharedPreferences
            clearTokenFromPreferences()

            // Redirect to login screen
            val intent = Intent(this@AdminActivity, LoginActivity::class.java)
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