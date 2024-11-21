package com.example.warehouseapp.customer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.auth0.android.jwt.JWT
import com.example.warehouseapp.OnCustomerOrderClickListener
import com.example.warehouseapp.R
import com.example.warehouseapp.adapter.CustomerHistroyAdapter
import com.example.warehouseapp.api.ApiService
import com.example.warehouseapp.api.RetrofitClient
import com.example.warehouseapp.databinding.FragmentCustomerHistoryBinding
import com.example.warehouseapp.model.OrderDetails
import com.example.warehouseapp.model.OrderItemRequest
import com.example.warehouseapp.model.OrderRequest
import com.example.warehouseapp.model.OrdersResponse
import com.example.warehouseapp.util.readBaseUrl
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.OffsetDateTime

class CustomerHistoryFragment : Fragment(), OnCustomerOrderClickListener {

    private lateinit var binding: FragmentCustomerHistoryBinding
    private lateinit var apiService: ApiService
    private lateinit var orderRecyclerView: RecyclerView
    private lateinit var customerHistoryAdapter: CustomerHistroyAdapter
    private lateinit var carticon: ImageView
    private var customerId: String = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCustomerHistoryBinding.inflate(inflater, container, false)
        val view = binding.root

        val baseUrl = readBaseUrl(requireContext())
        apiService = RetrofitClient.getRetrofitInstance(baseUrl).create(ApiService::class.java)

        val sharedPref2 = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val token = sharedPref2.getString("jwt_token", null)

        if (token != null) {
            val jwt = JWT(token)
            customerId = jwt.getClaim("_id").asString() ?: ""
        }

        orderRecyclerView = binding.historyRecyclerView
        orderRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        customerHistoryAdapter = CustomerHistroyAdapter(emptyList(), this)
        orderRecyclerView.adapter = customerHistoryAdapter
        carticon = binding.cartIcon
        // On clicking the cart icon, create an Order and add OrderItems to it
        carticon.setOnClickListener {
            val intent = Intent(requireContext(), CustomerCartActivity::class.java)
            startActivity(intent)

        }

        fetchAllOrderHistory()
        return view
    }

    private fun fetchAllOrderHistory() {
        apiService.getOrdersBCustomer(customerId).enqueue(object : Callback<OrdersResponse> {
            override fun onResponse(call: Call<OrdersResponse>, response: Response<OrdersResponse>) {
                Log.d("getOrdersBCustomer", "Response Code: ${response.code()}")
                Log.d("getOrdersBCustomer", "Response Body: ${response.body()}")

                if (response.isSuccessful && response.body() != null) {
                    val ordersResponse = response.body()!!
                    val orders = ordersResponse.orders // Extract the list of orders
                    Log.d("fetchAllOrderHistory", orders.toString())

                    // Check if fragment is still added before updating the adapter
                    if (isAdded) {
                        customerHistoryAdapter.updateOrderList(orders) // Pass List<Order> to the adapter
                    }
                } else {
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Failed to load orders", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<OrdersResponse>, t: Throwable) {
                Log.e("CustomerHomeFragment", "Error fetching orders", t)

                // Use a safe context to avoid crashes
                if (isAdded) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    override fun onViewOrderDetails(order: OrderDetails) {
        // Create a new instance of the CustomerOrderHistoryFragment
        val customerOrderHistoryFragment = CustomerOrderHistroyFragment()

        // Create a bundle to pass data to the fragment
        val bundle = Bundle()
        bundle.putString("orderId", order._id) // Pass the order ID or other required data
        bundle.putString("orderTotal", order.orderTotal.toString())
        bundle.putString("orderDate", order.orderDate)
        customerOrderHistoryFragment.arguments = bundle

        // Navigate to the CustomerOrderHistoryFragment
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, customerOrderHistoryFragment) // Replace with your container ID
            .addToBackStack(null) // Add to back stack to allow navigation back
            .commit()
    }


}
