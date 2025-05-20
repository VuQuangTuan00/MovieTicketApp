package com.example.movieticketsapp.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.movieticketsapp.R
import com.example.movieticketsapp.adapter.ItemMovieAdapter
import com.example.movieticketsapp.databinding.HomaPageLayoutBinding
import com.example.movieticketsapp.model.Movie
import com.example.movieticketsapp.utils.navigateTo
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class HomaPageActivity : AppCompatActivity() {
    private lateinit var binding: HomaPageLayoutBinding
    private lateinit var adapterMovie: ItemMovieAdapter
    private lateinit var listMovie: ArrayList<Movie>
    private lateinit var imgList: ArrayList<SlideModel>
    private lateinit var movieIdList: ArrayList<String>
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = HomaPageLayoutBinding.inflate(layoutInflater)
        initialize()
        setContentView(binding.root)
        setEvent()
        setAdapterMovie()
        listenToMovieCollectionRealtime()
        listenToImgMovieCollectionRealtime()
    }

    private fun setAdapterMovie() {
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rcvMovie.layoutManager = layoutManager
        adapterMovie = ItemMovieAdapter(listMovie){event ->
            val intent = Intent(this, DetailsMovieActivity::class.java)
            intent.putExtra("movie_id", event.id)
            startActivity(intent)
        }
        binding.rcvMovie.adapter = adapterMovie
    }


    private fun initialize() {
        imgList = ArrayList()
        listMovie = ArrayList()
        movieIdList = ArrayList()
        db = Firebase.firestore
        db = FirebaseFirestore.getInstance()
    }

    private fun setEvent() {
        binding.apply {
            tvViewAllNowPlaying.setOnClickListener {
                navigateTo(ViewAllMovie::class.java, flag = false)
            }
            binding.bottomnvg.setOnItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.ticket -> {
                      navigateTo(ListTicketActivity::class.java, flag = false)
                        true
                    }
                    R.id.food_order ->{
                        navigateTo(FoodActivity::class.java, flag = false)
                        true
                    }
                    else -> false
                }
            }
        }
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
                if (snapshots != null) {
                    imgList.clear()
                    for (doc in snapshots) {
                        val data = doc.data
                        val img = data["img"] as? String
                        if (!img.isNullOrEmpty()) {
                            imgList.add(SlideModel(img, ScaleTypes.FIT))
                            binding.imgSlider.setImageList(imgList, ScaleTypes.FIT)
                        }
                    }
                }
            }
    }
}