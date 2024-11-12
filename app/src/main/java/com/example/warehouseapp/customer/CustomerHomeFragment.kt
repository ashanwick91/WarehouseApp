package com.example.warehouseapp.customer

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.warehouseapp.OnProductItemClickListener
import com.example.warehouseapp.adapter.ProductAdapter
import com.example.warehouseapp.api.ApiService
import com.example.warehouseapp.api.RetrofitClient
import com.example.warehouseapp.databinding.FragmentCustomerHomeBinding
import com.example.warehouseapp.model.Item
import com.example.warehouseapp.model.Product
import com.example.warehouseapp.util.CartPreferences
import com.example.warehouseapp.util.readBaseUrl
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class CustomerHomeFragment : Fragment(),OnProductItemClickListener {

    private lateinit var binding: FragmentCustomerHomeBinding
    private lateinit var apiService: ApiService

    private var products: List<Product> = emptyList()
    private lateinit var productRecyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCustomerHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        val baseUrl = readBaseUrl(requireContext())
        apiService = RetrofitClient.getRetrofitInstance(baseUrl).create(ApiService::class.java)

        // Initialize RecyclerView
        productRecyclerView = binding.recyclerViewProducts
        productRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        productAdapter = ProductAdapter(emptyList(),this)
        productRecyclerView.adapter = productAdapter

        fetchAllProducts()

        return view
    }

    private fun fetchAllProducts() {
        apiService.getProducts().enqueue(object : Callback<List<Product>> {
            override fun onResponse(call: Call<List<Product>>, response: Response<List<Product>>) {
                if (response.isSuccessful && response.body() != null) {
                    val products = response.body()!!

                    // Get cart items from shared preferences
                    val cartItems = CartPreferences.getCartItems(requireContext())

                    // Merge the cart quantities with the products from the API
                    products.forEach { product ->
                        val matchingCartItem = cartItems.find { it.productId == product.id }
                        if (matchingCartItem != null) {
                            product.cartQuantity = matchingCartItem.quantity // Set the cart quantity
                        }
                    }

                    productAdapter.updateProductList(products)
                } else {
                    Toast.makeText(requireContext(), "Failed to load products", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Product>>, t: Throwable) {
                Log.e("CustomerHomeFragment", "Error fetching products", t)
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onAddToCartClick(product: Product, quantity: Int) {
        Log.d("CustomerHomeFragment", "Product Details: $product")
        if (product.id.isNullOrEmpty() || product.name.isNullOrEmpty() || product.price == null) {
            Toast.makeText(requireContext(), "Invalid product details. Cannot add to cart.", Toast.LENGTH_SHORT).show()
            return
        }

        val cartItem = Item(
            productId = product.id,
            productName = product.name,
            price = product.price,
            quantity = quantity
        )

        // Add the item to the cart using SharedPreferences
        CartPreferences.addToCart(requireContext(), cartItem)
        val cartSummary = CartPreferences.getCartSummary(requireContext())
        //Toast.makeText(requireContext(), "${product.name} added to cart. $cartSummary", Toast.LENGTH_SHORT).show()
    }

    override fun onRemoveFromCartClick(productId: String) {
        CartPreferences.removeFromCart(requireContext(), productId)
        val cartSummary = CartPreferences.getCartSummary(requireContext())
        Toast.makeText(requireContext(), "Item removed from cart. $cartSummary", Toast.LENGTH_SHORT)
            .show()

        val updatedCartItems = CartPreferences.getCartItems(requireContext())
        // Update the product list without modifying the original list reference
        val updatedProducts = products.map { product ->
            val matchingCartItem = updatedCartItems.find { it.productId == product.id }
            product.cartQuantity = matchingCartItem?.quantity ?: 0
            product
        }

        // Update the adapter with the updated product list
        productAdapter.updateProductList(updatedProducts)
    }

}