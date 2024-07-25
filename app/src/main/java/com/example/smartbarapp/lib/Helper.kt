package com.example.smartbarapp.lib

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Helper {


    fun navigate(context: Context, destinationClass: Class<out AppCompatActivity>) {
        val intent = Intent(context, destinationClass)
        context.startActivity(intent)
    }

    fun navigateWithPayload(context: Context, destinationClass: Class<out AppCompatActivity>, id: String) {
        val intent = Intent(context, destinationClass)
        intent.putExtra("EXTRA_ID", id)
        context.startActivity(intent)
    }

    fun showToastMessage(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }


    fun getToken(context: Context): String? {
        val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        return prefs.getString("authToken", null)
    }

    fun getName(context: Context): String? {
        val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        return prefs.getString("userName", null)
    }





}

