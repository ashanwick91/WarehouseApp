package com.example.warehouseapp.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.warehouseapp.R
import com.example.warehouseapp.api.ApiService
import com.example.warehouseapp.api.RetrofitClient
import com.example.warehouseapp.databinding.ActivitySignupBinding
import com.example.warehouseapp.model.RegisterRequest
import com.example.warehouseapp.model.RegisterResponse
import com.example.warehouseapp.util.readBaseUrl
import com.google.android.material.snackbar.Snackbar
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignupBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)

        val baseUrl = readBaseUrl(this)
        apiService = RetrofitClient.getRetrofitInstance(baseUrl).create(ApiService::class.java)

        val roles = resources.getStringArray(R.array.roles)
        val arrayAdapter = ArrayAdapter(this, R.layout.dropdown_item, roles)
        binding.roleInput.setAdapter(arrayAdapter)

        val snackbar = Snackbar.make(view, R.string.info_admin_chosen, Snackbar.LENGTH_LONG)

        binding.roleInput.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, _, _ ->
                val role = binding.roleInput.text.toString()
                if (role == "Admin") {
                    snackbar.setAction(R.string.dismiss) {
                        snackbar.dismiss();
                    }
                    snackbar.show()
                } else {
                    snackbar.dismiss()
                }
            }

        binding.signupButton.setOnClickListener {
            validateInputs()
        }

        binding.loginLink.setOnClickListener {
            // Redirect to LoginActivity
            val intent = Intent(this@SignupActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun validateInputs() {
        var isValid = true

        // Validate Email
        val emailInput = binding.emailInput.text.toString()
        if (emailInput.isEmpty()) {
            binding.emailLayout.error = "Email cannot be empty"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            binding.emailLayout.error = "Please enter a valid email"
            isValid = false
        } else {
            binding.emailLayout.error = null
        }

        // Validate Password
        val passwordInput = binding.passwordInput.text.toString()
        if (passwordInput.isEmpty()) {
            binding.passwordLayout.error = "Password cannot be empty"
            isValid = false
        } else if (passwordInput.length < 6) {
            binding.passwordLayout.error = "Password must be at least 6 characters"
            isValid = false
        } else {
            binding.passwordLayout.error = null
        }

        // Validate Re-enter Password
        val reEnterPasswordInput = binding.reEnterPasswordInput.text.toString()
        if (reEnterPasswordInput.isEmpty()) {
            binding.reEnterPasswordLayout.error = "Please re-enter your password"
            isValid = false
        } else if (reEnterPasswordInput != passwordInput) {
            binding.reEnterPasswordLayout.error = "Passwords do not match"
            isValid = false
        } else {
            binding.reEnterPasswordLayout.error = null
        }

        // If all validations are successful
        if (isValid) {
            performSignup(
                emailInput,
                reEnterPasswordInput,
                binding.roleInput.text.toString().lowercase()
            )
        }
    }

    private fun performSignup(email: String, password: String, role: String) {
        val registerRequest = RegisterRequest(email, password, role)

        apiService.registerUser(registerRequest).enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(
                call: Call<RegisterResponse>,
                response: Response<RegisterResponse>
            ) {
                if (response.isSuccessful) {
                    Toast.makeText(this@SignupActivity, "Signup successful", Toast.LENGTH_SHORT)
                        .show()

                    // Redirect to LoginActivity
                    val intent = Intent(this@SignupActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()

                } else {
                    // Retrieve and parse the error body
                    val errorBody = response.errorBody()?.string()
                    errorBody?.let {
                        try {
                            val jsonObject = JSONObject(it)
                            val errorMessage = jsonObject.getString("msg")

                            val snackbar = Snackbar.make(
                                binding.root,
                                "Login failed. $errorMessage",
                                Snackbar.LENGTH_LONG
                            )
                            snackbar.setAction(R.string.dismiss) {
                                snackbar.dismiss();
                            }
                            snackbar.show()
                        } catch (e: Exception) {
                            Toast.makeText(this@SignupActivity, "Signup failed", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                Toast.makeText(this@SignupActivity, "Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }
}