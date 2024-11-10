package com.example.warehouseapp.customer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.warehouseapp.R
import com.example.warehouseapp.auth.LoginActivity
import com.example.warehouseapp.databinding.FragmentAdminHomeBinding
import com.example.warehouseapp.databinding.FragmentCustomerProfileBinding


class CustomerProfileFragment : Fragment() {

    private lateinit var binding: FragmentCustomerProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCustomerProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.customerBtnLogout.setOnClickListener {
            // Clear the JWT token from SharedPreferences
            clearTokenFromPreferences()

            // Redirect to login screen
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        return view
    }

    private fun clearTokenFromPreferences() {
        val sharedPref = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.remove("jwt_token")
        editor.apply()
    }


}