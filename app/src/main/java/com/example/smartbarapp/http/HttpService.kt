package com.example.smartbarapp.http


import android.content.Context
import android.util.Log
import com.example.smartbarapp.lib.Helper
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class HTTPService {


    private val BASE_URL = "http://10.0.2.2:2125/api"
    private lateinit var helper: Helper


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
                            val userId = data.getString("id")


                            saveToken(context, token, userName, userId)
                            callback(responseString) // Success response passed here
                        } else {
                            callback("Error: No data in response") // Ensure error is passed correctly
                        }
                    } catch (e: JSONException) {
                        callback("Error: Failed to parse response") // Catch JSON parsing issues
                    }
                } else {
                    // Error flow
                    val errorResponse = StringBuilder()
                    BufferedReader(InputStreamReader(conn.errorStream)).use { reader ->
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            errorResponse.append(line).append("\n")
                        }
                    }
                    val formattedError = parseErrorResponse(errorResponse.toString())
                    callback("Error: $formattedError") // Ensure error is prefixed with "Error:"
                }
            } catch (e: Exception) {
                callback("Error: Exception: ${e.message}") // Exception handling
            }
        }.start()
    }

    fun parseErrorResponse(errorResponse: String): String {
        return try {
            val json = JSONObject(errorResponse)

            // Check if the response contains a "data" field
            val data = json.optJSONObject("data")
            if (data != null) {
                val errorMessages = mutableListOf<String>()
                data.keys().forEach { key ->
                    val message = data.optString(key, "")
                    if (message.isNotBlank()) {
                        errorMessages.add(message)
                    }
                }
                return if (errorMessages.isNotEmpty()) {
                    errorMessages.joinToString("\n") // Join multiple messages with a newline
                } else {
                    "An error occurred. Please try again."
                }
            }

            // Handle cases where error details are in "message" or "details"
            val message = json.optString("message", "")
            if (message.isNotBlank()) {
                return message
            }

            val details = json.optString("details", "")
            if (details.isNotBlank()) {
                return details
            }

            // Default fallback if no recognizable fields are found
            "An error occurred. Please try again."
        } catch (e: JSONException) {
            // If parsing fails, return a generic error message
            "An error occurred. Please try again."
        }
    }



    fun postRequestObject(
        context: Context,
        urlString: String,
        jsonObjectVal: String,
        callback: (String) -> Unit
    ) {
        Thread {
            try {
                // Retrieve the token
                val helper = Helper()
                val token = helper.getToken(context)
                val url = URL(BASE_URL + urlString)
                val conn = url.openConnection() as HttpURLConnection

                Log.i("token", ": $token")
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                // Set Bearer token in the Authorization header
                if (token != null) {
                    conn.setRequestProperty("Authorization", "Bearer $token")
                }
                conn.doOutput = true

                // Write payload to output stream
                DataOutputStream(conn.outputStream).use { outputStream ->
                    outputStream.writeBytes(jsonObjectVal)
                    outputStream.flush()
                }

                val responseCode = conn.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
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
                            val data = responseJson.get("data")
                            // Log response for debugging
                            Log.i("feedback", "Response: $data")
                            callback(responseString)
                        } else {
                            Log.e("feedback error", "No data in response")
                            callback("No data in response")
                        }
                    } catch (e: JSONException) {
                        // Handle the case where the "data" field is not a JSON object
                        if (responseString.contains("Order created")) {
                            Log.i("feedback", "Response: Order created")
                            callback("Order created")
                        } else {
                            Log.e("feedback error", "Failed to parse response: ${e.message}")
                            callback("Failed to parse response")
                        }
                    }
                } else {
                    val errorResponse = StringBuilder()
                    BufferedReader(InputStreamReader(conn.errorStream)).use { reader ->
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            errorResponse.append(line).append("\n")
                        }
                    }
                    val formattedError = parseErrorResponse(errorResponse.toString())
                    callback("Error: $formattedError")
                }
            } catch (e: Exception) {
                Log.e("feedback error", "Exception: ${e.message}")
                callback("Exception: ${e.message}")
            }
        }.start()
    }

    fun putRequestObject(
        context: Context,
        urlString: String,
        body: String, // Accept a plain string as the request body
        callback: (Int, String) -> Unit // Accept the response code as Int, and response as String
    ) {
        Thread {
            try {
                // Retrieve the token
                val helper = Helper()
                val token = helper.getToken(context)
                val url = URL(BASE_URL + urlString)
                val conn = url.openConnection() as HttpURLConnection

                conn.requestMethod = "PUT"
                conn.setRequestProperty("Content-Type", "application/json")
                if (token != null) {
                    conn.setRequestProperty("Authorization", "Bearer $token")
                }

                conn.doOutput = true

                // Write the plain string body to the output stream
                DataOutputStream(conn.outputStream).use { outputStream ->
                    outputStream.writeBytes(body)
                    outputStream.flush()
                }

                val responseCode = conn.responseCode
                val responseMessage = StringBuilder()

                // Check the response stream: if response code is in success range, use inputStream, otherwise use errorStream
                val inputStream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        responseMessage.append(line).append("\n")
                    }
                }

                // Log the response for debugging
                Log.d("HTTP Response", "Response Code: $responseCode")
                Log.d("HTTP Response", "Response Body: ${responseMessage.toString().trim()}")

                // Call the callback with the response code and message
                callback(responseCode, responseMessage.toString().trim())

            } catch (e: Exception) {
                // Log the exception for debugging
                Log.e("Feedback Error", "Exception: ${e.message}")
                callback(-1, "Exception: ${e.message}") // Pass a default error code and the exception message
            }
        }.start()
    }





    fun saveToken(context: Context, token: String, name: String, id: String) {
        val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("authToken", token)  // Save the token
        editor.putString("userName", name)    // Save the user name
        editor.putString("userId", id)        // Save the user ID
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


    fun fetchResponseWithToken(context: Context,urlString: String, callback: (Int, String) -> Unit) {
        Thread {
            val url = URL(BASE_URL + urlString)
            val helper = Helper()
            val token = helper.getToken(context)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"


                try {
                    Log.i("Token", "Token being sent: $token")
                    conn.requestMethod = "GET"
                    conn.setRequestProperty("Content-Type", "application/json")

                    // Set the Authorization header with the Bearer token
                    if (token != null) {
                        conn.setRequestProperty("Authorization", "Bearer $token")
                        Log.i("Authorization", "Bearer $token")
                    } else {
                        Log.e("Authorization Error", "Token is null")
                        return@Thread
                    }

                    conn.doOutput = false
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



    fun fetchOrderDetails(context: Context, orderId: Int, callback: (String) -> Unit) {
        Thread {
            val helper = Helper() // Assuming you have a Helper class to retrieve the token
            val token = helper.getToken(context)
            val urlString = "${BASE_URL}/orders/$orderId"
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection

            try {
                Log.i("Token", "Token being sent: $token")
                conn.requestMethod = "GET"
                conn.setRequestProperty("Content-Type", "application/json")

                // Set the Authorization header with the Bearer token
                if (token != null) {
                    conn.setRequestProperty("Authorization", "Bearer $token")
                    Log.i("Authorization", "Bearer $token")
                } else {
                    Log.e("Authorization Error", "Token is null")
                    callback("Error: Missing token")
                    return@Thread
                }

                conn.doOutput = false

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
                    Log.i("Response", "Raw response: $responseString")
                    callback(responseString)
                } else {
                    val error = "Error: $responseCode"
                    Log.e("Response Error", error)
                    callback(error)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("Exception", "Request failed: ${e.message}")
                callback("Error: ${e.message}")
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


















