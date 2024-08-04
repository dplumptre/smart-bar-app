package com.example.smartbarapp

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.smartbarapp.data.CartItem
import com.example.smartbarapp.databinding.ActivityMenuItemBinding
import com.example.smartbarapp.http.HTTPService
import com.example.smartbarapp.lib.Helper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONException
import org.json.JSONObject
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class MenuItemActivity : AppCompatActivity() {

    private lateinit var helper: Helper
    private lateinit var binding: ActivityMenuItemBinding
    private lateinit var httpService: HTTPService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMenuItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Enable back button on the toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        helper = Helper()
        httpService = HTTPService()

        val id = intent.getStringExtra("EXTRA_ID")
        if (id == null) {
            Log.e("MenuItemActivity", "No ID received")
            helper.showToastMessage(this, "Item ID is required.")
            finish() // Closes the current activity
            return //
        }


            Log.d("MenuItemActivity", "Received ID: $id")

            httpService.fetchResponse("/menu-items/$id") { responseCode, response ->
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_ACCEPTED) {
                    try {
                        val responseObject = JSONObject(response)
                        val success = responseObject.getBoolean("success")
                        if (success) {
                            val itemJSONObject = responseObject.getJSONObject("data")
                            val name = itemJSONObject.getString("name")
                            val price = itemJSONObject.getString("price").toDoubleOrNull() ?: 0.0
                            val description = itemJSONObject.getString("description")
                            val image = itemJSONObject.getString("image")
                            runOnUiThread {
                                binding.textViewMenuItem.text = name
                                binding.textViewPrice.text = helper.formatPrice(price)
                                binding.textViewDescription.text = description
                                loadImage(image, binding.imageView)
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
                    Log.e("HTTP Error", "Failed to fetch data: $response")
                }
            }


        binding.buttonOrderItem.setOnClickListener {
            val name = binding.textViewMenuItem.text.toString()

            // Ensure that the price is extracted correctly
            // Get the raw price string from the text view
            val priceString = binding.textViewPrice.text.toString().replace("₦", "").replace(",", "")
            val price = priceString.toDoubleOrNull() ?: 0.0 // Convert to Double, default to 0.0 if null

            // Get the quantity from the NumberPicker
            val quantity = binding.numberPicker.value

            // Get the image URL from the ImageView
            val imageUrl = getImageUrlFromImageView(binding.imageView)

            // Create a new CartItem with the correct values
            val newItem = CartItem(id ?: "", name, price, quantity, imageUrl)
            // Log the price to verify it's correct
            Log.d("MenuItemActivity", "Price: $price, Quantity: $quantity")

            // Retrieve existing cart items from SharedPreferences
            val existingCartItems = getCartItems()

            // Check for duplicates and add or update the item
            val existingItem = existingCartItems.find { it.title == newItem.title && it.price == newItem.price }
            if (existingItem == null) {
                existingCartItems.add(newItem)
            } else {
                existingItem.quantity += newItem.quantity // Update the quantity
            }

            // Save updated cart items back to SharedPreferences
            saveCartItems(existingCartItems)

            // Show a confirmation message to the user
            helper.showToastMessage(this, "$name Order has been placed!")
        }



        // Set OnClickListener for the button using View Binding
        binding.buttonContinueShoppingItem.setOnClickListener {
            helper.navigate(this, MenuListActivity::class.java)
        }

        binding.viewOrderButton.setOnClickListener {
            helper.navigate(this, OrderActivity::class.java)
        }
    }

    private fun loadImage(imageUrl: String, imageView: ImageView) {
        imageView.tag = imageUrl // Set the URL as the tag of the ImageView
        thread {
            try {
                val url = URL(imageUrl)
                val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val input: InputStream = connection.inputStream
                val drawable = Drawable.createFromStream(input, "src")
                runOnUiThread {
                    imageView.setImageDrawable(drawable)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getImageUrlFromImageView(imageView: ImageView): String {
        return imageView.tag as? String ?: "default_image_url"
    }

    private fun saveCartItems(cartItems: MutableList<CartItem>) {
        val sharedPref = getSharedPreferences("cart_data", Context.MODE_PRIVATE)
        val gson = Gson()
        val cartItemsJson = gson.toJson(cartItems)
        with(sharedPref.edit()) {
            putString("cartItems", cartItemsJson)
            apply()
        }
    }

    private fun getCartItems(): MutableList<CartItem> {
        val sharedPref = getSharedPreferences("cart_data", Context.MODE_PRIVATE)
        val cartItemsJson = sharedPref.getString("cartItems", null)
        return if (cartItemsJson != null) {
            val gson = Gson()
            val type = object : TypeToken<MutableList<CartItem>>() {}.type
            gson.fromJson(cartItemsJson, type)
        } else {
            mutableListOf()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
