package com.example.warehouseapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.warehouseapp.R
import com.example.warehouseapp.model.SalesDataProduct

class SalesDataAdapter(private var salesDataList: List<SalesDataProduct>) :
    RecyclerView.Adapter<SalesDataAdapter.SalesDataViewHolder>() {

    inner class SalesDataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productName: TextView = itemView.findViewById(R.id.productName)
        val salesPercentage: TextView = itemView.findViewById(R.id.salesPercentageProduct)
        val profitAmount: TextView = itemView.findViewById(R.id.profitProduct)
    }

    // Function to update data and refresh the RecyclerView
    fun updateData(newData: List<SalesDataProduct>) {
        salesDataList = newData
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SalesDataViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sales_data, parent, false)
        return SalesDataViewHolder(view)
    }

    override fun onBindViewHolder(holder: SalesDataViewHolder, position: Int) {
        val salesData = salesDataList[position]
        val formattedSales = String.format("%.2f", salesData.totalSalesProduct)
        val formattedProfit = String.format("%.2f", salesData.totalProfitProduct)
        holder.productName.text = salesData.product
        holder.salesPercentage.text = "$ $formattedSales"
        holder.profitAmount.text = "$ $formattedProfit"
    }

    override fun getItemCount(): Int = salesDataList.size
}