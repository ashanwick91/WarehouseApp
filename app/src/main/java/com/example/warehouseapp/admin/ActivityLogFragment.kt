package com.example.warehouseapp.admin

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.warehouseapp.R
import com.example.warehouseapp.adapter.ActivityLogAdapter
import com.example.warehouseapp.api.ApiService
import com.example.warehouseapp.api.RetrofitClient
import com.example.warehouseapp.databinding.FragmentActivityLogBinding
import com.example.warehouseapp.model.ActivityLog
import com.example.warehouseapp.util.readBaseUrl
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ActivityLogFragment : Fragment() {

    private lateinit var binding: FragmentActivityLogBinding
    private lateinit var apiService: ApiService
    private lateinit var activityLogAdapter: ActivityLogAdapter
    private lateinit var token: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentActivityLogBinding.inflate(inflater, container, false)

        // Initialize API service and token
        val baseUrl = readBaseUrl(requireContext())
        apiService = RetrofitClient.getRetrofitInstance(baseUrl).create(ApiService::class.java)
        val sharedPref = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        token = sharedPref.getString("jwt_token", null) ?: ""

        // Set up RecyclerView
        setupRecyclerView()

        // Fetch activity logs
        fetchActivityLogs()

        // Handle back navigation
        binding.titleBar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return binding.root

    }

    private fun setupRecyclerView() {
        binding.rvActivityLogs.layoutManager = LinearLayoutManager(requireContext())
        activityLogAdapter = ActivityLogAdapter()
        binding.rvActivityLogs.adapter = activityLogAdapter
    }

    private fun fetchActivityLogs() {
        apiService.getActivityLogs("Bearer $token").enqueue(object : Callback<List<ActivityLog>> {
            override fun onResponse(
                call: Call<List<ActivityLog>>,
                response: Response<List<ActivityLog>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val logs = response.body()!!
                    val customerLogs = logs.filter { it.metadata.role == "customer" }
                    Log.d("ActivityLogFragment", "Logs fetched: $logs")
                    activityLogAdapter.updateLogs(customerLogs)
                } else {
                    Toast.makeText(requireContext(), "Failed to load activity logs", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<ActivityLog>>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


}