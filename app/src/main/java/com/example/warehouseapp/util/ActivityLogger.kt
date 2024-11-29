package com.example.warehouseapp.util

import android.content.Context
import android.util.Log
import com.auth0.android.jwt.JWT
import com.example.warehouseapp.api.ApiService
import com.example.warehouseapp.model.ActivityLog
import com.example.warehouseapp.model.Metadata
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.OffsetDateTime

object ActivityLogger {

    fun logActivity(
        context: Context,
        apiService: ApiService,
        action: String,
        details: String,
        device: String = "Android",
        ip: String = "Unknown",
        location: String = "Unknown"
    ) {
        // Retrieve the token from SharedPreferences
        val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("jwt_token", null) ?: run {
            Log.e("ActivityLogger", "JWT Token not found")
            return
        }

        // Decode the token to extract userId and role
        var userId: String? = null
        var role: String? = null
        token?.let {
            val jwt = JWT(it)
            userId = jwt.getClaim("_id").asString()
            role = jwt.getClaim("role").asString()
        }

        // Create ActivityLog object
        val activityLog = ActivityLog(
            userId = userId!!,
            action = action,
            details = details,
            timestamp = OffsetDateTime.now(),
            metadata = Metadata(device = device, ip = ip, location = location, role = role!!)
        )

        // Call the API
        apiService.logActivity("Bearer $token", activityLog).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("ActivityLogger", "Activity logged successfully")
                } else {
                    Log.e("ActivityLogger", "Failed to log activity: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("ActivityLogger", "Error logging activity: ${t.message}")
            }
        })
    }
}
