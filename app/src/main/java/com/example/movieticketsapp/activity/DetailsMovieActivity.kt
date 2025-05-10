package com.example.movieticketsapp.activity

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.movieticketsapp.adapter.ItemPhotosApdater
import com.example.movieticketsapp.databinding.DetailsMovieLayoutBinding
import com.example.movieticketsapp.model.GenerMovie
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class DetailsMovieActivity : AppCompatActivity() {
    private lateinit var binding: DetailsMovieLayoutBinding
    private lateinit var adapterPhotoMovie: ItemPhotosApdater
    private lateinit var db: FirebaseFirestore
    private lateinit var listGenerMovie: ArrayList<GenerMovie>
    private var movieListener: ListenerRegistration? = null
    private lateinit var movieId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Nhận movieId từ Intent
        movieId = intent?.getStringExtra("movie_id") ?: run {
            Toast.makeText(this, "Movie ID is missing!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        listGenerMovie = ArrayList()
        binding = DetailsMovieLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = Firebase.firestore
    }

    private fun setAdapterMovie(list: List<String>) {
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rcvPhoto.layoutManager = layoutManager
        adapterPhotoMovie = ItemPhotosApdater(list)
        binding.rcvPhoto.adapter = adapterPhotoMovie
    }

    override fun onStart() {
        super.onStart()
        val genreMap = mutableMapOf<String, String>()
        listGenerMovie.clear()

        db.collection("gener")
            .get()
            .addOnSuccessListener { genreSnapshot ->
                for (doc in genreSnapshot) {
                    val genreId = doc.id
                    val name = doc.getString("name") ?: ""
                    genreMap[genreId] = name
                    if (name.isNotEmpty()) {
                        listGenerMovie.add(GenerMovie(doc.id, name))
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
                            val listPhotos =
                                data?.get("list_photos") as? List<String> ?: emptyList()
                            val genreIds  = data?.get("gener_movie") as? List<String> ?: emptyList()
                            val duration = data?.get("druation") as? Number ?: "Unknown duration"
                            Log.d("Druation", "Duration: $duration")
                            binding.tvTitleMovie.text = data?.get("title") as? String ?: "No title"
                            binding.tvDuration.text = "$duration minutes"
                            binding.tvDirector.text =
                                data?.get("director") as? String ?: "Unknown director"
                            binding.tvSynopsis.text =
                                data?.get("synopsis") as? String ?: "No synopsis"

                            val genreNames = genreIds.mapNotNull { genreMap[it] }
                            binding.tvGener.text = genreNames.joinToString(", ")
                            if (!imgMovie.isNullOrEmpty()) {
                                Glide.with(this)
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

    override fun onStop() {
        super.onStop()
        movieListener?.remove()
        movieListener = null
    }
}
