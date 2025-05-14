package com.example.movieticketsapp.model

data class MovieAdmin(
    val id: String = "",
    val title: String = "",
    val director: String = "",
    val duration: Int = 0,
    val gener_movie: List<String> = listOf(),
    val img_movie: String = "",
    val list_casts: List<String> = listOf(),
    val list_photos: List<String> = listOf(),
    val trailer: String = "",
    val rating: Double = 0.0,
    val synopsis: String = "",

    var genreNames: List<String> = listOf(), // Lưu tên thể loại
    var castNames: List<String> = listOf() // Lưu tên diễn viên
)

data class MovieDetail(
    val id: String = "",
    val title: String = "",
    val director: String = "",
    val duration: Int = 0,
    val gener_movie: List<String> = listOf(),
    val img_movie: String = "",
    val list_casts: List<String> = listOf(),
    val list_photos: List<String> = listOf(),
    val trailer: String = "",
    val rating: Double = 0.0,
    val synopsis: String = "",
)