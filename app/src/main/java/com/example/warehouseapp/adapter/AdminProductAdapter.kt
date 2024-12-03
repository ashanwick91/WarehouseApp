package com.example.warehouseapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.warehouseapp.databinding.ItemProductAdminBinding
import com.example.warehouseapp.listener.AdminProductItemClickListener
import com.example.warehouseapp.model.Product
import com.example.warehouseapp.util.loadImageFromFirebase

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
            binding.tvOriginalPriceAdmin.text = String.format("Original Price: $${item.originalPrice}")
            binding.tvDescriptionAdmin.text = String.format("Description: ${item.description}")
            binding.tvCategoryAdmin.text = String.format("Category: ${item.category}")

            binding.btnDelAdmin.setOnClickListener {
                adminProductItemClickListener.onDeleteClick(item.id!!)
            }

            binding.btnEditAdmin.setOnClickListener {
                adminProductItemClickListener.onEditClick(item)
            }

            item.imageUrl?.let { loadImageFromFirebase(it, binding.ivProductAdmin) }
        }
    }

    fun updateProductList(products: List<Product>) {
        this.products = products
        notifyDataSetChanged()
    }
}