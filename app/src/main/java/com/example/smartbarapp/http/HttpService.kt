package com.example.smartbarapp.http


import android.content.Context
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class HTTPService {


    private val BASE_URL = "http://10.0.2.2:2125/api"


    fun postRequest(
        context: Context,
        urlString: String,
        name: String,
        phoneNumber: String,
        callback: (String) -> Unit
    ) {
        Thread {
            try {
                val url = URL(BASE_URL + urlString)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                // Create JSON payload
                val jsonObject = JSONObject().apply {
                    put("name", name)
                    put("phoneNumber", phoneNumber)
                }
                val payload = jsonObject.toString()

                // Write payload to output stream
                DataOutputStream(conn.outputStream).use { outputStream ->
                    outputStream.writeBytes(payload)
                    outputStream.flush()
                }

                val responseCode = conn.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = StringBuilder()
                    BufferedReader(InputStreamReader(conn.inputStream)).use { reader ->
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            response.append(line).append("\n")
                        }
                    }
                    val responseString = response.toString().trim()

                    try {
                        val responseJson = JSONObject(responseString)
                        if (responseJson.has("data")) {
                            val data = responseJson.getJSONObject("data")
                            val token = data.getString("accessToken")
                            val userName = data.getString("name")

                            // Save token and user details
                            saveToken(context, token, userName)

                            // Log response for debugging
                            Log.i("feedback", "Response: $responseString")
                            callback(responseString)
                        } else {
                            Log.e("feedback error", "No data in response")
                            callback("No data in response")
                        }
                    } catch (e: JSONException) {
                        Log.e("feedback error", "Failed to parse response: ${e.message}")
                        callback("Failed to parse response")
                    }
                } else {
                    val errorResponse = StringBuilder()
                    BufferedReader(InputStreamReader(conn.errorStream)).use { reader ->
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            errorResponse.append(line).append("\n")
                        }
                    }
                    val error = "Error: ${conn.responseCode}, Details: ${errorResponse.toString().trim()}"
                    Log.e("feedback error", error)
                    callback(error)
                }
            } catch (e: Exception) {
                Log.e("feedback error", "Exception: ${e.message}")
                callback("Exception: ${e.message}")
            }
        }.start()
    }


    fun saveToken(context: Context, token: String, name: String) {
        val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("authToken", token)
        editor.putString("userName", name)
        editor.apply()
    }






    fun fetchResponse(urlString: String, callback: (Int, String) -> Unit) {
        Thread {
            val url = URL(BASE_URL + urlString)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"

            try {
                val responseCode = conn.responseCode
                val response = StringBuilder()
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                var line: String?

                while (reader.readLine().also { line = it } != null) {
                    response.append(line).append("\n")
                }
                reader.close()
                val responseString = response.toString().trim()

                Log.i("HTTP Response", responseString)

                callback(responseCode, responseString)
            } catch (e: Exception) {
                e.printStackTrace()
                callback(HttpURLConnection.HTTP_INTERNAL_ERROR, "Error: ${e.message}")
            } finally {
                conn.disconnect()
            }
        }.start()
    }






    fun fetchSingleResponse(urlString: String, callback: (String) -> Unit) {
        Thread {
            val url = URL(BASE_URL + urlString)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Accept", "application/json")

            try {
                val responseCode = conn.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = StringBuilder()
                    val reader = BufferedReader(InputStreamReader(conn.inputStream))
                    var line: String?

                    while (reader.readLine().also { line = it } != null) {
                        response.append(line).append("\n")
                    }
                    reader.close()
                    val responseString = response.toString().trim()

                    Log.i("feedback", "Raw response: $responseString")

                    callback(responseString)
                } else {
                    val error = "Error: $responseCode"
                    Log.e("feedback error", error)
                    callback(error)
                }
            } finally {
                conn.disconnect()
            }
        }.start()
    }





}







//    fun postUserIdResponse(urlString: String, payload: String, callback: (String) -> Unit) {
//        Thread {
//            val url = URL(BASE_URL + urlString)
//            val conn = url.openConnection() as HttpURLConnection
//            conn.requestMethod = "POST"
//            conn.setRequestProperty("Content-Type", "application/json") // Set content type to JSON
//            conn.doOutput = true // Enable output stream for sending data
//
//            // Write payload to output stream
//            val outputStream = DataOutputStream(conn.outputStream)
//            outputStream.writeBytes(payload)
//            outputStream.flush()
//            outputStream.close()
//
//            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
//                val response = StringBuilder()
//                val reader = BufferedReader(InputStreamReader(conn.inputStream))
//                var line: String?
//
//                while (reader.readLine().also { line = it } != null) {
//                    response.append(line).append("\n")
//                }
//                reader.close()
//                val responseString = response.toString().trim() // Trim extra characters (optional)
//
//                // Log raw response for debugging
//                Log.i("feedback", "Raw response: $responseString")
//
//                callback(responseString)
//            } else {
//                val error = "Error: ${conn.responseCode}"
//                Log.e("feedback error", error)
//                callback(error)  // Pass error message back in the callback
//            }
//
//            conn.disconnect()
//        }.start()
//    }


















