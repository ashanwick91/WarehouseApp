package com.example.warehouseapp.admin

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
import com.example.warehouseapp.databinding.FragmentAdminProfileBinding
import com.example.warehouseapp.model.User
import com.example.warehouseapp.util.readBaseUrl
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminProfileFragment : Fragment() {

    private lateinit var binding: FragmentAdminProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentAdminProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        // Decode token to get user ID
        val sharedPrefEdit =
            requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val token = sharedPrefEdit.getString("jwt_token", null)
        if (token != null) {
            val jwt = JWT(token)
            val userId = jwt.getClaim("_id").asString() ?: ""
            fetchUser(userId, token)
        }

        binding.cvAccessRequests.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_container, AccessRequestsFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.adminBtnLogout.setOnClickListener {
            // Clear the JWT token from SharedPreferences
            clearTokenFromPreferences()

            // Redirect to login screen
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        binding.cvCustomers.setOnClickListener{
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_container, ViewCustomersFragment())
                .addToBackStack(null)
                .commit()
        }

        return view
    }

    private fun fetchUser(userId: String, token: String?) {
        val baseUrl = readBaseUrl(requireContext())
        val apiService = RetrofitClient.getRetrofitInstance(baseUrl).create(ApiService::class.java)

        token?.let {
            apiService.getUserProfile(userId, "Bearer $token").enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if (response.isSuccessful && response.body() != null) {
                        val user = response.body()!!
                        // Populate the fields with the fetched user data
                        binding.adminEmail.text = user.email
                        binding.adminName.text =
                            String.format("${user.profile?.firstName ?: ""} ${user.profile?.lastName ?: ""}")
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Failed to fetch user data: ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun clearTokenFromPreferences() {
        val sharedPref = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.remove("jwt_token")
        editor.apply()
    }
}