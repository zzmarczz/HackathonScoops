package com.icecream.demo.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CartItem(
    val iceCream: IceCream,
    var quantity: Int = 1
) : Parcelable {
    
    val totalPrice: Double
        get() = iceCream.price * quantity
}

