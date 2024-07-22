package com.example.cafetariaapp.lib


import android.content.Context
import android.content.SharedPreferences

object NifManager {

    private lateinit var userDataSharedPreferences: SharedPreferences
    private lateinit var cartDataSharedPreferences: SharedPreferences
    private lateinit var summaryDataSharedPreferences: SharedPreferences


    fun init(context: Context) {
        userDataSharedPreferences = context.getSharedPreferences("user_data", Context.MODE_PRIVATE)
        cartDataSharedPreferences = context.getSharedPreferences("cart_data", Context.MODE_PRIVATE)
        summaryDataSharedPreferences = context.getSharedPreferences("summary_data", Context.MODE_PRIVATE)

    }

    fun storeUserData(name: String, nif: String, userId: String) {
        val editor = userDataSharedPreferences.edit()
        editor.putString("name", name)
        editor.putString("nif", nif)
        editor.putString("userId", userId)
        editor.apply()
    }

    fun retrieveName(): String? {
        return userDataSharedPreferences.getString("name", null)
    }

    fun retrieveNif(): String? {
        return userDataSharedPreferences.getString("nif", null)
    }

    fun retrieveUserId(): String? {
        return userDataSharedPreferences.getString("userId", null)
    }


    fun clearUserData() {
        val userDataEditor = userDataSharedPreferences.edit()
        userDataEditor.remove("name")
        userDataEditor.remove("nif")
        userDataEditor.remove("userId")
        userDataEditor.apply()

        val cartDataEditor = cartDataSharedPreferences.edit()
        cartDataEditor.clear() // Clear all cart items and keys
        cartDataEditor.apply()

        val summaryEditor = summaryDataSharedPreferences.edit()
        summaryEditor.clear() // Clear all cart items and keys
        summaryEditor.apply()
    }

    fun clearUserDataForNewSignup() {
        clearUserData()
    }


}


