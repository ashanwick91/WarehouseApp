package com.example.warehouseapp.admin

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.warehouseapp.R
import com.example.warehouseapp.adapter.AdminProductAdapter
import com.example.warehouseapp.api.ApiService
import com.example.warehouseapp.api.RetrofitClient
import com.example.warehouseapp.databinding.FragmentAdminInventoryBinding
import com.example.warehouseapp.listener.AdminProductItemClickListener
import com.example.warehouseapp.model.Product
import com.example.warehouseapp.util.readBaseUrl
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class AdminInventoryFragment : Fragment(R.layout.fragment_admin_inventory),
    AdminProductItemClickListener {

    private var _binding: FragmentAdminInventoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var productAdapter: AdminProductAdapter
    private lateinit var apiService: ApiService

    var searchTerm: String? = null
    val filters = mutableSetOf("name")
    private lateinit var token: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAdminInventoryBinding.bind(view)

        // Retrieve the token from SharedPreferences
        val sharedPref = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        token = sharedPref.getString("jwt_token", null) ?: ""

        val baseUrl = readBaseUrl(requireContext())
        apiService = RetrofitClient.getRetrofitInstance(baseUrl).create(ApiService::class.java)

        // Initialize RecyclerView
        val productRecyclerView = binding.rvProductsAdmin
        productRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        productAdapter = AdminProductAdapter(this)
        productRecyclerView.adapter = productAdapter

        fetchProducts()

        // Set up the toolbar
        binding.topAppBar.apply {
            setNavigationOnClickListener {
                // Handle back navigation
            }

            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.search -> {
                        true
                    }

                    else -> false
                }
            }
        }

        val searchItem = binding.topAppBar.menu.findItem(R.id.search)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    searchTerm = it
                    fetchProducts(searchTerm, filters)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {

                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                searchTerm = null
                fetchProducts()
                return true
            }
        })

        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            filters.clear()

            if (checkedIds.contains(binding.chipName.id)) {
                filters.add("name")
            }
            if (checkedIds.contains(binding.chipDesc.id)) {
                filters.add("description")
            }

            if (!searchTerm.isNullOrEmpty()) {
                fetchProducts(searchTerm, filters)
            }
        }

        binding.fabAddProduct.setOnClickListener{
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_container, AddProductFragment())
                .addToBackStack(null)
                .commit()
        }

    }

    private fun fetchProducts(searchTerm: String? = null, filters: Set<String> = emptySet()) {

        val name = if (filters.contains("name")) searchTerm else null
        val description = if (filters.contains("description")) searchTerm else null

        apiService.getProducts(name, description).enqueue(object : Callback<List<Product>> {

            override fun onResponse(call: Call<List<Product>>, response: Response<List<Product>>) {
                if (response.isSuccessful && response.body() != null) {
                    val products = response.body()!!
                    productAdapter.updateProductList(products)

                } else {
                    // Retrieve and parse the error body
                    val errorBody = response.errorBody()?.string()
                    errorBody?.let {
                        try {
                            val jsonObject = JSONObject(it)
                            val errorMessage = jsonObject.getString("msg")

                            val snackbar = Snackbar.make(
                                binding.root,
                                "Product load failed. $errorMessage",
                                Snackbar.LENGTH_LONG
                            )
                            snackbar.setAction(R.string.dismiss) {
                                snackbar.dismiss();
                            }
                            snackbar.show()
                        } catch (e: Exception) {
                            Toast.makeText(
                                requireContext(),
                                "Product load failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }

            override fun onFailure(call: Call<List<Product>>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Function to delete a product
    private fun deleteProduct(productId: String) {
        apiService.deleteProduct(productId, "Bearer $token").enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    val snackbar = Snackbar.make(
                        binding.root,
                        "Product deleted successfully",
                        Snackbar.LENGTH_LONG
                    )
                    snackbar.setAction(R.string.dismiss) {
                        snackbar.dismiss();
                    }
                    snackbar.show()

                    // Refresh the product list after deletion
                    fetchProducts(searchTerm, filters)

                } else {
                    // Retrieve and parse the error body
                    val errorBody = response.errorBody()?.string()
                    errorBody?.let {
                        try {
                            val jsonObject = JSONObject(it)
                            val errorMessage = jsonObject.getString("msg")

                            val snackbar = Snackbar.make(
                                binding.root,
                                "Product deletion failed. $errorMessage",
                                Snackbar.LENGTH_LONG
                            )
                            snackbar.setAction(R.string.dismiss) {
                                snackbar.dismiss();
                            }
                            snackbar.show()
                        } catch (e: Exception) {
                            Toast.makeText(
                                requireContext(),
                                "Product deletion failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemCLick(productId: String) {
        showDeleteConfirmationDialog(productId)
    }

    // Show confirmation dialog before deleting a product
    private fun showDeleteConfirmationDialog(productId: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Product")
            .setMessage("Are you sure you want to delete this product?")
            .setPositiveButton("DELETE") { dialog, _ ->
                deleteProduct(productId)  // Call delete function if confirmed
                dialog.dismiss()
            }
            .setNegativeButton("CANCEL") { dialog, _ ->
                dialog.dismiss()  // Dismiss the dialog if canceled
            }
            .show()
    }
}