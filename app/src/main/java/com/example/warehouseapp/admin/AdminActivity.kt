package com.example.warehouseapp.admin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.warehouseapp.R
import com.example.warehouseapp.databinding.ActivityAdminBinding

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAdminBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)

        // Load the default fragment
        loadFragment(AdminInventoryFragment())

        // Set up bottom navigation listener
        binding.bottomNavigation.setOnItemSelectedListener { item ->
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
}