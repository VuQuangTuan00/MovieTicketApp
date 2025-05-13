package com.example.movieticketsapp.activity

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movieticketsapp.adapter.ItemPhotosApdater
import com.example.movieticketsapp.databinding.DetailsMovieLayoutBinding
import com.example.movieticketsapp.databinding.ItemReviewBinding
import com.example.movieticketsapp.model.GenerMovie
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetailsMovieActivity : AppCompatActivity() {
    private lateinit var binding: DetailsMovieLayoutBinding
    private lateinit var adapterPhotoMovie: ItemPhotosApdater
    private val db: FirebaseFirestore = Firebase.firestore
    private val listGenerMovie = ArrayList<GenerMovie>()
    private var movieListener: ListenerRegistration? = null
    private lateinit var movieId: String

    // --- cho phần Reviews ---
    private lateinit var reviewAdapter: ReviewAdapter
    private val reviewList = mutableListOf<Review>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        movieId = intent?.getStringExtra("movie_id") ?: run {
            Toast.makeText(this, "Movie ID is missing!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding = DetailsMovieLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup RecyclerView ảnh
        setAdapterMovie(emptyList())

        // Setup RecyclerView reviews
        binding.rcvReviews.layoutManager = LinearLayoutManager(this)
        reviewAdapter = ReviewAdapter(reviewList)
        binding.rcvReviews.adapter = reviewAdapter
    }

    private fun setAdapterMovie(list: List<String>) {
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rcvPhoto.layoutManager = layoutManager
        adapterPhotoMovie = ItemPhotosApdater(list)
        binding.rcvPhoto.adapter = adapterPhotoMovie
    }

    override fun onStart() {
        super.onStart()

        // Lấy genres trước
        db.collection("gener")
            .get()
            .addOnSuccessListener { genreSnapshot ->
                genreSnapshot.forEach { doc ->
                    val genreId = doc.id
                    val name = doc.getString("name").orEmpty()
                    if (name.isNotEmpty()) {
                        listGenerMovie.add(GenerMovie(genreId, name))
                    }
                }

                // Lắng nghe realtime movie
                movieListener = db.collection("movie")
                    .document(movieId)
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            Log.w("MovieRealtime", "Listen failed.", e)
                            return@addSnapshotListener
                        }
                        snapshot?.let {
                            val data = it.data.orEmpty()
                            binding.tvTitleMovie.text = data["title"] as? String ?: "No title"
                            binding.tvDuration.text = "${(data["druation"] as? Number ?: 0)} minutes"
                            binding.tvDirector.text = data["director"] as? String ?: "Unknown director"
                            binding.tvSynopsis.text = data["synopsis"] as? String ?: "No synopsis"

                            val genreIds = data["gener_movie"] as? List<String> ?: emptyList()
                            val genreNames = genreIds.mapNotNull { id -> listGenerMovie.find { it.id == id }?.name }
                            binding.tvGener.text = genreNames.joinToString(", ")

                            (data["img_movie"] as? String)?.takeIf { it.isNotEmpty() }?.let { url ->
                                Glide.with(this).load(url).into(binding.imgMovie)
                            }
                            (data["trailer"] as? String)?.takeIf { it.isNotEmpty() }?.let { url ->
                                binding.vdvTrailer.setVideoURI(Uri.parse(url))
                                binding.vdvTrailer.start()
                            }

                            val photos = data["list_photos"] as? List<String> ?: emptyList()
                            setAdapterMovie(photos)
                        }
                    }
            }

        loadReviews()
    }

    override fun onStop() {
        super.onStop()
        movieListener?.remove()
        movieListener = null
    }

     fun loadReviews() {
        db.collection("movie")
            .document(movieId)
            .collection("ratings")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(3)
            .get()
            .addOnSuccessListener { snaps ->
                val items = snaps.documents.mapNotNull { doc ->
                    val email   = doc.getString("userEmail") ?: return@mapNotNull null
                    val score   = doc.getDouble("score")   ?: return@mapNotNull null
                    val comment = doc.getString("comment") ?: ""
                    val ts      = doc.getTimestamp("createdAt")?.toDate()
                    Review(email, score, comment, ts)
                }
                reviewList.apply {
                    clear()
                    addAll(items)
                }
                reviewAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Không tải được reviews.", Toast.LENGTH_SHORT).show()
            }
    }

    data class Review(
        val userEmail: String,
        val score: Double,
        val comment: String,
        val date: Date?
    )

    class ReviewAdapter(
        private val items: List<Review>
    ) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
            val b = ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ReviewViewHolder(b)
        }

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
            holder.bind(items[position])
        }

        class ReviewViewHolder(private val b: ItemReviewBinding) :
            RecyclerView.ViewHolder(b.root) {

            fun bind(r: Review) {
                b.tvUserName.text     = r.userEmail
                val fiveStars = (r.score / 10.0 * b.ratingBarItem.numStars).toFloat()
                b.ratingBarItem.rating = fiveStars
                b.tvRatingValue.text  = String.format(Locale.getDefault(), "%.0f/10", r.score)
                b.tvComment.text      = r.comment
                b.tvDate.text         = r.date?.let {
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
                } ?: ""
            }
        }
    }
}
