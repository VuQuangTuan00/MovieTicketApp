package com.example.movieticketsapp.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CartItem(
    val food: Food,
    var quantity: Int
) : Parcelable