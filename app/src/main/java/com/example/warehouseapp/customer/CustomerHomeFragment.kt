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
import java.util.Date
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.auth0.android.jwt.JWT
import com.example.warehouseapp.OnProductItemClickListener
import com.example.warehouseapp.R
import com.example.warehouseapp.adapter.ProductAdapter
import com.example.warehouseapp.api.ApiService
import com.example.warehouseapp.api.RetrofitClient
import com.example.warehouseapp.databinding.FragmentCustomerHomeBinding
import com.example.warehouseapp.model.ItemDetails
import com.example.warehouseapp.model.OrderRequest
import com.example.warehouseapp.model.OrderItemRequest
import com.example.warehouseapp.model.OrdersResponse
import com.example.warehouseapp.model.Product
import com.example.warehouseapp.util.CartPreferences
import com.example.warehouseapp.util.readBaseUrl
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale


class CustomerHomeFragment : Fragment(), OnProductItemClickListener {

    private lateinit var binding: FragmentCustomerHomeBinding
    private lateinit var apiService: ApiService
    private lateinit var productRecyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private var customerId: String = ""
    private var order: OrderRequest? = null // Changed from lateinit to nullable

    private val cartItems = mutableListOf<OrderItemRequest>()
    private var isAscendingOrder = true
    private val selectedCategories = mutableListOf<String>()


    override fun onAttach(context: Context) {
        super.onAttach(context)
        // If necessary, initialize variables or load data that requires a context here
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCustomerHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        val baseUrl = readBaseUrl(requireContext())
        apiService = RetrofitClient.getRetrofitInstance(baseUrl).create(ApiService::class.java)

        productRecyclerView = binding.recyclerViewProducts
        productRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        productAdapter = ProductAdapter(emptyList(), this, requireContext())
        productRecyclerView.adapter = productAdapter
        // Load the order from preferences
        loadOrderFromPreferences()
        val cartIcon = view.findViewById<ImageView>(R.id.cart_icon)
        cartIcon.setOnClickListener {
            val intent = Intent(requireContext(), CustomerCartActivity::class.java)
            startActivity(intent)
        }
        setupCategoryChips()
        fetchAllProducts()

        productAdapter.notifyDataSetChanged()


        binding.searchIcon.setOnClickListener {
            val query = binding.searchInput.text.toString()
            if (query.isNotEmpty()) {
                searchProducts(query)
            } else {
                fetchAllProducts() // Show all products if the search is empty
            }
        }


        binding.chipSortPrice.setOnClickListener {
            sortProductsByPrice()
        }


        // Handle price sorting
        binding.chipSortPrice.setOnClickListener {
            sortProductsByPrice()
        }


        return view
    }

