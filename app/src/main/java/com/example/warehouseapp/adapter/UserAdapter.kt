package com.example.warehouseapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.warehouseapp.databinding.ItemAccessRequestBinding
import com.example.warehouseapp.listener.AccessRequestItemClickListener
import com.example.warehouseapp.model.User
import com.example.warehouseapp.util.DateTimeFormatter

class UserAdapter(
    val accessRequestItemClickListener: AccessRequestItemClickListener
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private var users = listOf<User>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding =
            ItemAccessRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun getItemCount() = users.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    inner class UserViewHolder(private val binding: ItemAccessRequestBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Bind data to the views
        fun bind(user: User) {
            binding.tvEmail.text = String.format("Email: ${user.email}")
            binding.tvRegisteredAt.text = String.format(
                "Registered: ${
                    user.createdAt?.let {
                        DateTimeFormatter.formatOffsetDateTime(it)
                    }
                }"
            )

            if (!user.isApproved) {
                binding.btnApprove.visibility = View.VISIBLE
                binding.btnApprove.setOnClickListener {
                    accessRequestItemClickListener.onApproveClick(user.id)
                }
                binding.tvStatus.text = String.format("Status: Not Approved")
            } else {
                binding.btnApprove.visibility = View.INVISIBLE
                binding.btnApprove.setOnClickListener(null)
                binding.tvStatus.text = String.format("Status: Approved")
            }
        }
    }

    fun updateUserList(users: List<User>) {
        this.users = users
        notifyDataSetChanged()
    }
}