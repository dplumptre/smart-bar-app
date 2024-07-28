package com.example.smartbarapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartbarapp.data.ItemDataViewModel
import com.squareup.picasso.Picasso

class CustomAdapter(
    private val mList: List<ItemDataViewModel>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    // Define an interface for item click events
    interface OnItemClickListener {
        fun onItemClick(item: ItemDataViewModel)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.item_title)
        val textViewDescription: TextView = itemView.findViewById(R.id.item_description)
        val textViewPrice: TextView = itemView.findViewById(R.id.item_price)
        val imageView: ImageView = itemView.findViewById(R.id.item_image)

        // Bind the click listener to the view
        fun bind(item: ItemDataViewModel, listener: OnItemClickListener) {
            itemView.setOnClickListener {
                listener.onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemViewModel = mList[position]
        holder.textView.text = itemViewModel.name
        holder.textViewDescription.text = itemViewModel.description
        holder.textViewPrice.text = itemViewModel.price
        Picasso.get().load(itemViewModel.image).into(holder.imageView)
        holder.bind(itemViewModel, listener)
    }

    override fun getItemCount(): Int {
        return mList.size
    }
}

