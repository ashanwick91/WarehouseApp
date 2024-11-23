package com.example.warehouseapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.warehouseapp.OnProductItemClickListener
import com.example.warehouseapp.R
import com.example.warehouseapp.databinding.ItemProductAdminBinding
import com.example.warehouseapp.databinding.ProductItemBinding
import com.example.warehouseapp.model.Product
import com.example.warehouseapp.util.loadImageFromFirebase
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso

class ProductAdapter(
    private var productList: List<Product>,
    private val listener: OnProductItemClickListener
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>(){

    val currentList: List<Product>
        get() = productList

    fun updateProductList(newProduct: List<Product>) {
        productList = newProduct
        notifyDataSetChanged()
    }

    fun updateItemQuantity(productId: String, newQuantity: Int) {
        val position = productList.indexOfFirst { it.id == productId }
        if (position != -1) {
            productList[position].quantity = newQuantity
            notifyItemChanged(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding =
            ProductItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val item = productList[position]
        holder.productName.text = item.name
        holder.productPrice.text =String.format("Price: ${item.price}")
        holder.productCategory.text = String.format("Category: ${item.category}")
        holder.productDescripion.text = String.format("Description: ${item.description}")

        Picasso.get()
            .load(item.imageUrl)
            .placeholder(R.drawable.loading)
            .into(holder.productImage)

        var quantity = item.cartQuantity


        holder.plusButton.setOnClickListener {
            quantity++
            holder.quantityText.text = quantity.toString()
            item.cartQuantity = quantity
            listener.onAddToCartClick(item, 1) // Update quantity and save to SharedPreferences
        }

        holder.minusButton.setOnClickListener {
            if (quantity > 1) {
                quantity--
                holder.quantityText.text = quantity.toString()
                item.cartQuantity = quantity
                listener.onAddToCartClick(item, -1) // Update quantity and save to SharedPreferences
            } else if (quantity == 1) {
                quantity = 0
                holder.quantityText.text = quantity.toString()
                item.cartQuantity = quantity
                listener.onRemoveFromCartClick(item.id!!)
            }
        }
        item.imageUrl?.let { loadImageFromFirebase(it, holder.productImage) }
        holder.quantityText.text = quantity.toString()

    }
    override fun getItemCount() = productList.size

    inner class ProductViewHolder(private val binding: ProductItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val productName: TextView = binding.textViewProductName
        val productPrice: TextView = binding.textViewPrice
        val productCategory: TextView = binding.textViewCategory
        val productImage: ImageView = binding.imageViewProduct
        val quantityText: TextView = binding.textViewItemCount
        val plusButton: ImageView = binding.imageViewPlus
        val minusButton: ImageView = binding.imageViewMinus
        var productDescripion: TextView = binding.descriptionCustomer

    }

}