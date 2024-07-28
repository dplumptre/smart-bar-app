package com.example.cafetariaapp.lib

import com.example.smartbarapp.data.CartItem

data class Order(
    val name: String,
    val orderItems: MutableList<CartItem>,
    val coffeeVoucher:String,
    val discountVoucher:String,
    val total: Double
)