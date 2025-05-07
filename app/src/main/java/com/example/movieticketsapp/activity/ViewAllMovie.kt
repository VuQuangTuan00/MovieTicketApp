package com.example.movieticketsapp.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movieticketsapp.R
import com.example.movieticketsapp.adapter.ItemGenerMovieAdapter
import com.example.movieticketsapp.adapter.ItemMovieAdapter
import com.example.movieticketsapp.databinding.ViewAllMovieLayoutBinding
import com.example.movieticketsapp.model.GenerMovie
import com.example.movieticketsapp.model.Movie
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class ViewAllMovie : AppCompatActivity() {
    private lateinit var binding: ViewAllMovieLayoutBinding
    private lateinit var adapterGenerMovie: ItemGenerMovieAdapter
    private lateinit var listGenerMovie: ArrayList<GenerMovie>
    private lateinit var adapterMovie: ItemMovieAdapter
    private lateinit var listMovie: ArrayList<Movie>
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize()
        setContentView(binding.root)
        setEvent()
        loadMoviesWithGenres()
        setAdapterGenerMovie()
        setAdapterMovie()
        searchMovie()
        filterMovieByGener()
//        listenToGenerMovieCollectionRealtime()
    }

    private fun initialize() {
        binding = ViewAllMovieLayoutBinding.inflate(layoutInflater)
        db = Firebase.firestore
        db = FirebaseFirestore.getInstance()
        listGenerMovie = ArrayList()
        listMovie = ArrayList()
    }

    private fun setEvent() {
        binding.apply {
            imgBack.setOnClickListener {
                finish()
            }
        }
    }

    private fun setAdapterMovie() {
        val layoutManager = GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false)
        binding.rcvMovie.layoutManager = layoutManager
        adapterMovie = ItemMovieAdapter(listMovie)
        binding.rcvMovie.adapter = adapterMovie
    }

    private fun setAdapterGenerMovie() {
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rcvGenerMovie.layoutManager = layoutManager
        adapterGenerMovie = ItemGenerMovieAdapter(listGenerMovie) { _ -> }
        binding.rcvGenerMovie.adapter = adapterGenerMovie
    }

    private fun searchMovie() {
        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val keyword = s.toString().trim().lowercase()

                val filteredList = listMovie.filter { movie ->
                    movie.title.lowercase().contains(keyword)
                }
                adapterMovie.updateData(filteredList)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadMoviesWithGenres() {
        val genreMap = mutableMapOf<String, String>()
        db.collection("gener")
            .get()
            .addOnSuccessListener { genreSnapshot ->
                for (doc in genreSnapshot) {
                    val genreId = doc.id
                    val name = doc.getString("name") ?: ""
                    genreMap[genreId] = name
                    if (name.isNotEmpty()) {
                        listGenerMovie.add(GenerMovie(doc.id, name))
                        adapterGenerMovie.notifyDataSetChanged()
                    }
                }
                db.collection("movie")
                    .addSnapshotListener { snapshots, e ->
                        if (e != null) {
                            Log.w("MovieRealtime", "Listen failed.", e)
                            return@addSnapshotListener
                        }
                        if (snapshots != null) {
                            listMovie.clear()
                            for (doc in snapshots) {
                                val data = doc.data
                                val imgMovie = data["img_movie"] as? String
                                val title = data["title"] as? String
                                val generMovie = data["gener_movie"] as? List<String>?: listOf()
                                if (!imgMovie.isNullOrEmpty() && !title.isNullOrEmpty()) {
                                    listMovie.add(
                                        Movie(
                                            title,
                                            "",
                                            0,
                                            generMovie,
                                            imgMovie,
                                            listOf(),
                                            "",
                                            0.0,
                                            ""
                                        )
                                    )
                                }
                            }
                            adapterMovie.notifyDataSetChanged()
                        }
                    }
            }
            .addOnFailureListener { e ->
                Log.e("GenreLoad", "Failed to load genres", e)
            }

    }
    private fun filterMovieByGener() {
        adapterGenerMovie = ItemGenerMovieAdapter(listGenerMovie) { selectedGenre ->
            val genreId = selectedGenre.id
            val filteredMovies = listMovie.filter { movie ->
                movie.gener_movie.contains(genreId)
            }
            adapterMovie.updateData(filteredMovies)
        }
        binding.rcvGenerMovie.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rcvGenerMovie.adapter = adapterGenerMovie
    }
}