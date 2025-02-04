package com.example.smartbarapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartbarapp.data.ItemDataViewModel
import com.example.smartbarapp.databinding.ActivityMenuListBinding
import com.example.smartbarapp.http.HTTPService
import com.example.smartbarapp.lib.Helper
import org.json.JSONException
import org.json.JSONObject
import java.net.HttpURLConnection

class MenuListActivity : AppCompatActivity(), CustomAdapter.OnItemClickListener {

    private lateinit var binding: ActivityMenuListBinding
    private lateinit var helper: Helper
    private lateinit var httpService: HTTPService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        // removing back button
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        supportActionBar?.setDisplayShowHomeEnabled(true)

        helper = Helper()
        httpService = HTTPService()

        httpService.fetchResponse("/menu-items") { responseCode, response ->
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_ACCEPTED) {
                try {
                    val responseObject = JSONObject(response)
                    val success = responseObject.getBoolean("success")
                    if (success) {
                        val itemsJsonArray = responseObject.getJSONArray("data")
                        val items = mutableListOf<ItemDataViewModel>()
                        for (i in 0 until itemsJsonArray.length()) {
                            val itemJsonObject = itemsJsonArray.getJSONObject(i)
                            val id = itemJsonObject.getString("id")
                            val name = itemJsonObject.getString("name")
//                            val price = itemJsonObject.getString("price")
                            val price =  helper.formatPrice(itemJsonObject.getString("price").toDouble())
                            val description = itemJsonObject.getString("description")
                            val image = itemJsonObject.getString("image")
                            val menuItem = ItemDataViewModel(id, name, price, description, image)
                            items.add(menuItem)
                        }
                        runOnUiThread {
                            setupRecyclerView(items)
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
    }

    private fun setupRecyclerView(items: List<ItemDataViewModel>) {
        val recyclerview = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerview.layoutManager = LinearLayoutManager(this)
        val adapter = CustomAdapter(items, this) // Pass 'this' as the OnItemClickListener
        recyclerview.adapter = adapter
    }

    override fun onItemClick(item: ItemDataViewModel) {
        sendItemRequest(item.id)
    }

    private fun sendItemRequest(itemId: String) {
        runOnUiThread {
                helper.navigateWithPayload( this, MenuItemActivity::class.java, itemId)
            }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }




    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // This ensures the back button works
                onBackPressed()
                true
            }
            R.id.action_order_history -> {
                // Navigate to the Order History page
                startActivity(Intent(this, OrderHistoryActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }




}
