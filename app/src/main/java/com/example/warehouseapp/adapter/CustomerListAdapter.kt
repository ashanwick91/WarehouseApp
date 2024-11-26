package com.example.warehouseapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.warehouseapp.R
import com.example.warehouseapp.databinding.ItemCustomerBinding
import com.example.warehouseapp.model.User

class CustomerListAdapter : RecyclerView.Adapter<CustomerListAdapter.CustomerViewHolder>() {

    private var customers = listOf<User>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_customer, parent, false)
        return CustomerViewHolder(view)
    }

    override fun getItemCount() = customers.size

    override fun onBindViewHolder(holder: CustomerViewHolder, position: Int) {
        holder.bind(customers[position])
    }

    fun updateCustomerList(newCustomers: List<User>) {
       customers = newCustomers
        notifyDataSetChanged()
    }

    class CustomerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val addressTextView: TextView = itemView.findViewById(R.id.tvAddress)
        private val phoneTextView: TextView = itemView.findViewById(R.id.tvPhoneNumber)
        private val profileImageView: ImageView = itemView.findViewById(R.id.profileImage)
        private val fullNameTextView: TextView = itemView.findViewById(R.id.tvFullName)

        fun bind(user: User) {
            val address = user.profile?.address ?: "N/A"
            val phone = user.profile?.phoneNumber ?: "N/A"
            val firstName = user.profile?.firstName ?: "N/A"
            val lastName = user.profile?.lastName ?: ""
            val fullName = if (lastName.isNotEmpty()) "$firstName $lastName" else firstName

            addressTextView.text = "Address: $address"
            phoneTextView.text = "Phone: $phone"
            fullNameTextView.text = fullName

            // Set a default profile image (optional)
            profileImageView.setImageResource(R.drawable.baseline_account_circle_24)
        }
    }
}
