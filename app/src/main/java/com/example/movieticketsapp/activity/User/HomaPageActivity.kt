package com.example.movieticketsapp.activity.User

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Adapter
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
//        setAdapterMovie()
//        setAdapterBestMovie()
//        listenToBestRatedMovies()
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
            adapterFactory = { movies ->
                listMovie = movies
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

                snapshots?.let {
                    listMovie.clear()
                    for (doc in it) {
                        val data = doc.data
                        val idMovie = doc.id.toInt()
                        val imgMovie = data["poster_path"] as? String
                        val title = data["title"] as? String

                        if (!imgMovie.isNullOrEmpty() && !title.isNullOrEmpty()) {
                            listMovie.add(
                                MovieModel(
                                   idMovie, title,"", listOf(),"",""
                                )
                            )
                        }
                    }
                    Log.d("ListMovie", "ListMovie: ${listMovie.size}")
                    adapterMovie.notifyDataSetChanged()
                }
            }
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

    override fun onStop() {
        super.onStop()
        movieListener?.remove()
        movieListener = null
    }
}
