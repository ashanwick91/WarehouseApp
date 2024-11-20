package com.example.warehouseapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.warehouseapp.R
import com.example.warehouseapp.customer.CustomerHistoryFragment
import com.example.warehouseapp.model.Order
import com.example.warehouseapp.model.OrderDetails
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class CustomerHistroyAdapter(

    private var orderList: List<OrderDetails>,
    private val listener: CustomerHistoryFragment
) : RecyclerView.Adapter<CustomerHistroyAdapter.CustomerHistroyViewHolder>(){

    fun updateOrderList(newOrder: List<OrderDetails>) {
        orderList = newOrder
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerHistroyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_customer_order_history, parent, false)
        return CustomerHistroyViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomerHistroyViewHolder, position: Int) {
        val item = orderList[position]
        // Parse the date if it's a string
        val offsetDateTime = OffsetDateTime.parse(item.orderDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val date = Date.from(offsetDateTime.toInstant())

        // Format the date
        val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val formattedDate = dateFormatter.format(date)

        holder.orderDate.text = formattedDate

        holder.orderStatus.text = item.status
        holder.noItems.text = item.items.size.toString()
        holder.paymentAmount.text = "$" + item.orderTotal.toString()
        // Handle item click
        holder.itemView.setOnClickListener {
            listener.onViewOrderDetails(item) // Call the listener with the selected item
        }




    }
    override fun getItemCount() = orderList.size

    inner class CustomerHistroyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val orderDate: TextView = view.findViewById(R.id.orderDate)
        val orderStatus: TextView = view.findViewById(R.id.orderStatus)
        val noItems: TextView = view.findViewById(R.id.noItems)
        val paymentAmount: TextView = view.findViewById(R.id.paymentAmount)
    }
}