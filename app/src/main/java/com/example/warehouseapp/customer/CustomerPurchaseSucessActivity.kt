package com.example.warehouseapp.customer

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.warehouseapp.R
import com.example.warehouseapp.databinding.ActivityCustomerBinding
import com.example.warehouseapp.databinding.ActivityCustomerCartBinding
import com.example.warehouseapp.databinding.ActivityCustomerPurchaseSucessBinding

class CustomerPurchaseSucessActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCustomerPurchaseSucessBinding
    private lateinit var continueShopping: Button
    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityCustomerPurchaseSucessBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)
        super.onCreate(savedInstanceState)
        continueShopping = binding.continueButton

        continueShopping.setOnClickListener{
            val intent = Intent(this, CustomerActivity::class.java)
            startActivity(intent)
        }


    }
}