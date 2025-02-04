package com.example.smartbarapp.adapter

import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartbarapp.R
import com.example.smartbarapp.data.OrderHistoryModel




class OrderHistoryAdapter(
    private val orders: MutableList<OrderHistoryModel>, // Changed to MutableList
    private val onMarkAsCompleted: (String, Int) -> Unit // Pass position as an argument
) : RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder>() {

    // ViewHolder to hold the layout of each row
    class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateView: TextView = view.findViewById(R.id.dateView)
        val totalView: TextView = view.findViewById(R.id.totalView)
        val refView: TextView = view.findViewById(R.id.refView)
        val statusView: TextView = view.findViewById(R.id.statusView)
        val markAsCompletedButton: Button = view.findViewById(R.id.markAsCompletedButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.order_history_recycler_view_row, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]

        // Set text
        holder.dateView.text = order.myDate
        holder.totalView.text = order.total
        holder.refView.text = order.reference
        holder.statusView.text = order.status

        // Set bold for total and reference
        holder.totalView.setTypeface(null, Typeface.BOLD)
        holder.refView.setTypeface(null, Typeface.BOLD)

        // Handle "Mark as Completed" button click
        holder.markAsCompletedButton.setOnClickListener {
            onMarkAsCompleted(order.id, position) // Pass position to update later
        }

        // Update the button state based on order status
        updateButtonState(holder, order)
    }

    // Method to update the button based on order status
    private fun updateButtonState(holder: OrderViewHolder, order: OrderHistoryModel) {
        if (order.status == "COMPLETED") {
            holder.markAsCompletedButton.isEnabled = false
            holder.markAsCompletedButton.setBackgroundColor(Color.GRAY) // Disabled color
            holder.markAsCompletedButton.text = "Completed" // Button text for completed orders
        } else {
            holder.markAsCompletedButton.isEnabled = true
            holder.markAsCompletedButton.setBackgroundColor(Color.BLUE) // Enabled color
            holder.markAsCompletedButton.text = "Mark as Completed" // Default button text
        }
    }

    override fun getItemCount(): Int = orders.size

    // Add a method to update the status of an order and notify the adapter
    fun updateOrderStatus(position: Int, newStatus: String) {
        if (position in orders.indices) {
            orders[position].status = newStatus // Update the status in the data list
            notifyItemChanged(position) // Notify RecyclerView to refresh the specific item
        }
    }
}
