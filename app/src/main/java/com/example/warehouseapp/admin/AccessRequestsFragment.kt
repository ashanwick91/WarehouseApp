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
import com.example.warehouseapp.R
import com.example.warehouseapp.adapter.UserAdapter
import com.example.warehouseapp.api.ApiService
import com.example.warehouseapp.api.RetrofitClient
import com.example.warehouseapp.databinding.FragmentAccessRequestsBinding
import com.example.warehouseapp.listener.AccessRequestItemClickListener
import com.example.warehouseapp.model.Role
import com.example.warehouseapp.model.User
import com.example.warehouseapp.util.readBaseUrl
import com.google.android.material.snackbar.Snackbar
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AccessRequestsFragment : Fragment(), AccessRequestItemClickListener {

    private lateinit var binding: FragmentAccessRequestsBinding

    private lateinit var userAdapter: UserAdapter
    private lateinit var apiService: ApiService

    private lateinit var token: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentAccessRequestsBinding.inflate(inflater, container, false)
        val view = binding.root

        // Retrieve the token from SharedPreferences
        val sharedPref = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        token = sharedPref.getString("jwt_token", null) ?: ""

        val baseUrl = readBaseUrl(requireContext())
        apiService = RetrofitClient.getRetrofitInstance(baseUrl).create(ApiService::class.java)

        // Initialize RecyclerView
        val productRecyclerView = binding.rvAccessRequests
        productRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        userAdapter = UserAdapter(this)
        productRecyclerView.adapter = userAdapter

        fetchUsers(Role.ADMIN.type)

        binding.titleBar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack() // Navigate back
        }

        return view
    }

    private fun fetchUsers(role: String) {

        apiService.getUsers(role, "Bearer $token").enqueue(object : Callback<List<User>> {

            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful && response.body() != null) {
                    val users = response.body()!!
                    userAdapter.updateUserList(users)

                } else {
                    // Retrieve and parse the error body
                    val errorBody = response.errorBody()?.string()
                    errorBody?.let {
                        try {
                            val jsonObject = JSONObject(it)
                            val errorMessage = jsonObject.getString("msg")

                            val snackbar = Snackbar.make(
                                binding.root,
                                "Users load failed. $errorMessage",
                                Snackbar.LENGTH_LONG
                            )
                            snackbar.setAction(R.string.dismiss) {
                                snackbar.dismiss();
                            }
                            snackbar.show()
                        } catch (e: Exception) {
                            Toast.makeText(
                                requireContext(),
                                "Users load failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                t.message?.let { Log.e("WarehouseApp", it) }
            }

        })
    }

    override fun onApproveClick(userId: String) {
        apiService.approveUser(userId, "Bearer $token")
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        val snackbar = Snackbar.make(
                            binding.root,
                            "User approved successfully",
                            Snackbar.LENGTH_LONG
                        )
                        snackbar.setAction(R.string.dismiss) {
                            snackbar.dismiss();
                        }
                        snackbar.show()

                        fetchUsers(Role.ADMIN.type)

                    } else {
                        // Retrieve and parse the error body
                        val errorBody = response.errorBody()?.string()
                        errorBody?.let {
                            try {
                                val jsonObject = JSONObject(it)
                                val errorMessage = jsonObject.getString("msg")

                                val snackbar = Snackbar.make(
                                    binding.root,
                                    "Failed to approve user. $errorMessage",
                                    Snackbar.LENGTH_LONG
                                )
                                snackbar.setAction(R.string.dismiss) {
                                    snackbar.dismiss();
                                }
                                snackbar.show()
                            } catch (e: Exception) {
                                Toast.makeText(
                                    requireContext(),
                                    "User approval failed",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }
}