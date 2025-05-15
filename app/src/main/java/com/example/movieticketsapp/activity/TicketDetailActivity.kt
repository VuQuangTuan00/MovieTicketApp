package com.example.movieticketsapp.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.movieticketsapp.R
import com.example.movieticketsapp.databinding.TicketDetailLayoutBinding
import com.example.movieticketsapp.model.GenerMovie
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class TicketDetailActivity : AppCompatActivity() {
    private lateinit var binding: TicketDetailLayoutBinding
    private val db = FirebaseFirestore.getInstance()
    private var movieListener: ListenerRegistration? = null
    private lateinit var movieId:String
    private lateinit var ticketId:String
    private lateinit var standard: String
    private lateinit var totalAmounts: String
    private lateinit var conversionFee: String
    private lateinit var listGenerMovie: ArrayList<GenerMovie>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listGenerMovie = ArrayList()
        binding = TicketDetailLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
         ticketId = intent.getStringExtra("TICKET_ID").toString()
         movieId = intent.getStringExtra("MOVIE_ID").toString()
        fetchTicketDetail()
        binding.btnPay.setOnClickListener{
            cancelPayment()
        }
    }

    private fun cancelPayment() {
        db
            .collection("tickets_movie")
            .document(ticketId)
            .delete()
            .addOnSuccessListener {
                finish()
                Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    override fun onStart() {
        super.onStart()
        listGenerMovie.clear()
        val generMap = mutableMapOf<String, String>()
        db.collection("gener").get().addOnSuccessListener { genreSnapshot ->
            for (doc in genreSnapshot) {
                val genreId = doc.id
                val name = doc.getString("name") ?: ""
                generMap[genreId] = name
                if (name.isNotEmpty()) {
                    listGenerMovie.add(GenerMovie(genreId, name))
                }
            }
            fetchMovieDetail(generMap)
        }
    }

    private fun fetchMovieDetail(generMap: Map<String, String>) {
        movieListener = db.collection("movie").document(movieId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("MovieRealtime", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val data = snapshot.data
                    val imgMovie = data?.get("img_movie") as? String
                    val genreIds = data?.get("gener_movie") as? List<String> ?: emptyList()
                    val duration = data?.get("druation") as? Number ?: "Unknown duration"

                    binding.tvTitleMovie.text = data?.get("title") as? String ?: "No title"
                    binding.tvDuration.text = "$duration minutes"
                    binding.tvDirector.text = data?.get("director") as? String ?: "Unknown director"
                    val genreNames = genreIds.mapNotNull { generMap[it] }
                    binding.tvGener.text = genreNames.joinToString(", ")

                    if (!imgMovie.isNullOrEmpty()) {
                        Glide.with(this@TicketDetailActivity)
                            .load(imgMovie)
                            .into(binding.imgMovie)
                    }
                }
            }
    }
    private fun fetchTicketDetail() {
        db.collection("tickets_movie")
            .document(ticketId)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    Toast.makeText(this, "Không tìm thấy vé", Toast.LENGTH_SHORT).show()
                    finish()
                    return@addOnSuccessListener
                }
                // Lấy các giá trị
                val date          = doc.getString("date").orEmpty()
                val hour          = doc.getString("hour").orEmpty()
                val seatIds       = doc.getString("seatIds").orEmpty()
                standard          = doc.getString("standard").orEmpty()
                conversionFee     = doc.getString("conversionFee").orEmpty()
                totalAmounts      = doc.getString("totalAmounts").orEmpty()

                // Bơm lên UI
                binding.tvDate.text       = date
                binding.tvHours.text       = hour
                binding.tvSeat.text          = seatIds
                binding.tvStandard.text         = standard
                binding.tvConversionFree.text   = conversionFee
                binding.tvActualPay.text        = totalAmounts
            }
            .addOnFailureListener { e ->
                Log.e("TicketDetail", "Lỗi lấy chi tiết vé", e)
                Toast.makeText(
                    this,
                    "Error loading ticket details: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}