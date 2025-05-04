package com.example.movieticketsapp.model

data class Movie(
    val title: String = "",
    val director: String = "",
    val duration: Int = 0,
    val genre: List<String> = listOf(),
    val img_movie: String = "",
    val list_photos: List<String> = listOf(),
    val trailer: String = "",
    val rating: Double = 0.0,
    val synopsis: String = ""
)
