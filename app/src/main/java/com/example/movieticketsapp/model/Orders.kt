package com.example.movieticketsapp.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue

data class Orders(
    val receivedAt:String ,
    val foodDeliveryDate: String,
    val status: String,
    val timestamp: FieldValue,
)
