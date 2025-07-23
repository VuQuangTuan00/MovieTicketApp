package com.example.movieticketsapp.model

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

data class User(
    var name: String,
    var phone:String,
    val dob:String,
    val avatar:String,
    var email: String,
    val role: String,
)
