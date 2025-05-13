package com.example.movieticketsapp.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movieticketsapp.adapter.ItemMovieAdminAdapter
import com.example.movieticketsapp.databinding.AdminMovieMovieLayoutBinding
import com.example.movieticketsapp.model.MovieAdmin
import com.google.firebase.firestore.FirebaseFirestore

class AdminMovieMovieActivity : AppCompatActivity() {
    private lateinit var binding: AdminMovieMovieLayoutBinding
    private lateinit var adapter: ItemMovieAdminAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AdminMovieMovieLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appbar.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Danh sách phim"

        binding.listMovies.layoutManager = LinearLayoutManager(this)

        loadMoviesFromFirestore()
    }

    private fun loadMoviesFromFirestore() {
        db.collection("movie")
            .get()
            .addOnSuccessListener { result ->
                val movieList = mutableListOf<MovieAdmin>()
                val genreNamesMap = mutableMapOf<String, String>()
                val castNamesMap = mutableMapOf<String, String>()

                db.collection("gener").get().addOnSuccessListener { genreResult ->
                    for (genreDoc in genreResult) {
                        val genreName = genreDoc.getString("name") ?: ""
                        genreNamesMap[genreDoc.id] = genreName
                    }

                    db.collection("cast").get().addOnSuccessListener { actorResult ->
                        for (castDoc in actorResult) {
                            val actorName = castDoc.getString("name") ?: ""
                            castNamesMap[castDoc.id] = actorName
                        }

                        for (document in result) {
                            val movie = document.toObject(MovieAdmin::class.java)

                            val genreNames = movie.gener_movie.mapNotNull { genreNamesMap[it] }
                            val castNames = movie.list_casts.mapNotNull { castNamesMap[it] }

                            val updatedMovie = movie.copy(
                                genreNames = genreNames,
                                castNames = castNames
                            )

                            movieList.add(updatedMovie)
                        }

                        adapter = ItemMovieAdminAdapter(movieList) { movie ->
                            Toast.makeText(this, "Clicked on: ${movie.title}", Toast.LENGTH_SHORT).show()
                        }
                        binding.listMovies.adapter = adapter
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error loading movies: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
