package com.example.movieticketsapp.activity.User

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movieticketsapp.APIModule.ThemovieAPI.RetrofitClient
import com.example.movieticketsapp.APIModule.ThemovieAPI.TMDBANowPlaying
import com.example.movieticketsapp.APIModule.ThemovieAPI.model.GenreModel
import com.example.movieticketsapp.APIModule.ThemovieAPI.model.MovieModel
import com.example.movieticketsapp.adapter.ItemGenerMovieAdapter
import com.example.movieticketsapp.adapter.ItemMovieAdapter
import com.example.movieticketsapp.databinding.ViewAllMovieLayoutBinding
import com.example.movieticketsapp.model.Movie
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ViewAllMovie : AppCompatActivity() {
    private lateinit var binding: ViewAllMovieLayoutBinding
    private lateinit var listGenerMovie: ArrayList<GenreModel>
    private lateinit var listMovie: ArrayList<MovieModel>
    private lateinit var db: FirebaseFirestore
    private lateinit var adapterMovie: ItemMovieAdapter
    private var selectedGenreId: Int = 0
    private var searchKeyword: String = ""
    private var searchQuery: String = ""
    private var searchJob: Job? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize()
        setContentView(binding.root)
        setEvent()
        filterMovieAndGener()
        searchMovie()
    }

    private fun initialize() {
        binding = ViewAllMovieLayoutBinding.inflate(layoutInflater)
        listMovie = ArrayList()
        adapterMovie = ItemMovieAdapter(listMovie) {}
        db = Firebase.firestore
        db = FirebaseFirestore.getInstance()
        listGenerMovie = ArrayList()
    }

    private fun setEvent() {
        binding.apply {
            imgBack.setOnClickListener {
                finish()
            }
        }
    }

    private fun filterMovieAndGener() {
        lifecycleScope.launch {
            try {
                val genreDeferred =
                    async { RetrofitClient.apiGenreMovie.getGenresMovies(TMDBANowPlaying.API_KEY) }
                val movieDeferred =
                    async { RetrofitClient.apiMovieNowPlaying.getNowPlayingMovies(TMDBANowPlaying.API_KEY) }

                val genres = genreDeferred.await().genres
                listGenerMovie.addAll(genres)

                val movies = movieDeferred.await().results
                val originalMovies = movies.toList() // giữ danh sách gốc để dùng lại

                listMovie.clear()
                listMovie.addAll(originalMovies)

                // Gán adapter cho genre
                binding.rcvGenerMovie.layoutManager = LinearLayoutManager(
                    this@ViewAllMovie,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )

                val adapterGenre = ItemGenerMovieAdapter(listGenerMovie) {
                    selectedGenreId = it.id
                    if (selectedGenreId == 0) {
                        // Hiện tất cả phim
                        listMovie.clear()
                        listMovie.addAll(originalMovies)
                        adapterMovie.updateData(listMovie)
                    } else {
                        filterAndSearchMovies(originalMovies)
                    }
                }

                binding.rcvGenerMovie.adapter = adapterGenre

                // Gán adapter cho movie
                val gridLayoutManager = GridLayoutManager(
                    this@ViewAllMovie,
                    2,
                    GridLayoutManager.VERTICAL,
                    false
                )
                binding.rcvMovie.layoutManager = gridLayoutManager

                adapterMovie = ItemMovieAdapter(listMovie) {
                    val intent = Intent(this@ViewAllMovie, DetailsMovieActivity::class.java)
                    intent.putExtra("movie_id", it.id)
                    startActivity(intent)
                }

                binding.rcvMovie.adapter = adapterMovie

            } catch (e: Exception) {
                Log.e("TMDB", "Lỗi khi gọi API: ${e.message}")
            }
        }
    }

    private fun searchMovie() {
        binding.edtSearch.addTextChangedListener { editable ->
            searchJob?.cancel() // Hủy job cũ nếu có
            searchJob = lifecycleScope.launch {
                delay(300) // Debounce để tránh gọi liên tục
                val query = editable?.toString()?.trim().orEmpty()
                searchKeyword = query.lowercase()
                filterAndSearchMovies(listMovie)
            }
        }
    }


    //    private fun loadMoviesWithGenres() {
//        val genreMap = mutableMapOf<String, String>()
//        db.collection("gener")
//            .get()
//            .addOnSuccessListener { genreSnapshot ->
//                for (doc in genreSnapshot) {
//                    val genreId = doc.id
//                    val name = doc.getString("name") ?: ""
//                    genreMap[genreId] = name
//                    if (name.isNotEmpty()) {
//                        listGenerMovie.add(GenerMovie(doc.id, name))
//                        adapterGenerMovie.notifyDataSetChanged()
//                    }
//                }
//                db.collection("movie")
//                    .addSnapshotListener { snapshots, e ->
//                        if (e != null) {
//                            Log.w("MovieRealtime", "Listen failed.", e)
//                            return@addSnapshotListener
//                        }
//                        if (snapshots != null) {
//                            listMovie.clear()
//                            for (doc in snapshots) {
//                                val data = doc.data
//                                val imgMovie = data["img_movie"] as? String
//                                val title = data["title"] as? String
//                                val generMovie = data["gener_movie"] as? List<String>?: listOf()
//                                if (!imgMovie.isNullOrEmpty() && !title.isNullOrEmpty()) {
//                                    listMovie.add(
//                                        Movie(
//                                            doc.id,
//                                            title,
//                                            "",
//                                            0,
//                                            generMovie,
//                                            imgMovie,
//                                            listOf(),
//                                            "",
//                                            0.0,
//                                            ""
//                                        )
//                                    )
//                                }
//                            }
//                            adapterMovie.notifyDataSetChanged()
//                        }
//                    }
//            }
//            .addOnFailureListener { e ->
//                Log.e("GenreLoad", "Failed to load genres", e)
//            }
//    }
    private fun filterAndSearchMovies(originalList: List<MovieModel>) {
        val filtered = originalList.filter { movie ->
            val matchGenre = movie.genre_ids.contains(selectedGenreId)
            val matchTitle = movie.title.lowercase().contains(searchKeyword.lowercase())
            matchGenre && matchTitle
        }
        adapterMovie.updateData(filtered)
    }
}