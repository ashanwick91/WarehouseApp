package com.example.warehouseapp.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.auth0.android.jwt.JWT
import com.example.warehouseapp.MainActivity
import com.example.warehouseapp.R
import com.example.warehouseapp.api.ApiService
import com.example.warehouseapp.api.RetrofitClient
import com.example.warehouseapp.databinding.ActivityLoginBinding
import com.example.warehouseapp.model.LoginRequest
import com.example.warehouseapp.model.LoginResponse
import com.example.warehouseapp.util.ActivityLogger
import com.example.warehouseapp.util.readBaseUrl
import com.google.android.material.snackbar.Snackbar
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)
        clearOrderPreferences()
        val baseUrl = readBaseUrl(this)
        apiService = RetrofitClient.getRetrofitInstance(baseUrl).create(ApiService::class.java)

        binding.loginButton.setOnClickListener {
            validateInputs()
        }

        binding.signupLink.setOnClickListener {
            // Redirect to SignupActivity
            val intent = Intent(this@LoginActivity, SignupActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun validateInputs() {
        var isValid = true

        val emailInput = binding.loginEmailInput.text.toString()
        if (emailInput.isEmpty()) {
            binding.loginEmailLayout.error = "Email is required"
            isValid = false
        } else {
            binding.loginEmailLayout.error = null
        }

        val passwordInput = binding.loginPasswordInput.text.toString()
        if (passwordInput.isEmpty()) {
            binding.loginPasswordLayout.error = "Password is required"
            isValid = false
        } else {
            binding.loginPasswordLayout.error = null
        }

        // If all validations are successful
        if (isValid) {
            performLogin(emailInput, passwordInput)
        }
    }

    private fun performLogin(email: String, password: String) {
        val loginRequest = LoginRequest(email, password)

        // Make the network call using Retrofit
        apiService.loginUser(loginRequest).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val token = response.body()?.accessToken
                    token?.let {
                        Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT)
                            .show()

                        // Save the token in SharedPreferences
                        saveTokenToPreferences(token)

                        // Log activity after successful login
                        ActivityLogger.logActivity(
                            context = this@LoginActivity,
                            apiService = apiService,
                            action = "Login",
                            details = "User $email logged in successfully"
                        )

                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)

                        finish()
                    }

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
                            Toast.makeText(this@LoginActivity, "Login failed", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Function to save JWT token in SharedPreferences
    private fun saveTokenToPreferences(token: String) {
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("jwt_token", token)
        editor.apply()
    }

    private fun clearOrderPreferences() {
        val sharedPref = getSharedPreferences("order_prefs", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.clear()
        editor.apply()
    }

}