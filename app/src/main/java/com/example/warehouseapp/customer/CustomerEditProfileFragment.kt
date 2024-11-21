package com.example.warehouseapp.customer

import android.content.Context
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.auth0.android.jwt.JWT
import com.example.warehouseapp.api.ApiService
import com.example.warehouseapp.api.RetrofitClient
import com.example.warehouseapp.databinding.FragmentCustomerEditProfileBinding
import com.example.warehouseapp.model.Profile
import com.example.warehouseapp.model.User
import com.example.warehouseapp.util.readBaseUrl
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class CustomerEditProfileFragment : Fragment() {

    private lateinit var binding: FragmentCustomerEditProfileBinding
    private lateinit var apiService: ApiService
    private var customerId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCustomerEditProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        // Get the base URL and initialize the API service
        val baseUrl = readBaseUrl(requireContext())
        apiService = RetrofitClient.getRetrofitInstance(baseUrl).create(ApiService::class.java)

        // Decode token to get customer ID
        val sharedPrefEdit = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val token = sharedPrefEdit.getString("jwt_token", null)
        if (token != null) {
            val jwt = JWT(token)
            customerId = jwt.getClaim("_id").asString() ?: ""
        }

        // Fetch user data from the API
        fetchUserData()

        // Handle Save button click
        binding.buttonSave.setOnClickListener {
            validateAndUpdateProfile()
        }

        return view
    }

    private fun fetchUserData() {
        val sharedPref = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("jwt_token", null)

        if (token != null) {
            apiService.getUserProfile(customerId, "Bearer $token").enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if (response.isSuccessful && response.body() != null) {
                        val user = response.body()!!
                        // Populate the fields with the fetched user data
                        binding.editTextFirstName.setText(user.profile?.firstName)
                        binding.editTextLastName.setText(user.profile?.lastName)
                        binding.editTextEmail.setText(user.email)
                        binding.editTextPassword.setText("")
                        binding.editTextAddress.setText(user.profile?.address)
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
        } else {
            Toast.makeText(context, "Token not found. Please log in again.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateAndUpdateProfile() {
        var isValid = true

        // Validate email
        val updatedEmail = binding.editTextEmail.text.toString()
        if (updatedEmail.isBlank()) {
            binding.editTextEmail.error = "Email cannot be empty"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(updatedEmail).matches()) {
            binding.editTextEmail.error = "Invalid email format"
            isValid = false
        } else {
            binding.editTextEmail.error = null
        }

        // Validate password if entered
        val updatedPassword = binding.editTextPassword.text.toString()
        if (updatedPassword.isNotBlank() && updatedPassword.length < 6) {
            binding.editTextPassword.error = "Password must be at least 6 characters"
            isValid = false
        } else {
            binding.editTextPassword.error = null
        }

        // Validate other fields
        val updatedFirstName = binding.editTextFirstName.text.toString()
        val updatedLastName = binding.editTextLastName.text.toString()
        val updatedAddress = binding.editTextAddress.text.toString()
        val updatedPhone = binding.editTextPhone.text.toString()

        // If all validations pass, update the profile
        if (isValid) {
            updateUserProfile(updatedFirstName, updatedLastName, updatedEmail, updatedPassword, updatedAddress,updatedPhone)
        }
    }

    private fun updateUserProfile(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        address: String,
        phone: String
    ) {
        val sharedPref = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("jwt_token", null)

        if (token == null) {
            Toast.makeText(context, "Token not found. Please log in again.", Toast.LENGTH_SHORT).show()
            return
        }

        // Build a map of only the updated fields
        val updatedFields = mutableMapOf<String, Any>()
        if (firstName.isNotBlank()) updatedFields["profile.firstName"] = firstName
        if (lastName.isNotBlank()) updatedFields["profile.lastName"] = lastName
        if (email.isNotBlank()) updatedFields["email"] = email
        if (password.isNotBlank()) updatedFields["password"] = password
        if (address.isNotBlank()) updatedFields["profile.address"] = address
        if (phone.isNotBlank()) updatedFields["profile.phoneNumber"] = phone

        if (updatedFields.isEmpty()) {
            Toast.makeText(context, "No changes detected to update.", Toast.LENGTH_SHORT).show()
            return
        }

        // Make the API call to update the user profile
        apiService.updateUserProfile(customerId, "Bearer $token", updatedFields)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Snackbar.make(
                            requireView(),
                            "Profile updated successfully",
                            Snackbar.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Failed to update profile: ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }


}