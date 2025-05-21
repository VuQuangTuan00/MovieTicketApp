package com.example.movieticketsapp.model

import java.time.LocalDateTime

data class ShowTime(
    val movieId: String,
    val cinemaId: String,
    val startTime: LocalDateTime,
    val price: Int,
)
