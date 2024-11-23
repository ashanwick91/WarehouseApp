package com.example.warehouseapp.customer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.warehouseapp.R
import com.example.warehouseapp.databinding.ActivityCustomerBinding

class CustomerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustomerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCustomerBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)
        binding.mainTitle.text = "HOME"

        // Load the default fragment
        loadFragment(CustomerHomeFragment())
        //  clearOrderPreferences()

        // Set up bottom navigation listener
        binding.customerBottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_home -> {
                    binding.mainTitle.text = "HOME"
                    loadFragment(CustomerHomeFragment())
                    true
                }

                R.id.bottom_history -> {
                    binding.mainTitle.text = "HISTORY"
                    loadFragment(CustomerHistoryFragment())
                    true
                }

                R.id.bottom_profile -> {
                    binding.mainTitle.text = "PROFILE"
                    loadFragment(CustomerProfileFragment())
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
}