package com.example.warehouseapp.customer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.auth0.android.jwt.JWT
import com.example.warehouseapp.R
import com.example.warehouseapp.api.ApiService
import com.example.warehouseapp.api.RetrofitClient
import com.example.warehouseapp.auth.LoginActivity
import com.example.warehouseapp.databinding.FragmentCustomerProfileBinding
import com.example.warehouseapp.model.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CustomerProfileFragment : Fragment() {

    private lateinit var binding: FragmentCustomerProfileBinding
    private lateinit var apiService: ApiService
    private var customerId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCustomerProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        // Initialize API service
        val baseUrl = requireContext().getSharedPreferences("base_url", Context.MODE_PRIVATE)
            .getString("base_url", "") ?: ""
        apiService = RetrofitClient.getRetrofitInstance(baseUrl).create(ApiService::class.java)

        // Decode token to get customer ID
        val sharedPref = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("jwt_token", null)
        if (token != null) {
            val jwt = JWT(token)
            customerId = jwt.getClaim("_id").asString() ?: ""
        }

        // Fetch customer data
        fetchUserData()

        binding.customerBtnLogout.setOnClickListener {
            // Clear the JWT token from SharedPreferences
            clearTokenFromPreferences()
            // Redirect to login screen
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        binding.cardViewMyAccount.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_container, CustomerEditProfileFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.cardViewHistory.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_container, CustomerHistoryFragment())
                .addToBackStack(null)
                .commit()
        }

        return view
    }

    private fun clearTokenFromPreferences() {
        val sharedPref = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.remove("jwt_token")
        editor.apply()
    }

    private fun fetchUserData() {
        val sharedPref = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("jwt_token", null)

        if (token != null) {
            apiService.getUserProfile(customerId, "Bearer $token").enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if (response.isSuccessful && response.body() != null) {
                        val user = response.body()!!
                        // Set customer name and email
                        binding.customerName.text = "${user.profile?.firstName} ${user.profile?.lastName}"
                        binding.customerEmail.text = user.email
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Failed to fetch user data: ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(requireContext(), "Token not found. Please log in again.", Toast.LENGTH_SHORT).show()
        }
    }



}