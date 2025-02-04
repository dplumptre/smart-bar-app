package com.example.smartbarapp


import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.AppBarConfiguration
import com.example.smartbarapp.databinding.ActivityMainBinding
import com.example.smartbarapp.http.HTTPService
import com.example.smartbarapp.lib.Helper
import org.json.JSONException
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var helper: Helper
    private lateinit var httpService: HTTPService;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        helper = Helper()
        httpService = HTTPService();
        binding = ActivityMainBinding.inflate(layoutInflater)
       setContentView(binding.root)
//        setSupportActionBar(binding.toolbar)
        clearCartItems() // clear cart
//        val navController = findNavController(R.id.nav_host_fragment_content_main)
//        appBarConfiguration = AppBarConfiguration(navController.graph)
//        setupActionBarWithNavController(navController, appBarConfiguration)
        // 08186810722


        val continueButton = findViewById<Button>(R.id.buttonSignUp)
        val name = findViewById<EditText>(R.id.name)
        val phoneNumber = findViewById<EditText>(R.id.phoneNumber)

        continueButton.setOnClickListener {
            val nameStr = name.text.toString()
            val phoneNumberStr = phoneNumber.text.toString()

            httpService.postRequest(this, "/auth/customer-login-or-register", nameStr, phoneNumberStr) { response ->
                runOnUiThread {
                    if (response.startsWith("Error")) {
                        // Extract error message and show it
                        val errorMessage = response.removePrefix("Error: ").trim()
                        helper.showToastMessage(this, errorMessage)
                    } else {
                        try {
                            // Parse success response
                            val jsonResponse = JSONObject(response)
                            if (jsonResponse.has("data")) {
                                // Navigate to the next screen on success
                                helper.navigate(this, MenuListActivity::class.java)
                            } else {
                                // Handle case where data is missing in success response
                                helper.showToastMessage(this, "Unexpected server response")
                            }
                        } catch (e: JSONException) {
                            // Handle JSON parsing errors
                            helper.showToastMessage(this, "Unexpected response format")
                        }
                    }
                }
            }

        }


    }


    private fun clearCartItems() {
        val sharedPref = getSharedPreferences("cart_data", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            remove("cartItems")
            apply()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        return when (item.itemId) {
//            R.id.action_settings -> true
//            else -> super.onOptionsItemSelected(item)
//        }
//    }

//    override fun onSupportNavigateUp(): Boolean {
//        val navController = findNavController(R.id.nav_host_fragment_content_main)
//        return navController.navigateUp(appBarConfiguration)
//                || super.onSupportNavigateUp()
//    }
}