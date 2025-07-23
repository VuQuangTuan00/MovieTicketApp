package com.example.movieticketsapp.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Food(
    var food_id: String = "",
    val img_food:String = "",
    val food_name: String = "",
    val price: Double = 0.0,
    val description: String = "",
    val status: String = "",
): Parcelable
