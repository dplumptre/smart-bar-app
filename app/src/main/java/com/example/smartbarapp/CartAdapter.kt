package com.example.smartbarapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartbarapp.data.CartItem
import com.shawnlin.numberpicker.NumberPicker

class CartAdapter(
    private val cartItems: MutableList<CartItem>,
    private val onDelete: (CartItem) -> Unit,
    private val onQuantityChanged: () -> Unit // Callback for quantity change
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.textViewTitle)
        val price: TextView = itemView.findViewById(R.id.textViewPrice)
        val quantityPicker: NumberPicker = itemView.findViewById(R.id.numberPickerQuantity)
        val deleteButton: Button = itemView.findViewById(R.id.buttonDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cart_row, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = cartItems[position]
        holder.title.text = item.title
        holder.price.text = item.price.toString()
        holder.quantityPicker.value = item.quantity

        holder.quantityPicker.setOnValueChangedListener { _, _, newVal ->
            item.quantity = newVal
            onQuantityChanged() // Notify the activity of quantity change
        }

        holder.deleteButton.setOnClickListener {
            onDelete(item)
            // Notify that an item has been deleted
        }
    }

    override fun getItemCount() = cartItems.size
}
