package com.example.warehouseapp.admin

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
import androidx.fragment.app.Fragment
import com.example.warehouseapp.R
import com.example.warehouseapp.api.ApiService
import com.example.warehouseapp.api.RetrofitClient
import com.example.warehouseapp.databinding.FragmentAddProductBinding
import com.example.warehouseapp.model.Product
import com.example.warehouseapp.util.loadImageFromFirebase
import com.example.warehouseapp.util.readBaseUrl
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.UUID

class AddProductFragment(
    private val product: Product? = null
) : Fragment() {

    private var _binding: FragmentAddProductBinding? = null
    private val binding get() = _binding!!

    private lateinit var token: String
    private lateinit var apiService: ApiService

    private var image: Uri? = null
    private var imageUrl: String? = null

    var categories = emptyList<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAddProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve token from SharedPreferences
        val sharedPref = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        token = sharedPref.getString("jwt_token", null) ?: ""

        val baseUrl = readBaseUrl(requireContext())
        apiService = RetrofitClient.getRetrofitInstance(baseUrl).create(ApiService::class.java)

        if (product != null) {
            binding.tvTitle.text = String.format("Edit Product")
            fetchCategories(product.category)
            populateForm(product)
        } else {
            fetchCategories()
        }

        binding.ivProductImage.setOnClickListener { chooseCameraOrPhotos() }

        binding.btnUploadImage.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE

            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference

            val uploadTask =
                storageRef.child("product_images/" + UUID.randomUUID().toString()).putFile(image!!)

            uploadTask.addOnFailureListener {
                binding.progressBar.visibility = View.INVISIBLE
                val snackbar = Snackbar.make(
                    binding.root,
                    "Image upload failed! Please try again..",
                    Snackbar.LENGTH_LONG
                )
                snackbar.setAction(R.string.dismiss) {
                    snackbar.dismiss();
                }
                snackbar.show()

            }.addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot ->
                binding.progressBar.visibility = View.INVISIBLE
                val snackbar = Snackbar.make(
                    binding.root,
                    "Image uploaded Successfully.",
                    Snackbar.LENGTH_LONG
                )
                snackbar.setAction(R.string.dismiss) {
                    snackbar.dismiss();
                }
                snackbar.show()

                binding.btnUploadImage.isEnabled = false
                imageUrl = taskSnapshot.storage.path
            }
        }

        // Set up the Add Category button
        binding.btnAddCat.setOnClickListener {
            showAddCategoryDialog()
        }

        // Set up the save button
        binding.saveButton.setOnClickListener {
            if (validateInputs()) {
                if (product != null) {
                    updateProduct(product)
                } else {
                    saveProduct()
                }
            }
        }

        binding.clearButton.setOnClickListener {
            clearInputs()
        }

        binding.titleBar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack() // Navigate back
        }
    }

    private fun populateForm(product: Product) {
        product.imageUrl?.let { loadImageFromFirebase(it, binding.ivProductImage) }
        binding.nameInput.setText(product.name)
        binding.descInput.setText(product.description)
        binding.priceInput.setText(product.price.toString())
        binding.originalPriceInput.setText(product.originalPrice.toString())
        binding.qtyInput.setText(product.quantity.toString())
    }

    private fun fetchCategories(category: String? = null) {
        apiService.getCategories("Bearer $token").enqueue(object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                if (response.isSuccessful) {
                    categories = response.body() ?: emptyList()

                    if (categories.isNotEmpty()) {
                        // Populate the AutoCompleteTextView
                        val adapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_dropdown_item_1line,
                            categories
                        )
                        binding.catInput.setAdapter(adapter)

                        var selectedCat: String? = categories[0]
                        category?.let {
                            selectedCat = categories.firstOrNull { it == category }
                        }
                        binding.catInput.setText(
                            selectedCat,
                            false
                        )
                    }

                } else {
                    Toast.makeText(
                        requireContext(),
                        "Failed to fetch categories",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showAddCategoryDialog() {
        // Inflate the custom layout
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_category, null)
        val nameLayout = dialogView.findViewById<TextInputLayout>(R.id.nameLayout)
        val nameInput = dialogView.findViewById<TextInputEditText>(R.id.nameInput)

        // Create the dialog
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add New Category")
            .setView(dialogView)
            .setNegativeButton("CANCEL") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("ADD", null) // Null listener to override behavior
            .create()

        // Set custom behavior for the "Add" button
        dialog.setOnShowListener {
            val addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            addButton.setOnClickListener {
                val categoryName = nameInput.text.toString().trim()

                // Validate input
                if (categoryName.isEmpty()) {
                    nameLayout.error = "Category name cannot be empty"
                } else {
                    nameLayout.error = null // Clear error
                    addCategory(categoryName) // Call the backend
                    dialog.dismiss() // Close the dialog after successful validation
                }
            }
        }

        dialog.show()
    }


    private fun addCategory(categoryName: String) {
        val category = mapOf("name" to categoryName)

        apiService.addCategory("Bearer $token", category).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    val snackbar = Snackbar.make(
                        binding.root,
                        "Category added successfully.",
                        Snackbar.LENGTH_LONG
                    )
                    snackbar.setAction(R.string.dismiss) {
                        snackbar.dismiss();
                    }
                    snackbar.show()

                    // Refresh categories dropdown after successful addition
                    fetchCategories()
                } else if (response.code() == 409) {
                    // Handle duplicate category error
                    val snackbar = Snackbar.make(
                        binding.root,
                        "Category already exists",
                        Snackbar.LENGTH_LONG
                    )
                    snackbar.setAction(R.string.dismiss) {
                        snackbar.dismiss();
                    }
                    snackbar.show()
                } else {
                    Toast.makeText(requireContext(), "Failed to add category", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun validateInputs(): Boolean {
        var isValid = true

        // Validate name
        val name = binding.nameInput.text.toString().trim()
        if (name.isEmpty()) {
            binding.nameLayout.error = "Product name cannot be empty"
            isValid = false
        } else {
            binding.nameLayout.error = null
        }

        // Validate description
        val description = binding.descInput.text.toString().trim()
        if (description.isEmpty()) {
            binding.descLayout.error = "Product description cannot be empty"
            isValid = false
        } else {
            binding.descLayout.error = null
        }

        // Validate price
        val price = binding.priceInput.text.toString().trim()
        if (price.isEmpty() || price.toDoubleOrNull() == null || price.toDouble() <= 0) {
            binding.priceLayout.error = "Enter a valid price"
            isValid = false
        } else {
            binding.priceLayout.error = null
        }

        // Validate original price
        val originalPrice = binding.originalPriceInput.text.toString().trim()
        if (originalPrice.isEmpty() || originalPrice.toDoubleOrNull() == null || originalPrice.toDouble() <= 0) {
            binding.originalPriceLayout.error = "Enter a valid price"
            isValid = false
        } else {
            binding.originalPriceLayout.error = null
        }

        // Validate quantity
        val quantity = binding.qtyInput.text.toString().trim()
        if (quantity.isEmpty() || quantity.toIntOrNull() == null || quantity.toInt() < 0) {
            binding.qtyLayout.error = "Enter a valid quantity"
            isValid = false
        } else {
            binding.qtyLayout.error = null
        }

        return isValid
    }

    private fun saveProduct() {
        val product = Product(
            name = binding.nameInput.text.toString().trim(),
            description = binding.descInput.text.toString().trim(),
            price = binding.priceInput.text.toString().toDouble(),
            originalPrice = binding.originalPriceInput.text.toString().toDouble(),
            category = binding.catInput.text.toString(),
            imageUrl = imageUrl,
            quantity = binding.qtyInput.text.toString().toInt()
        )

        apiService.addProduct("Bearer $token", product).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    val snackbar = Snackbar.make(
                        binding.root,
                        "Product saved successfully",
                        Snackbar.LENGTH_LONG
                    )
                    snackbar.setAction(R.string.dismiss) {
                        snackbar.dismiss();
                    }
                    snackbar.show()

                    clearInputs()

                } else {
                    // Retrieve and parse the error body
                    val errorBody = response.errorBody()?.string()
                    errorBody?.let {
                        try {
                            val jsonObject = JSONObject(it)
                            val errorMessage = jsonObject.getString("msg")

                            val snackbar = Snackbar.make(
                                binding.root,
                                "Failed to save product. $errorMessage",
                                Snackbar.LENGTH_LONG
                            )
                            snackbar.setAction(R.string.dismiss) {
                                snackbar.dismiss();
                            }
                            snackbar.show()
                        } catch (e: Exception) {
                            Toast.makeText(
                                requireContext(),
                                "Product save failed",
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

    private fun updateProduct(product: Product) {
        if (imageUrl == null) {
            imageUrl = product.imageUrl
        }

        val updatedProduct = Product(
            name = binding.nameInput.text.toString().trim(),
            description = binding.descInput.text.toString().trim(),
            price = binding.priceInput.text.toString().toDouble(),
            originalPrice = binding.originalPriceInput.text.toString().toDouble(),
            category = binding.catInput.text.toString(),
            imageUrl = imageUrl,
            quantity = binding.qtyInput.text.toString().toInt(),
            createdAt = product.createdAt
        )

        apiService.updateProduct("Bearer $token", product.id!!, updatedProduct)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        val snackbar = Snackbar.make(
                            binding.root,
                            "Product updated successfully",
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
                                    "Failed to update product. $errorMessage",
                                    Snackbar.LENGTH_LONG
                                )
                                snackbar.setAction(R.string.dismiss) {
                                    snackbar.dismiss();
                                }
                                snackbar.show()
                            } catch (e: Exception) {
                                Toast.makeText(
                                    requireContext(),
                                    "Product update failed",
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


    private fun clearInputs() {
        binding.nameInput.text?.clear()
        binding.descInput.text?.clear()
        binding.priceInput.text?.clear()
        binding.originalPriceInput.text?.clear()
        binding.catInput.setText(categories[0], false)
        binding.ivProductImage.setImageResource(R.drawable.placeholder_image)
        binding.btnUploadImage.isEnabled = false
        binding.qtyInput.text?.clear()
    }

    private fun chooseCameraOrPhotos() {
        val items = arrayOf<CharSequence>("Camera", "Photos")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Complete action using")
            .setItems(items) { _: DialogInterface?, item: Int ->
                if (items[item] == "Camera") {
                    pickFromCamera()
                } else if (items[item] == "Photos") {
                    pickMedia.launch(
                        PickVisualMediaRequest.Builder()
                            .setMediaType(ImageOnly)
                            .build()
                    )
                }
            }.show()
    }

    private var pickMedia: ActivityResultLauncher<PickVisualMediaRequest> =
        registerForActivityResult<PickVisualMediaRequest, Uri>(ActivityResultContracts.PickVisualMedia())
        { uri: Uri? ->
            if (uri != null) {
                binding.ivProductImage.setImageURI(uri)

                binding.btnUploadImage.isEnabled = true
                image = uri

                Log.d("PhotoPicker", "Selected URI: $uri")
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

    private fun pickFromCamera() {
        val values = ContentValues()
        image = requireContext().contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        )

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image)

        camera.launch(cameraIntent)
    }

    private var camera: ActivityResultLauncher<Intent> =
        registerForActivityResult<Intent, ActivityResult>(ActivityResultContracts.StartActivityForResult())
        { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                binding.ivProductImage.setImageURI(image)
                binding.btnUploadImage.isEnabled = true

                Log.d("Camera", "Selected URI: $image")
            } else {
                Log.d("Camera", "No media selected")
            }
        }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}