package com.example.movieticketsapp.model

import java.util.Date

data class ShowTime(
    val id: String = "",
    val cinema_id: String = "",
    val movie_id: String = "",
    val price: Int = 0,
    val start_time: Date = Date()
)
