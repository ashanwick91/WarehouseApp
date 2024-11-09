package com.example.warehouseapp.admin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.warehouseapp.R
import com.example.warehouseapp.auth.LoginActivity
import com.example.warehouseapp.databinding.ActivityAdminBinding

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAdminBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)

        binding.mainTitle.text = "HOME"

        // Load the default fragment
        loadFragment(AdminHomeFragment())

        /*
        binding.adminBtnLogout.setOnClickListener {
            // Clear the JWT token from SharedPreferences
            clearTokenFromPreferences()

            // Redirect to login screen
            val intent = Intent(this@AdminActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }*/

        // Set up bottom navigation listener
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_home -> {
                    binding.mainTitle.text = "HOME"
                    loadFragment(AdminHomeFragment())
                    true
                }
                R.id.bottom_inventory -> {
                    binding.mainTitle.text = "INVENTORY"
                    loadFragment(AdminInventoryFragment())
                    true
                }
                R.id.bottom_profile -> {
                    binding.mainTitle.text = "PROFILE"
                    loadFragment(AdminProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun clearTokenFromPreferences() {
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.remove("jwt_token")
        editor.apply()
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .commit()
    }
}