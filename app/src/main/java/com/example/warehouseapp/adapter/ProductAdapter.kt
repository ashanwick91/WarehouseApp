package com.example.warehouseapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.warehouseapp.OnProductItemClickListener
import com.example.warehouseapp.R
import com.example.warehouseapp.databinding.ProductItemBinding
import com.example.warehouseapp.model.ItemDetails
import com.example.warehouseapp.model.OrderItemRequest
import com.example.warehouseapp.model.Product
import com.example.warehouseapp.util.CartPreferences
import com.example.warehouseapp.util.loadImageFromFirebase
import com.squareup.picasso.Picasso
import java.util.Date

class ProductAdapter(
    private var productList: List<Product>,
    private val listener: OnProductItemClickListener,
    private val context: Context
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
            item.cartQuantity++
            holder.quantityText.text = item.cartQuantity.toString()
            updateCart(item)
        }

        holder.minusButton.setOnClickListener {
            if (item.cartQuantity > 0) {
                item.cartQuantity--
                holder.quantityText.text = item.cartQuantity.toString()
                updateCart(item)
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

    private fun updateCart(product: Product) {
        val cartItems = CartPreferences.getCart(context).toMutableList()
        val index = cartItems.indexOfFirst { it.productId == product.id }

        if (index != -1) {
            if (product.cartQuantity > 0) {
                cartItems[index].quantity = product.cartQuantity
            } else {
                cartItems.removeAt(index)
            }
        } else {
            if (product.cartQuantity > 0) {
                cartItems.add(
                    ItemDetails(
                        productId = product.id.toString(),
                        productName = product.name,
                        category = product.category,
                        salesAmount = 0.0,
                        quantitySold = 0, // Adjust this if you track sold quantities
                        transactionDate = Date().toString(),
                        price = product.price,
                        quantity = product.cartQuantity,
                    )
                )
            }
        }

        CartPreferences.saveCart(context, cartItems)
    }

}
