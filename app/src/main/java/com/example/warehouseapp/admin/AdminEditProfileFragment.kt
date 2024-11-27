package com.example.warehouseapp.admin

import android.content.Context
import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.auth0.android.jwt.JWT
import com.example.warehouseapp.R
import com.example.warehouseapp.api.ApiService
import com.example.warehouseapp.api.RetrofitClient
import com.example.warehouseapp.databinding.FragmentAdminEditProfileBinding
import com.example.warehouseapp.model.User
import com.example.warehouseapp.util.readBaseUrl
import com.google.android.material.snackbar.Snackbar
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class AdminEditProfileFragment : Fragment() {

    private lateinit var binding: FragmentAdminEditProfileBinding

    private lateinit var apiService: ApiService

    private lateinit var user: User

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentAdminEditProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.phoneNoInput.addTextChangedListener(PhoneNumberFormattingTextWatcher())

        val baseUrl = readBaseUrl(requireContext())
        apiService = RetrofitClient.getRetrofitInstance(baseUrl).create(ApiService::class.java)

        // Decode token to get user ID
        val sharedPref = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("jwt_token", null)
        var userId = ""
        if (token != null) {
            val jwt = JWT(token)
            userId = jwt.getClaim("_id").asString() ?: ""
            fetchUser(userId, token)
        }

        binding.titleBar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack() // Navigate back
        }

        binding.saveButton.setOnClickListener {
            if (validateInputs()) {
                // If all validations pass, update the profile
                val updatedFirstName = binding.firstNameInput.text.toString().trim()
                val updatedLastName = binding.lastNameInput.text.toString().trim()
                val updatedAddress = binding.addressInput.text.toString().trim()
                val updatedPhone = binding.phoneNoInput.text.toString().trim()

                updateUser(
                    userId,
                    token,
                    updatedFirstName,
                    updatedLastName,
                    updatedAddress,
                    updatedPhone
                )
            }
        }

        return view
    }

    private fun fetchUser(userId: String, token: String?) {
        token?.let {
            apiService.getUserProfile(userId, "Bearer $token").enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if (response.isSuccessful && response.body() != null) {
                        user = response.body()!!
                        // Populate the fields with the fetched user data
                        populateForm(user)
                    } else {
                        // Retrieve and parse the error body
                        val errorBody = response.errorBody()?.string()
                        errorBody?.let {
                            try {
                                val jsonObject = JSONObject(it)
                                val errorMessage = jsonObject.getString("msg")

                                val snackbar = Snackbar.make(
                                    binding.root,
                                    "User profile load failed. $errorMessage",
                                    Snackbar.LENGTH_LONG
                                )
                                snackbar.setAction(R.string.dismiss) {
                                    snackbar.dismiss();
                                }
                                snackbar.show()
                            } catch (e: Exception) {
                                Toast.makeText(
                                    requireContext(),
                                    "User profile load failed",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        // Validate First Name
        val firstName = binding.firstNameInput.text.toString().trim()
        if (firstName.isNotEmpty() && !firstName.matches("^[a-zA-Z]+$".toRegex())) {
            binding.firstNameLayout.error = "Invalid first name"
            isValid = false
        } else {
            binding.firstNameLayout.error = null
        }

        // Validate Last Name
        val lastName = binding.lastNameInput.text.toString().trim()
        if (lastName.isNotEmpty() && !lastName.matches("^[a-zA-Z]+$".toRegex())) {
            binding.lastNameLayout.error = "Invalid last name"
            isValid = false
        } else {
            binding.lastNameLayout.error = null
        }

        // Validate Address
        val address = binding.addressInput.text.toString().trim()
        if (address.isNotEmpty() && !address.matches("^[a-zA-Z0-9 ,.-]+$".toRegex())) {
            binding.addressLayout.error = "Invalid address format"
            isValid = false
        } else {
            binding.addressLayout.error = null
        }

        // Validate Phone Number
        val phoneNo = binding.phoneNoInput.text.toString().trim()
        if (phoneNo.isNotEmpty() && !phoneNo.matches("^(\\+1[-.\\s]?)?(\\(?[2-9][0-9]{2}\\)?[-.\\s]?)?[2-9][0-9]{2}[-.\\s]?[0-9]{4}$".toRegex())) {
            binding.phoneNoLayout.error = "Enter a valid phone number"
            isValid = false
        } else {
            binding.phoneNoLayout.error = null
        }

        return isValid
    }

    private fun updateUser(
        userId: String,
        token: String?,
        firstName: String,
        lastName: String,
        address: String,
        phone: String
    ) {
        // Build a map of only the updated fields
        val updatedFields = mutableMapOf<String, Any>()
        if (firstName != user.profile?.firstName) updatedFields["profile.firstName"] = firstName
        if (firstName != user.profile?.lastName) updatedFields["profile.lastName"] = lastName
        if (firstName != user.profile?.address) updatedFields["profile.address"] = address
        if (firstName != user.profile?.phoneNumber) updatedFields["profile.phoneNumber"] = phone

        apiService.updateUserProfile(userId, "Bearer $token", updatedFields)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        val snackbar = Snackbar.make(
                            binding.root,
                            "Profile updated successfully",
                            Snackbar.LENGTH_LONG
                        )
                        snackbar.setAction(R.string.dismiss) {
                            snackbar.dismiss();
                        }
                        snackbar.show()

                    } else {
                        // Retrieve and parse the error body
                        val errorBody = response.errorBody()?.string()
                        errorBody?.let {
                            try {
                                val jsonObject = JSONObject(it)
                                val errorMessage = jsonObject.getString("msg")

                                val snackbar = Snackbar.make(
                                    binding.root,
                                    "Failed to update profile. $errorMessage",
                                    Snackbar.LENGTH_LONG
                                )
                                snackbar.setAction(R.string.dismiss) {
                                    snackbar.dismiss();
                                }
                                snackbar.show()
                            } catch (e: Exception) {
                                Toast.makeText(
                                    requireContext(),
                                    "Profile update failed",
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

    private fun populateForm(user: User) {
        binding.firstNameInput.setText(user.profile?.firstName)
        binding.lastNameInput.setText(user.profile?.lastName)
        binding.addressInput.setText(user.profile?.address)
        binding.phoneNoInput.setText(user.profile?.phoneNumber)
    }
}