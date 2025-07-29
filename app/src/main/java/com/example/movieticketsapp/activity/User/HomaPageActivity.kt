package com.example.movieticketsapp.activity.User

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.movieticketsapp.APIModule.ThemovieAPI.list.MovieList
import com.example.movieticketsapp.APIModule.ThemovieAPI.model.MovieModel
import com.example.movieticketsapp.APIModule.ThemovieAPI.RetrofitClient
import com.example.movieticketsapp.APIModule.ThemovieAPI.TMDBANowPlaying
import com.example.movieticketsapp.R
import com.example.movieticketsapp.adapter.ItemMovieAdapter
import com.example.movieticketsapp.databinding.HomaPageLayoutBinding
import com.example.movieticketsapp.model.Movie
import com.example.movieticketsapp.utils.navigateTo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

class HomaPageActivity : AppCompatActivity() {
    private lateinit var binding: HomaPageLayoutBinding
    private lateinit var adapterMovie: ItemMovieAdapter
    private lateinit var listMovie: ArrayList<MovieModel>
    private lateinit var imgList: ArrayList<SlideModel>
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var movieListener: ListenerRegistration? = null
    private lateinit var listBestMovie: ArrayList<Movie>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize()
        setContentView(binding.root)
        setEvent()
        listenToImgMovieCollectionRealtime()
        fetchPlayingMovies()
        fetchUpCommingMovies()
        getUserInfo()
        setupBottomNavigationView()
        listenToMovieCollectionRealtime()
    }

    private fun initialize() {
        binding = HomaPageLayoutBinding.inflate(layoutInflater)
        listMovie = ArrayList()
        adapterMovie = ItemMovieAdapter(listMovie) {}
        imgList = ArrayList()
        listBestMovie = ArrayList()
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
    }

    private fun setEvent() {
        binding.apply {
            tvViewAllNowPlaying.setOnClickListener {
                navigateTo(ViewAllMovie::class.java, flag = false)
            }
        }
    }

    private fun fetchMovies(
        apiCall: suspend () -> MovieList,
        recyclerView: RecyclerView,
        adapterFactory: (ArrayList<MovieModel>) -> RecyclerView.Adapter<*>,

    ) {

        lifecycleScope.launch {
            try {
                val response = apiCall()
                val movies = response.results
                val layoutManager = LinearLayoutManager(
                    this@HomaPageActivity,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                recyclerView.layoutManager = layoutManager
                recyclerView.adapter = adapterFactory(movies)
            } catch (e: Exception) {
                Log.e("TMDB", "Lỗi khi gọi API: ${e.message}")
            }
        }
    }

    private fun fetchPlayingMovies() {
        fetchMovies(
            apiCall = { RetrofitClient.apiMovieNowPlaying.getNowPlayingMovies(TMDBANowPlaying.API_KEY) },
            recyclerView = binding.rcvMovie,
            adapterFactory = { movies ->
                listMovie = movies
                ItemMovieAdapter(listMovie){
                    val intent = Intent(this, DetailsMovieActivity::class.java)
                    intent.putExtra("movie_id", it.id)
                    startActivity(intent)
                }
            }
        )
    }

    private fun fetchUpCommingMovies() {
        fetchMovies(
            apiCall = { RetrofitClient.apiMovieUpComming.getNowUpCommingMovies(TMDBANowPlaying.API_KEY) },
            recyclerView = binding.rcvUpComming,
            adapterFactory = {
                listMovie = it
                adapterMovie =  ItemMovieAdapter(listMovie) {
                    val intent = Intent(this, DetailsMovieActivity::class.java)
                    intent.putExtra("movie_id", it.id)
                    startActivity(intent)
                }
                adapterMovie
            }
        )
    }
    private fun listenToMovieCollectionRealtime() {
        movieListener = db.collection("movie")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("MovieRealtime", "Listen failed.", e)
                    return@addSnapshotListener
                }

                snapshots?.let { querySnapshot ->
                    val firebaseMovies = ArrayList<MovieModel>()

                    for (doc in querySnapshot) {
                        val data = doc.data
                        val idMovie = doc.id.toIntOrNull() ?: continue
                        val imgMovie = data["poster_path"] as? String ?: ""
                        val title = data["title"] as? String ?: ""
                        val overview = data["overview"] as? String ?: ""
                        val releaseDate = data["release_date"] as? String ?: ""

                        if (title.isNotEmpty()) {
                            firebaseMovies.add(
                                MovieModel(
                                    id = idMovie,
                                    title = title,
                                    poster_path = imgMovie,
                                    genre_ids = listOf(),
                                    overview = overview,
                                    release_date = releaseDate
                                )
                            )
                        }
                    }

                    // Merge với existing data
                    updateMovieList(firebaseMovies)
                }
            }
    }

    private fun updateMovieList(firebaseMovies: ArrayList<MovieModel>) {
        // Add Firebase movies vào cuối list
        val startPosition = listMovie.size
        listMovie.addAll(firebaseMovies)

        // Notify adapter về việc insert new items
        adapterMovie.notifyItemRangeInserted(startPosition, firebaseMovies.size)

        Log.d("MovieUpdate", "Added ${listMovie.size} ${ firebaseMovies.size} Firebase movies to existing ${startPosition} API movies")
    }


    private fun listenToImgMovieCollectionRealtime() {
        db.collection("img_slide")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("SlideRealTime", "Listen failed.", e)
                    return@addSnapshotListener
                }

                snapshots?.let {
                    imgList.clear()
                    for (doc in it) {
                        val img = doc.getString("img")
                        img?.let {
                            imgList.add(SlideModel(it, ScaleTypes.FIT))
                            binding.imgSlider.setImageList(imgList, ScaleTypes.FIT)
                        }
                    }
                }
            }
    }

    private fun getUserInfo() {
        val currentUser = auth.currentUser
        currentUser?.let {
            val userId = it.uid
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val name = document.getString("name") ?: "User"
                        val avatarUrl = document.getString("avatar")
                        binding.tvNameUser.text = name

                        avatarUrl?.let { url ->
                            Glide.with(this)
                                .load(url)
                                .circleCrop()
                                .into(binding.imgAvatarUser)
                        }
                    } else {
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("GetUserInfo", "Error getting user info", e)
                    Toast.makeText(this, "Error getting user info", Toast.LENGTH_SHORT).show()
                }
        } ?: run {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBottomNavigationView() {
        binding.bottomnvg.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    val intent = Intent(this, HomaPageActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.food_order -> {
                    startActivity(Intent(this, FoodActivity::class.java))
                    true
                }

                R.id.ticket -> {
                    val intent = Intent(this, ListTicketActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.account -> {
                    val intent = Intent(this, AccountProfileActivity::class.java)
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        movieListener?.remove()
    }
    override fun onStop() {
        super.onStop()
        movieListener?.remove()
        movieListener = null
    }
}
