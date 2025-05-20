package com.example.movieticketsapp.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.movieticketsapp.R
import com.example.movieticketsapp.activity.AccountProfileActivity
import com.example.movieticketsapp.activity.DetailsMovieActivity
import com.example.movieticketsapp.activity.ViewAllMovie
import com.example.movieticketsapp.adapter.ItemMovieAdapter
import com.example.movieticketsapp.databinding.HomaPageLayoutBinding
import com.example.movieticketsapp.model.Movie
import com.example.movieticketsapp.utils.navigateTo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class HomaPageActivity : AppCompatActivity() {
    private lateinit var binding: HomaPageLayoutBinding
    private lateinit var adapterMovie: ItemMovieAdapter
    private lateinit var listMovie: ArrayList<Movie>
    private lateinit var imgList: ArrayList<SlideModel>
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var movieListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = HomaPageLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initialize()
        setEvent()
        setAdapterMovie()
        listenToMovieCollectionRealtime()
        listenToImgMovieCollectionRealtime()
        getUserInfo()

        setupBottomNavigationView()
    }

    private fun initialize() {
        listMovie = ArrayList()
        imgList = ArrayList()
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

    private fun setAdapterMovie() {
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rcvMovie.layoutManager = layoutManager
        adapterMovie = ItemMovieAdapter(listMovie) { event ->
            val intent = Intent(this, DetailsMovieActivity::class.java)
            intent.putExtra("movie_id", event.id)
            startActivity(intent)
        }
        binding.rcvMovie.adapter = adapterMovie
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
                        val imgMovie = data["img_movie"] as? String
                        val title = data["title"] as? String

                        if (!imgMovie.isNullOrEmpty() && !title.isNullOrEmpty()) {
                            listMovie.add(
                                Movie(
                                    doc.id,
                                    title,
                                    "",
                                    0,
                                    listOf(),
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
                /*
                R.id.nav_food_order -> {
                    val intent = Intent(this, FoodOrderActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_ticket -> {
                    val intent = Intent(this, TicketActivity::class.java)
                    startActivity(intent)
                    true
                }
                 */
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