    private fun fetchAllProducts() {
        apiService.getProducts().enqueue(object : Callback<List<Product>> {
            override fun onResponse(call: Call<List<Product>>, response: Response<List<Product>>) {
                if (response.isSuccessful && response.body() != null) {
                    val products = response.body()!!
                    syncCartWithProducts(products)
                    products.forEach { product ->
                        val matchingCartItem = order?.items?.find { it.productId == product.id }
                        if (matchingCartItem != null) {
                            product.cartQuantity = matchingCartItem.quantity
                        }
                    }
                    productAdapter.updateProductList(products)
                } else {
                    Toast.makeText(requireContext(), "Failed to load products", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(call: Call<List<Product>>, t: Throwable) {
                Log.e("CustomerHomeFragment", "Error fetching products", t)
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadOrderFromPreferences() {
        // Retrieve cart items as ItemDetails from CartPreferences
        val cartItems = CartPreferences.getCart(requireContext())

        // Map cartItems (ItemDetails) to OrderItemRequest
        val itemDetailsList = cartItems.map { cartItem ->
            OrderItemRequest(
                productId = cartItem.productId,
                productName = cartItem.productName,
                category = cartItem.category,
                salesAmount = cartItem.price * cartItem.quantity,
                profitAmount = 0.0, // Adjust as necessary
                quantitySold = 0, // Default or based on business logic
                transactionDate = Date(), // Default or derived value
                price = cartItem.price,
                quantity = cartItem.quantity
            )
        }

        // Populate the `order` object
        order = OrderRequest(
            customerId = customerId,
            orderDate = Date(), // Current date or derived value
            items = itemDetailsList, // Mapped items
            orderTotal = itemDetailsList.sumOf { it.salesAmount },
            status = "Pending", // Default status
            createdAt = Date() // Current timestamp
        )
    }
    override fun onShowMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun searchProducts(query: String) {
        apiService.getProducts(name = query, description = query).enqueue(object : Callback<List<Product>> {
            override fun onResponse(call: Call<List<Product>>, response: Response<List<Product>>) {
                if (response.isSuccessful && response.body() != null) {
                    productAdapter.updateProductList(response.body()!!)
                } else {
                    onShowMessage("No products found matching your search!")
                }
            }

            override fun onFailure(call: Call<List<Product>>, t: Throwable) {
                onShowMessage("Error: ${t.message}")
            }
        })
    }

    private fun sortProductsByPrice() {
        val sortedProducts = if (isAscendingOrder) {
            productAdapter.currentList.sortedBy { it.price }
        } else {
            productAdapter.currentList.sortedByDescending { it.price }
        }
        productAdapter.updateProductList(sortedProducts)

        // Toggle the sort order for the next click
        isAscendingOrder = !isAscendingOrder

        // Update the arrow icon
        updateSortArrow()
    }

    private fun updateSortArrow() {
        val sortIcon = binding.chipSortPrice // Update with actual arrow ImageView reference
        if (isAscendingOrder) {
            sortIcon.setChipIconResource(R.drawable.ic_arrow_up) // Replace with your drawable
        } else {
            sortIcon.setChipIconResource(R.drawable.ic_arrow_down) // Replace with your drawable
        }
    }



    private fun handleCategorySelection(category: String, isSelected: Boolean) {
        if (isSelected) {
            if (!selectedCategories.contains(category)) {
                selectedCategories.add(category)
            }
        } else {
            selectedCategories.remove(category)
        }
        Log.d("CustomerHomeFragment", "Selected categories: $selectedCategories")
        fetchFilteredProducts()
    }

    private fun fetchFilteredProducts() {
        if (selectedCategories.isEmpty()) {
            fetchAllProducts()
            return
        }

        val selectedCategoriesString = selectedCategories.joinToString(",")
        apiService.getProducts(category = selectedCategoriesString).enqueue(object : Callback<List<Product>> {
            override fun onResponse(call: Call<List<Product>>, response: Response<List<Product>>) {
                if (response.isSuccessful && response.body() != null) {
                    val products = response.body()!!
                    Log.d("CustomerHomeFragment", "Filtered Products: $products")

                    productAdapter.updateProductList(products) // Update the adapter with the new products
                    Log.d("CustomerHomeFragment", "ProductAdapter updated with ${products.size} products")
                } else {
                    Toast.makeText(requireContext(), "No products found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Product>>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupCategoryChips() {
        binding.chipBoots.setOnCheckedChangeListener { _, isChecked ->
            handleCategorySelection("Boots", isChecked)
        }
        binding.chipCasual.setOnCheckedChangeListener { _, isChecked ->
            handleCategorySelection("Casual", isChecked)
        }
        binding.chipSports.setOnCheckedChangeListener { _, isChecked ->
            handleCategorySelection("Sports", isChecked)
        }


    }

    private fun syncCartWithProducts(products: List<Product>) {
        val cartItems = CartPreferences.getCart(requireContext())
        for (product in products) {
            val cartItem = cartItems.find { it.productId == product.id }
            if (cartItem != null) {
                product.cartQuantity = cartItem.quantity
            }
        }
        productAdapter.updateProductList(products)
    }

}