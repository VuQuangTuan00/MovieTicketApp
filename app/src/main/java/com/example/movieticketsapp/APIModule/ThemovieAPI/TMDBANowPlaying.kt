package com.example.movieticketsapp.APIModule.ThemovieAPI

import com.example.movieticketsapp.APIModule.ThemovieAPI.list.GenreList
import com.example.movieticketsapp.APIModule.ThemovieAPI.list.MovieList
import retrofit2.http.GET
import retrofit2.http.Query

interface TMDBANowPlaying {
    companion object {
        const val API_KEY = "f9ff8fb064599817d1e3023214924caa"
    }
    @GET("movie/now_playing")
    suspend fun getNowPlayingMovies(
        @Query("api_key") apiKey:String,
        @Query("language") language:String = "en-US",
        @Query("page") page: Int = 1,
    ): MovieList
}

interface TMDBAUpComming {
    @GET("movie/upcoming")
    suspend fun getNowUpCommingMovies(
        @Query("api_key") apiKey:String,
        @Query("language") language:String = "en-US",
        @Query("page") page: Int = 1,
    ): MovieList
}

interface TMDBAGenres {
    @GET("genre/movie/list")
    suspend fun getGenresMovies(
        @Query("api_key") apiKey:String,
        @Query("language") language:String = "en",
    ): GenreList
}

