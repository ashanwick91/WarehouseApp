package com.example.warehouseapp.api

import com.example.warehouseapp.model.ActivityLog
import com.example.warehouseapp.model.FinancialReport
import com.example.warehouseapp.model.LoginRequest
import com.example.warehouseapp.model.LoginResponse
import com.example.warehouseapp.model.OrderDetails
import com.example.warehouseapp.model.OrderRequest
import com.example.warehouseapp.model.OrdersResponse
import com.example.warehouseapp.model.Product
import com.example.warehouseapp.model.RegisterRequest
import com.example.warehouseapp.model.RegisterResponse
import com.example.warehouseapp.model.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("/register")
    fun registerUser(@Body registerRequest: RegisterRequest): Call<RegisterResponse>

    @POST("/login")
    fun loginUser(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @GET("/reports")
    fun getFinancialReport(
        @Query("reportType") reportType: String,
        @Query("filterMonth") filterMonth: String? = null,
        @Query("filterDate") filterDate: String? = null
    ): Call<FinancialReport>

    @GET("/products")
    fun getProducts(
        @Query("name") name: String? = null,
        @Query("description") description: String? = null,
        @Query("category") category: String? = null,
        @Query("sort") sort: String? = null
    ): Call<List<Product>>

    @DELETE("/products/{product_id}")
    fun deleteProduct(
        @Path("product_id") productId: String,
        @Header("Authorization") token: String
    ): Call<Void>

    @POST("/order")
    fun placeOrder(@Body cartRequest: OrderRequest): Call<OrderRequest>

    @GET("/categories")
    fun getCategories(
        @Header("Authorization") token: String
    ): Call<List<String>> // The response is a list of category names

    @POST("/categories")
    fun addCategory(
        @Header("Authorization") token: String,
        @Body category: Map<String, String>
    ): Call<Void> // Adjust return type if needed

    @POST("/products")
    fun addProduct(
        @Header("Authorization") token: String,
        @Body product: Product
    ): Call<Void>

    @PUT("/products/{id}")
    fun updateProduct(
        @Header("Authorization") token: String,
        @Path("id") productId: String,
        @Body product: Product
    ): Call<Void>

    @GET("/orders/{customer_id}")
    fun getOrdersBCustomer(
        @Path("customer_id") customerId: String
    ): Call<OrdersResponse>

    @GET("/order/{order_id}")
    fun getOrdersByOrderID(
        @Path("order_id") orderId: String
    ): Call<OrderDetails>

    @GET("/users")
    fun getUsers(
        @Query("role") role: String,
        @Header("Authorization") token: String
    ): Call<List<User>>

    @GET("/users/{id}")
    fun getUserProfile(
        @Path("id") userId: String,
        @Header("Authorization") token: String
    ): Call<User>

    @PUT("/users/{id}")
    fun updateUserProfile(
        @Path("id") userId: String,
        @Header("Authorization") token: String,
        @Body updatedFields: Map<String, @JvmSuppressWildcards Any>
    ): Call<Void>

    @PUT("/users/{id}/approve")
    fun approveUser(
        @Path("id") userId: String,
        @Header("Authorization") token: String,
    ): Call<Void>


    @POST("/activity-log")
    fun logActivity(
        @Header("Authorization") token: String,
        @Body activityLog: ActivityLog
    ): Call<Void>

    @GET("/activity-log")
    fun getActivityLogs(@Header("Authorization") token: String): Call<List<ActivityLog>>


}