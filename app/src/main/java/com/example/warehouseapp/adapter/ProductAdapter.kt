package com.example.warehouseapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.warehouseapp.OnProductItemClickListener
import com.example.warehouseapp.R
import com.example.warehouseapp.model.Product
import com.squareup.picasso.Picasso

class ProductAdapter(
    private var productList: List<Product>,
    private val listener: OnProductItemClickListener
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>(){

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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.product_item, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val item = productList[position]
        holder.productName.text = item.name
        holder.productPrice.text = item.price.toString()
        holder.productCategory.text = item.category

        Picasso.get()
            .load(item.image)
            .placeholder(R.drawable.sample_shoe_image)
            .into(holder.productImage)

        var quantity = item.cartQuantity
        holder.quantityText.text = quantity.toString()

        holder.plusButton.setOnClickListener {
            quantity++
            holder.quantityText.text = quantity.toString()
            item.cartQuantity = quantity
            listener.onAddToCartClick(item, 1)
        }

        holder.minusButton.setOnClickListener {
            if (quantity > 0) {
                quantity--
                holder.quantityText.text = quantity.toString()
                item.cartQuantity = quantity
                if (quantity == 0) {
                    listener.onAddToCartClick(item, -1)
                } else {
                    listener.onAddToCartClick(item, -1)
                }
            }
        }

    }
    override fun getItemCount() = productList.size

    inner class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productName: TextView = view.findViewById(R.id.textViewProductName)
        val productPrice: TextView = view.findViewById(R.id.textViewPrice)
        val productCategory: TextView = view.findViewById(R.id.textViewCategory)
        val productImage: ImageView = view.findViewById(R.id.imageViewProduct)
        val quantityText: TextView = view.findViewById(R.id.textViewItemCount)
        val plusButton: ImageView = view.findViewById(R.id.imageViewPlus)
        val minusButton: ImageView = view.findViewById(R.id.imageViewMinus)
    }
}