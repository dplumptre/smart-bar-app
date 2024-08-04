package com.example.smartbarapp

import android.R
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartbarapp.data.CartItem
import com.example.smartbarapp.data.PaymentMethodItem
import com.example.smartbarapp.databinding.ActivityOrderBinding
import com.example.smartbarapp.http.HTTPService
import com.example.smartbarapp.lib.Helper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONException
import org.json.JSONObject
import java.net.HttpURLConnection

class OrderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderBinding
    private lateinit var helper: Helper
    private val cartItems: MutableList<CartItem> = mutableListOf()
    private lateinit var cartAdapter: CartAdapter
    private lateinit var httpService: HTTPService
    private var paymentMethodItems: List<PaymentMethodItem> = emptyList()

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
        httpService = HTTPService()
        loadCartItems() // Load cart items when the activity is created

        if (cartItems.isEmpty()) {
            binding.textViewNotice.text = "Your Order list is empty!"
        } else {
            Log.i("Json", cartItems.toString())
            setupRecyclerView() // Setup RecyclerView to display items
            updateTotalPrice() // Update the total price displayed
        }

        fetchPaymentMethods()

        binding.placeOrderButton.setOnClickListener {
            placeOrder()
        }
        binding.buttonContinueShoppingItem.setOnClickListener {
            helper.navigate(this, MenuListActivity::class.java)
        }
    }

    private fun fetchPaymentMethods() {
        httpService.fetchResponse("/payment-methods") { responseCode, response ->
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_ACCEPTED) {
                try {
                    val responseObject = JSONObject(response)
                    val success = responseObject.getBoolean("success")
                    if (success) {
                        val itemsJsonArray = responseObject.getJSONArray("data")
                        val items = ArrayList<PaymentMethodItem>()

                        for (i in 0 until itemsJsonArray.length()) {
                            val itemJsonObject = itemsJsonArray.getJSONObject(i)
                            val itemName = itemJsonObject.getString("name")
                            val itemId = itemJsonObject.getInt("id")
                            items.add(PaymentMethodItem(itemId, itemName))
                        }

                        paymentMethodItems = items // Update paymentMethodItems

                        runOnUiThread {
                            val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, items.map { it.name })
                            adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                            binding.spinner.adapter = adapter

                            binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                                    val selectedItem = items[position]
                                    Toast.makeText(this@OrderActivity, "Selected Payment Method: ${selectedItem.name}", Toast.LENGTH_SHORT).show()
                                }

                                override fun onNothingSelected(parent: AdapterView<*>) {
                                    // Do nothing
                                }
                            }
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Log.e("JSON Parse Error", "Failed to parse response: $response")
                    runOnUiThread {
                        // Update UI to show an error message to the user
                    }
                }
            } else {
                Log.e("HTTP Error Response", "$responseCode  $response")
                Log.e("HTTP Error", "Failed to fetch data: $response")
            }
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
        binding.textViewTotal.text = "Total: " + helper.formatPrice(total) // Format total price
    }

    private fun placeOrder() {
        val selectedPosition = binding.spinner.selectedItemPosition

        if (selectedPosition < 0 || selectedPosition >= paymentMethodItems.size) {
            Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedPaymentMethod = paymentMethodItems[selectedPosition]
        val total = cartItems.sumOf { it.price * it.quantity }

        val orderData = mapOf(
            "menuItemEntities" to cartItems,
            "paymentMethodId" to selectedPaymentMethod.id,
            "totalPrice" to total
        )

        val orderJson = Gson().toJson(orderData)

        httpService.postRequestObject(this, "/orders", orderJson) { response ->
            runOnUiThread {
                if (response.startsWith("Error") || response.startsWith("Exception")) {
                    helper.showToastMessage(this, "Failed to add: $response")
                } else {
                    Log.i("Response: ", response)
                    helper.showToastMessage(this, "Success: $response")

                    // Navigate to MenuListActivity
                    helper.navigate(this, MenuListActivity::class.java)
                }
            }
        }
    }


}
