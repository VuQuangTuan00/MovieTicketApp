package com.example.movieticketsapp.model
import java.util.Date

data class Review(
    val userEmail: String,
    val score: Double,
    val comment: String,
    val date: Date?
)