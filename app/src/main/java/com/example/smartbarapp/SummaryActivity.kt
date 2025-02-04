package com.example.smartbarapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.smartbarapp.databinding.ActivitySummaryBinding
import com.example.smartbarapp.http.HTTPService
import com.example.smartbarapp.lib.Helper
import org.json.JSONException
import org.json.JSONObject

class SummaryActivity : AppCompatActivity() {



        private lateinit var binding: ActivitySummaryBinding
        private lateinit var helper: Helper
        private lateinit var httpService: HTTPService

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = ActivitySummaryBinding.inflate(layoutInflater)
            setContentView(binding.root)

            setSupportActionBar(binding.toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)

            helper = Helper()
            httpService = HTTPService()

            val id = intent.getStringExtra("EXTRA_ID")
            if (id == null) {
                Log.e("Summary", "No ID received")
                helper.showToastMessage(this, "Item ID is required.")
                finish() // Closes the current activity
                return //
            }

            binding.textSuccessMessage.text = "Your Order has been place. kindly wait a few minutes to receive your order";

            binding.buttonOrderHistory.setOnClickListener {
                helper.navigate(this,OrderHistoryActivity::class.java)
            }

            binding.buttonContinueShoppingItem.setOnClickListener {
                helper.navigate(this, MenuListActivity::class.java)
            }



            httpService.fetchOrderDetails(this, id.toInt()) { response ->
                if (response.startsWith("Error:")) {
                    Log.e("JSON Parse Error", "Failed to fetch data: $response")
                } else {
                    try {
                        val responseObject = JSONObject(response)
                        val success = responseObject.getBoolean("success")
                        if (success) {
                            val itemJSONObject = responseObject.getJSONObject("data")
                            val price = itemJSONObject.getString("totalPrice").toDoubleOrNull() ?: 0.0
                            val paymentMethod = itemJSONObject.getJSONObject("paymentMethod") // Fetching the paymentMethod as a JSONObject
                            val orderReference = itemJSONObject.getString("orderReference")

                            // Log for debugging
                            Log.i("price", price.toString())

                            val formattedPrice = helper.formatPrice(price)

                            // Extract the name from the paymentMethod object
                            val paymentMethodName = paymentMethod.getString("name")

                            // Update UI on the main thread
                            runOnUiThread {
                                binding.NameTitle.text = helper.getName(this)
                                binding.OrderNumberTitle.text = "Order Details"
                                binding.OrderNumber.text = orderReference
                                binding.TotalPrice.text = formattedPrice
                                binding.PaymentMethod.text = paymentMethodName
                            }
                        } else {
                            // Handle case where the success flag is false
                            Log.e("Order Fetch Error", "Order fetch was not successful.")
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Log.e("JSON Parse Error", "Failed to parse response: $response")
                        runOnUiThread {
                            // Update UI to show an error message to the user
                        }
                    }
                }
            }



        }
}