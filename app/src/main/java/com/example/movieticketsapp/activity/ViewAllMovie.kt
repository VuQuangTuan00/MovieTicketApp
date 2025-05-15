package com.example.movieticketsapp.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movieticketsapp.adapter.ItemGenerMovieAdapter
import com.example.movieticketsapp.adapter.ItemMovieAdapter
import com.example.movieticketsapp.databinding.ViewAllMovieLayoutBinding
import com.example.movieticketsapp.model.GenerMovie
import com.example.movieticketsapp.model.Movie
import com.example.movieticketsapp.utils.navigateTo
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ViewAllMovie : AppCompatActivity() {
    private lateinit var binding: ViewAllMovieLayoutBinding
    private lateinit var adapterGenerMovie: ItemGenerMovieAdapter
    private lateinit var listGenerMovie: ArrayList<GenerMovie>
    private lateinit var adapterMovie: ItemMovieAdapter
    private lateinit var listMovie: ArrayList<Movie>
    private lateinit var db: FirebaseFirestore
    private var selectedGenreId: String? = null
    private var searchKeyword: String = ""
    private var searchQuery: String = ""
    private var searchJob: Job? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize()
        setContentView(binding.root)
        setEvent()
        loadMoviesWithGenres()
        setAdapterMovie()
        searchMovie()
        filterMovieByGener()
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
        adapterMovie = ItemMovieAdapter(listMovie){event ->
            val intent = Intent(this, DetailsMovieActivity::class.java)
            intent.putExtra("movie_id", event.id)
            startActivity(intent)
        }
        binding.rcvMovie.adapter = adapterMovie
    }

    private fun filterMovieByGener() {
        adapterGenerMovie = ItemGenerMovieAdapter(listGenerMovie) { selectedGenre ->
            selectedGenreId = selectedGenre.id
            filterAndSearchMovies()
        }
        binding.rcvGenerMovie.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rcvGenerMovie.adapter = adapterGenerMovie
    }

    private fun searchMovie() {
        binding.edtSearch.addTextChangedListener { editable ->
            searchJob?.cancel()
            searchJob = CoroutineScope(Dispatchers.Main).launch {
                delay(300)
                searchQuery = editable.toString().trim()
                searchKeyword = searchQuery
                filterAndSearchMovies()
            }
        }
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
                                            doc.id,
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
    private fun filterAndSearchMovies() {
        val filtered = listMovie.filter { movie ->
            val matchGenre = selectedGenreId == null || movie.gener_movie.contains(selectedGenreId)
            val matchTitle = movie.title.lowercase().contains(searchKeyword)
            matchGenre && matchTitle
        }
        adapterMovie.updateData(filtered)
    }
}