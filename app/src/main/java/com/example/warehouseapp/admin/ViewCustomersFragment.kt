package com.example.warehouseapp.admin

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.warehouseapp.adapter.CustomerListAdapter
import com.example.warehouseapp.api.ApiService
import com.example.warehouseapp.api.RetrofitClient
import com.example.warehouseapp.databinding.FragmentViewCustomersBinding
import com.example.warehouseapp.model.User
import com.example.warehouseapp.util.readBaseUrl
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ViewCustomersFragment : Fragment() {

    private lateinit var binding: FragmentViewCustomersBinding
    private lateinit var customerListAdapter: CustomerListAdapter
    private lateinit var apiService: ApiService
    private lateinit var token: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewCustomersBinding.inflate(inflater, container, false)

        // Initialize API service
        val baseUrl = readBaseUrl(requireContext())
        apiService = RetrofitClient.getRetrofitInstance(baseUrl).create(ApiService::class.java)

        // Retrieve token from SharedPreferences
        val sharedPref = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        token = sharedPref.getString("jwt_token", null) ?: ""

        // Setup RecyclerView
        setupRecyclerView()

        // Fetch customer data
        fetchCustomers()

        // Set navigation onClickListener
        binding.titleBar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack() // Navigate back
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        // Initialize RecyclerView and Adapter
        binding.rvViewCustomers.layoutManager = LinearLayoutManager(requireContext())
        customerListAdapter = CustomerListAdapter()
        binding.rvViewCustomers.adapter = customerListAdapter
    }

    private fun fetchCustomers() {
        apiService.getUsers("customer", "Bearer $token").enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful && response.body() != null) {
                    val users = response.body()!! // List<User>
                    Log.d("ViewCustomersFragment", "API Response: ${response.body()}")
                    customerListAdapter.updateCustomerList(users)
                } else {
                    Toast.makeText(requireContext(), "Failed to load customers", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
