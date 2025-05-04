package com.example.movieticketsapp.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
        setAdapterGenerMovie()
        setAdapterMovie()
        listenToGenerMovieCollectionRealtime()
        listenToMovieCollectionRealtime()
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
        adapterGenerMovie = ItemGenerMovieAdapter(listGenerMovie)
        binding.rcvGenerMovie.adapter = adapterGenerMovie
    }

    private fun listenToMovieCollectionRealtime() {
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

                        if (!imgMovie.isNullOrEmpty() && !title.isNullOrEmpty()) {
                            listMovie.add(
                                Movie(
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
    private fun listenToGenerMovieCollectionRealtime() {
        db.collection("gener")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("GenerMovieRealtime", "Listen failed.", e)
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    listGenerMovie.clear()
                    for (doc in snapshots) {
                        val data = doc.data
                        val name = data["name"] as? String
                        if (!name.isNullOrEmpty()) {
                            listGenerMovie.add(GenerMovie(name))
                            adapterGenerMovie.notifyDataSetChanged()
                        }
                    }
                }
            }
    }
//    private fun listenToGenerMovieCollectionRealtime() {
//        val movieId = "LUBKF4fj8IZSh24HdNTp"
//        val generMovieRef = db.collection("movie").document(movieId).collection("gener_movie")
//
//        generMovieRef.addSnapshotListener { querySnapshot, error ->
//            if (error != null) {
//                Log.e("FIRESTORE", "Error listening to gener_movie", error)
//                return@addSnapshotListener
//            }
//
//            if (querySnapshot != null) {
//                for (doc in querySnapshot.documents) {
//                    val genreId = doc.id
//                    db.collection("gener").document(genreId)
//                        .addSnapshotListener { genreSnapshot, genreError ->
//                            if (genreError != null) {
//                                Log.e("GENRE_ERROR", "Error listening to genre $genreId", genreError)
//                            }
//                            if (genreSnapshot != null && genreSnapshot.exists()) {
//                                val genreData = genreSnapshot.data
//                                val name = genreData?.get("name") as? String
//                                if (!name.isNullOrEmpty()) {
//                                    listGenerMovie.add(GenerMovie(name))
//                                    adapterGenerMovie.notifyDataSetChanged()
//                                }
//                            }
//                        }
//                }
//            }
//        }
}