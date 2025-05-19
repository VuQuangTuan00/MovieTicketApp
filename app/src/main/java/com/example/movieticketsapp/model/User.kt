package com.example.movieticketsapp.model

import TicketMovie
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

data class User(
    val name: String,
    val phone:String,
    val dob:String,
    val avatar:String,
    val email: String,
    val role: String,
)
{
}