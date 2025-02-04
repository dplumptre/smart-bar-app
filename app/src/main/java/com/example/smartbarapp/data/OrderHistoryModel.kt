package com.example.smartbarapp.data



data class OrderHistoryModel(
    val id: String,
    val myDate: String,
    val reference: String,
    val total: String,
    var status: String
)
