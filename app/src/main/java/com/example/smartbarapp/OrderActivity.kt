package com.example.smartbarapp

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartbarapp.data.CartItem
import com.example.smartbarapp.databinding.ActivityOrderBinding
import com.example.smartbarapp.lib.Helper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class OrderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderBinding
    private lateinit var helper: Helper
    private val cartItems: MutableList<CartItem> = mutableListOf()
    private lateinit var cartAdapter: CartAdapter

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Enable back button on the toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        helper = Helper()

        loadCartItems() // Load cart items when the activity is created

        if (cartItems.isEmpty()) {
            binding.textViewNotice.text = "Your Order list is empty!"
        } else {
            Log.i("Json", cartItems.toString())
            setupRecyclerView() // Setup RecyclerView to display items
            updateTotalPrice() // Update the total price displayed
        }

        binding.menuItemButton.setOnClickListener {
            helper.navigate(this, MenuItemActivity::class.java)
        }
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(cartItems, { item ->
            removeCartItem(item) // Handle item deletion
        }, {
            saveCartItems() // Save updated cart items when quantity changes
            updateTotalPrice() // Update total price displayed
            Toast.makeText(this, "Quantity updated!", Toast.LENGTH_SHORT).show()
        })

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@OrderActivity)
            adapter = cartAdapter
        }
    }

    private fun loadCartItems() {
        val sharedPref = getSharedPreferences("cart_data", Context.MODE_PRIVATE)
        val cartItemsJson = sharedPref.getString("cartItems", null)

        if (!cartItemsJson.isNullOrEmpty()) {
            val gson = Gson()
            val type = object : TypeToken<MutableList<CartItem>>() {}.type
            val items: MutableList<CartItem> = gson.fromJson(cartItemsJson, type)
            cartItems.clear()
            cartItems.addAll(items) // Load updated cart items
        }
    }

    private fun removeCartItem(item: CartItem) {
        val position = cartItems.indexOf(item)
        if (position != -1) {
            cartItems.removeAt(position)
            cartAdapter.notifyItemRemoved(position) // Notify adapter of item removal
            saveCartItems() // Save updated cart items
            updateTotalPrice() // Update total price
            Toast.makeText(this, "Item removed!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveCartItems() {
        val gson = Gson()
        val cartItemsJson = gson.toJson(cartItems)
        val sharedPref = getSharedPreferences("cart_data", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("cartItems", cartItemsJson)
            apply()
        }
    }

    private fun updateTotalPrice() {
        val total = cartItems.sumOf { it.price * it.quantity }
        binding.textViewTotal.text = "Total: "+ helper.formatPrice(total) // Format total price
    }
}
