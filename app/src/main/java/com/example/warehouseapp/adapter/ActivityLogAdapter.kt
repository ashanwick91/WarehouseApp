package com.example.warehouseapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.warehouseapp.R
import com.example.warehouseapp.model.ActivityLog

class ActivityLogAdapter : RecyclerView.Adapter<ActivityLogAdapter.ActivityLogViewHolder>() {

    private var activityLogs = listOf<ActivityLog>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityLogViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_activity_log, parent, false)
        return ActivityLogViewHolder(view)
    }

    override fun getItemCount() = activityLogs.size

    override fun onBindViewHolder(holder: ActivityLogViewHolder, position: Int) {
        holder.bind(activityLogs[position])
    }

    fun updateLogs(logs: List<ActivityLog>) {
        activityLogs = logs
        notifyDataSetChanged()
    }

    class ActivityLogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val actionTextView: TextView = itemView.findViewById(R.id.tvAction)
        private val detailsTextView: TextView = itemView.findViewById(R.id.tvDetails)
        private val timestampTextView: TextView = itemView.findViewById(R.id.tvTimestamp)

        fun bind(log: ActivityLog) {
            actionTextView.text = "Action: ${log.action}"
            detailsTextView.text = "Details: ${log.details}"
            timestampTextView.text = "Time: ${log.timestamp}"
        }
    }
}
