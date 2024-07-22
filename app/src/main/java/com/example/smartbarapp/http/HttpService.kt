package com.example.smartbarapp.http


import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class HTTPService {


    private val BASE_URL = "http://10.0.2.2:2125/api"

    fun postRequest(urlString: String, name: String, phoneNumber: String, callback: (String) -> Unit) {
        Thread {
            try {
                val url = URL(BASE_URL + urlString)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

                val postData = "name=${URLEncoder.encode(name, "UTF-8")}&phone=${URLEncoder.encode(phoneNumber, "UTF-8")}"

                conn.outputStream.use { os ->
                    val writer = OutputStreamWriter(os)
                    writer.write(postData)
                    writer.flush()
                }

                val responseCode = conn.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = StringBuilder()
                    BufferedReader(InputStreamReader(conn.inputStream)).use { reader ->
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            response.append(line)
                        }
                    }
                    callback(response.toString())
                } else {
                    val errorResponse = StringBuilder()
                    BufferedReader(InputStreamReader(conn.errorStream)).use { reader ->
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            errorResponse.append(line)
                        }
                    }
                    Log.i("errors to catch", "Error: ${conn.responseMessage}, Details: $errorResponse")
                    callback("Error: ${conn.responseMessage}, Details: $errorResponse")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.i("Exception:", "${e.message}")
                callback("Exception: ${e.message}")
            }
        }.start()
    }







//    fun fetchResponse(urlString: String, callback: (String) -> Unit) {
//        Thread {
//            val url = URL(BASE_URL + urlString)
//            val conn = url.openConnection() as HttpURLConnection
//            conn.requestMethod = "GET"
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
//                val responseString = response.toString()
//                println(responseString)
//                callback(responseString)
//            } else {
//                // Handle errors (e.g., logging, displaying error message)
//                callback("Error: ${conn.responseCode}")
//            }
//
//            conn.disconnect()  // Disconnect after completion or error
//        }.start()
//    }







//    fun fetchSingleResponse(urlString: String, callback: (String) -> Unit) {
//        Thread {
//            val url = URL(BASE_URL + urlString)
//            val conn = url.openConnection() as HttpURLConnection
//            conn.requestMethod = "GET"
//            conn.setRequestProperty("Accept", "application/json") // Request JSON response
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















}



