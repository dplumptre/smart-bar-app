package com.example.smartbarapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartbarapp.adapter.OrderHistoryAdapter
import com.example.smartbarapp.data.OrderHistoryModel
import com.example.smartbarapp.databinding.ActivityOrderHistoryBinding
import com.example.smartbarapp.http.HTTPService
import com.example.smartbarapp.lib.Helper
import org.json.JSONException
import org.json.JSONObject
import java.net.HttpURLConnection

class OrderHistoryActivity : AppCompatActivity() {

    private lateinit var helper: Helper
    private lateinit var binding: ActivityOrderHistoryBinding
    private lateinit var httpService: HTTPService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        helper = Helper()
        httpService = HTTPService()

        binding = ActivityOrderHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        fetchUsersOrders()

        binding.buttonContinueShoppingItem.setOnClickListener {
            helper.navigate(this, MenuListActivity::class.java)
        }
    }

    private fun fetchUsersOrders() {
        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val userId = prefs.getString("userId", null)

        if (userId == null) {
            Log.e("UserDetails", "User ID not found")
            return
        }

        Log.d("UserDetails", "Fetching orders for User ID: $userId")

        httpService.fetchResponseWithToken(this, "/orders/user/$userId") { responseCode, response ->
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_ACCEPTED) {
                try {
                    val responseObject = JSONObject(response)
                    if (responseObject.getBoolean("success")) {
                        val itemsJsonArray = responseObject.getJSONArray("data")
                        val items = List(itemsJsonArray.length()) { index ->
                            val itemJsonObject = itemsJsonArray.getJSONObject(index)
                            OrderHistoryModel(
                                id = itemJsonObject.getString("id"),
                                myDate = itemJsonObject.getString("createdAt"),
                                reference = itemJsonObject.getString("orderReference"),
                                total = helper.formatPrice(itemJsonObject.getString("totalPrice").toDouble()),
                                status = itemJsonObject.getString("status")
                            )
                        }
                        runOnUiThread { setupRecyclerView(items.toMutableList()) }
                    }
                } catch (e: JSONException) {
                    Log.e("JSON Parse Error", "Failed to parse response: $response", e)
                }
            } else {
                Log.e("HTTP Error", "Failed to fetch data: $response")
            }
        }
    }

    private fun setupRecyclerView(items: MutableList<OrderHistoryModel>) {
        val adapter = OrderHistoryAdapter(items) { orderId, position ->
            onMarkAsCompleted(orderId, position)
        }
        binding.orderHistoryRecyclerView.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(this@OrderHistoryActivity)
        }
    }

    private fun onMarkAsCompleted(orderId: String, position: Int) {
        val alertDialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Confirm Action")
            .setMessage("Are you sure you want to mark this order as completed?")
            .setPositiveButton("Yes") { _, _ ->
                markOrderAsCompleted(orderId, position)
            }
            .setNegativeButton("Cancel", null)
            .create()

        alertDialog.show()
    }

    private fun markOrderAsCompleted(orderId: String, position: Int) {


        val requestBody = """{"status":"COMPLETED"}""" // A proper JSON body

        httpService.putRequestObject(this, "/orders/$orderId", requestBody) { responseCodeString, response ->
            try {
                val responseCode = responseCodeString.toInt() // Parse the response code to an integer
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_ACCEPTED) {
                    Log.d("OrderHistory", "Order marked as completed: $orderId")
                    runOnUiThread { updateOrderStatus(position, "Completed") }
                } else {
                    Log.e("HTTP Error", "Failed to mark order as completed: $response")
                }
            } catch (e: NumberFormatException) {
                Log.e("HTTP Error", "Invalid response code format: $responseCodeString", e)
            }
        }

    }

    private fun updateOrderStatus(position: Int, newStatus: String) {
        val adapter = binding.orderHistoryRecyclerView.adapter as? OrderHistoryAdapter
        adapter?.updateOrderStatus(position, newStatus)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
