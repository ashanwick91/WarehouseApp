package com.example.warehouseapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.warehouseapp.R
import com.example.warehouseapp.databinding.ItemProductAdminBinding
import com.example.warehouseapp.listener.AdminProductItemClickListener
import com.example.warehouseapp.model.Product
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class AdminProductAdapter(
    val adminProductItemClickListener: AdminProductItemClickListener
) : RecyclerView.Adapter<AdminProductAdapter.ProductViewHolder>() {

    private var products = listOf<Product>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding =
            ItemProductAdminBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun getItemCount() = products.size

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    inner class ProductViewHolder(private val binding: ItemProductAdminBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Bind data to the views
        fun bind(item: Product) {
            binding.tvPriceAdmin.text = String.format("$${item.price}")
            binding.tvNameAdmin.text = item.name
            binding.tvQtyAdmin.text = String.format("Available Qty: ${item.quantity}")
            binding.tvDescriptionAdmin.text = String.format("Description: ${item.description}")
            binding.tvCategoryAdmin.text = String.format("Category: ${item.category}")

            binding.btnDelAdmin.setOnClickListener {
                adminProductItemClickListener.onItemCLick(item.id!!)
            }

            item.imageUrl?.let { loadImageFromFirebase(it, binding.ivProductAdmin) }
        }
    }

    fun updateProductList(products: List<Product>) {
        this.products = products
        notifyDataSetChanged()
    }

    fun loadImageFromFirebase(storagePath: String, imageView: ImageView) {
        // Reference to the Firebase Storage location
        val storageRef = FirebaseStorage.getInstance().reference.child(storagePath)

        // Get the download URL
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            // Load the image using Picasso
            Picasso.get()
                .load(uri)
                .placeholder(R.drawable.loading)
                .error(R.drawable.error)
                .into(imageView);

        }.addOnFailureListener {
            // Handle any errors
            imageView.setImageResource(R.drawable.error) // Optional: error image
        }
    }
}