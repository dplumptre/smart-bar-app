package com.example.smartbarapp

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.AppBarConfiguration
import com.example.smartbarapp.databinding.ActivityMenuListBinding
import com.example.smartbarapp.http.HTTPService
import com.example.smartbarapp.lib.Helper
import org.json.JSONException
import org.json.JSONObject
import java.net.HttpURLConnection


class MenuListActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMenuListBinding
    private lateinit var helper: Helper
    private lateinit var httpService: HTTPService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMenuListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Enable back button on the toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)


        helper = Helper()
        httpService = HTTPService()

//        val name = helper.getName(this);
//
//        if (name != null) {
//            Log.i("name: ", name)
//        };

        httpService.fetchResponse("/menu-items") { responseCode, response ->
            processMenuItemsResponse(responseCode, response)
        }


        // Set OnClickListener for the button using View Binding
        binding.menuItemButton.setOnClickListener {
            helper.navigate(this, MenuItemActivity::class.java)
        }
    }


    data class MenuItem(val id: String, val name: String, val price: String, val image: String)

    fun processMenuItemsResponse(responseCode: Int, response: String) {
        if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_ACCEPTED) {
            try {
                val responseObject = JSONObject(response)
                val success = responseObject.getBoolean("success")
                if (success) {
                    val itemsJsonArray = responseObject.getJSONArray("data")
                    val items = mutableListOf<MenuItem>()
                    for (i in 0 until itemsJsonArray.length()) {
                        val itemJsonObject = itemsJsonArray.getJSONObject(i)
                        val id = itemJsonObject.getString("id")
                        val name = itemJsonObject.getString("name")
                        val price = itemJsonObject.getString("price")
                        val image = itemJsonObject.getString("image")
                        val menuItem = MenuItem(id, name, price, image)
                        items.add(menuItem)
                    }

                    Log.i("menu-item", items.toString())

                    runOnUiThread {
                        // Update UI here with items
                    }
                } else {
                    Log.e("Server Error", "Response indicates failure: $response")
                    runOnUiThread {
                        // Update UI to show an error message to the user
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
            // Handle different response codes and log appropriately
            Log.e("Server Error", "Error: $responseCode - $response")
            runOnUiThread {
                // Update UI to show an error message to the user
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            android.R.id.home -> {
//                onBackPressed()
//                true
//            }
//            R.id.action_settings -> true
//            else -> super.onOptionsItemSelected(item)
//        }
//    }
}
