package com.example.movieticketsapp.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.movieticketsapp.adapter.ItemCastsAdapter
import com.example.movieticketsapp.adapter.ItemPhotosApdater
import com.example.movieticketsapp.databinding.DetailsMovieLayoutBinding
import com.example.movieticketsapp.model.Cast
import com.example.movieticketsapp.model.GenerMovie
import com.example.movieticketsapp.utils.navigateTo
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class DetailsMovieActivity : AppCompatActivity() {
    private lateinit var binding: DetailsMovieLayoutBinding
    private lateinit var adapterPhotoMovie: ItemPhotosApdater
    private lateinit var db: FirebaseFirestore
    private lateinit var listGenerMovie: ArrayList<GenerMovie>
    private lateinit var listCasts: ArrayList<Cast>
    private lateinit var adapterCasts: ItemCastsAdapter
    private var movieListener: ListenerRegistration? = null
    private lateinit var movieId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DetailsMovieLayoutBinding.inflate(layoutInflater)
        initialize()
        setEvent()
        setContentView(binding.root)
    }

    private fun initialize() {
        movieId = intent?.getStringExtra("movie_id") ?: run {
            Toast.makeText(this, "Movie ID is missing!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        listCasts = ArrayList()
        listGenerMovie = ArrayList()
        binding = DetailsMovieLayoutBinding.inflate(layoutInflater)
        db = Firebase.firestore
    }

    private fun setEvent() {
        binding.apply {
            btnBookNow.setOnClickListener {
                val intent = Intent(this@DetailsMovieActivity, ChooseDateAndTimeActivity::class.java)
                intent.putExtra("movie_id", movieId)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun setAdapterMovie(list: List<String>) {
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rcvPhoto.layoutManager = layoutManager
        adapterPhotoMovie = ItemPhotosApdater(list)
        binding.rcvPhoto.adapter = adapterPhotoMovie
    }

    private fun setAdapterCast(list: List<Cast>) {
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rcvCasts.layoutManager = layoutManager
        adapterCasts = ItemCastsAdapter(list)
        binding.rcvCasts.adapter = adapterCasts
    }

    override fun onStart() {
        super.onStart()
        val generMap = mutableMapOf<String, String>()
        val castMap = mutableMapOf<String, Cast>()
        listGenerMovie.clear()
        listCasts.clear()

        db.collection("gener").get().addOnSuccessListener { genreSnapshot ->
            for (doc in genreSnapshot) {
                val genreId = doc.id
                val name = doc.getString("name") ?: ""
                generMap[genreId] = name
                if (name.isNotEmpty()) {
                    listGenerMovie.add(GenerMovie(genreId, name))
                }
            }

            db.collection("cast").get().addOnSuccessListener { castSnapshot ->
                for (doc in castSnapshot) {
                    val castId = doc.id
                    val name = doc.getString("name") ?: ""
                    val avatar = doc.getString("avatar") ?: ""
                    if (name.isNotEmpty()) {
                        val cast = Cast(castId, avatar, name)
                        listCasts.add(cast)
                        castMap[castId] = cast
                    }
                }
                movieListener = db.collection("movie").document(movieId)
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            Log.w("MovieRealtime", "Listen failed.", e)
                            return@addSnapshotListener
                        }

                        if (snapshot != null && snapshot.exists()) {
                            val data = snapshot.data
                            val imgMovie = data?.get("img_movie") as? String
                            val trailer = data?.get("trailer") as? String
                            val listPhotos = data?.get("list_photos") as? List<String> ?: emptyList()
                            val castIds = data?.get("list_casts") as? List<String> ?: emptyList()
                            val genreIds = data?.get("gener_movie") as? List<String> ?: emptyList()
                            val duration = data?.get("druation") as? Number ?: "Unknown duration"

                            binding.tvTitleMovie.text = data?.get("title") as? String ?: "No title"
                            binding.tvDuration.text = "$duration minutes"
                            binding.tvDirector.text = data?.get("director") as? String ?: "Unknown director"
                            binding.tvSynopsis.text = data?.get("synopsis") as? String ?: "No synopsis"

                            val genreNames = genreIds.mapNotNull { generMap[it] }
                            binding.tvGener.text = genreNames.joinToString(", ")

                            val selectedCasts = castIds.mapNotNull { castMap[it] }
                            setAdapterCast(selectedCasts)

                            if (!imgMovie.isNullOrEmpty()) {
                                Glide.with(this@DetailsMovieActivity)
                                    .load(imgMovie)
                                    .into(binding.imgMovie)
                            }

                            if (!trailer.isNullOrEmpty()) {
                                val videoUri = Uri.parse(trailer)
                                binding.vdvTrailer.setVideoURI(videoUri)
                                binding.vdvTrailer.start()
                            }

                            setAdapterMovie(listPhotos)
                        }
                    }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        movieListener?.remove()
        movieListener = null
    }
}
