package com.example.movieticketsapp.APIModule.ThemovieAPI.model


data class MovieModel(
    var id:Int,
    var title:String,
    var overview:String,
    var genre_ids:List<Int>,
    val poster_path: String?,
    val release_date: String
)