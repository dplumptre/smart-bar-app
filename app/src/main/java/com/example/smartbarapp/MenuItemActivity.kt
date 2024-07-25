package com.example.smartbarapp

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.smartbarapp.databinding.ActivityMenuItemBinding
import com.example.smartbarapp.http.HTTPService
import com.example.smartbarapp.lib.Helper
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
        if (id != null) {
            Log.d("MenuItemActivity", "Received ID: $id")

            httpService.fetchResponse("/menu-items/$id") { responseCode, response ->
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_ACCEPTED) {
                try {
                    val responseObject = JSONObject(response)
                    val success = responseObject.getBoolean("success")
                    if (success) {
                        val itemJSONObject = responseObject.getJSONObject("data")
                        val name = itemJSONObject.getString("name")
                        val price = itemJSONObject.getString("price")
                        val description = itemJSONObject.getString("description")
                        val image = itemJSONObject.getString("image")
                        runOnUiThread {


                            binding.textViewMenuItem.text = name
                            binding.textViewPrice.text = price
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
        } else {
            Log.e("MenuItemActivity", "No ID received")
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