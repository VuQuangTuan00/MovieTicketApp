package com.example.movieticketsapp.activity.User

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.movieticketsapp.databinding.TicketDetailLayoutBinding
import com.example.movieticketsapp.model.Cinema
import com.example.movieticketsapp.model.GenerMovie
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class TicketDetailActivity : AppCompatActivity() {
    private lateinit var binding: TicketDetailLayoutBinding
    private val db = FirebaseFirestore.getInstance()
    private var movieListener: ListenerRegistration? = null
    private val movieId: String by lazy { intent.getStringExtra("MOVIE_ID").orEmpty() }
    private val ticketId: String by lazy { intent.getStringExtra("TICKET_ID").orEmpty() }
    private val userId  = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private lateinit var listGenerMovie: ArrayList<GenerMovie>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listGenerMovie = ArrayList()
        binding = TicketDetailLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnPay.setOnClickListener{
            cancelPayment()
        }
        binding.imgBack.setOnClickListener {
            finish()
        }
        listenToTicketDetailRealtime()
    }
    private fun fetchLocation() {
        var locationCinema = Cinema("", "", "", "")
        db.collection("cinema")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("Firestore", "Error fetching location_cinema", e)
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    for (doc in snapshots) {
                        val cinemaName = doc.getString("cinema_name") ?: "Không có thông tin"
                        locationCinema = Cinema(cinemaName, "", "phone", "")
                    }
                }
                binding.tvCinema.text = locationCinema.cinemaName
            }
    }
    private fun cancelPayment() {
        db.collection("users").document(userId).collection("tickets").document(ticketId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                finish()
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
    private fun listenToTicketDetailRealtime() {
        movieListener = db.collection("users")
            .document(userId)
            .collection("tickets")
            .document(ticketId)

            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(this, "Lỗi khi cập nhật vé", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val date = snapshot.getString("date").orEmpty()
                    val hour = snapshot.getString("hour").orEmpty()
                    val seatIds = snapshot.get("seatIds") as? List<String> ?: emptyList()
                    val standard = snapshot.getDouble("standard")
                    val conversionFee = snapshot.getDouble("conversionFee")
                    val totalAmounts = snapshot.getDouble("totalAmounts")

                    binding.tvDate.text = date
                    binding.tvHours.text = hour
                    binding.tvSeat.text = seatIds.joinToString ( ", " )
                    binding.tvStandard.text = standard.toString()
                    binding.tvConversionFree.text = conversionFee.toString()
                    binding.tvActualPay.text = totalAmounts.toString()
                }
            }
    }

    override fun onStop() {
        super.onStop()
        movieListener?.remove()
    }
}