package com.example.movieticketsapp.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movieticketsapp.R
import com.example.movieticketsapp.adapter.ItemDateAdapter
import com.example.movieticketsapp.adapter.ItemTimeAdapter
import com.example.movieticketsapp.databinding.ChooseDateAndTimeLayoutBinding
import com.example.movieticketsapp.databinding.DetailsMovieLayoutBinding
import com.example.movieticketsapp.utils.navigateTo
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ChooseDateAndTimeActivity : AppCompatActivity() {
    private lateinit var binding: ChooseDateAndTimeLayoutBinding
    private val db = Firebase.firestore
    private lateinit var movieId: String
    private var showtimeStartDate: LocalDate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ChooseDateAndTimeLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        movieId = intent?.getStringExtra("movie_id") ?: run {
            Toast.makeText(this, "Movie ID is missing!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setEvent()
        fetchShowtimeStartDate(movieId)
    }

    private fun setEvent() {
        binding.imgBack.setOnClickListener { finish() }
        binding.btnContinue.setOnClickListener {
            navigateTo(SeatActivity::class.java, flag = false)
        }
    }

    private fun setupDateAdapter() {
        val dates = generateDates()

        binding.rcvDate.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rcvDate.adapter = ItemDateAdapter(dates) { selectedInternalDate ->
            val selectedDate = LocalDate.parse(selectedInternalDate)
            if (showtimeStartDate != null && selectedDate.isBefore(showtimeStartDate)) {
                binding.rcvTime.adapter = null
                Toast.makeText(
                    this,
                    "Không có suất chiếu trước ngày công chiếu",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                fetchTimelinesByMovieId(movieId, selectedDate)
            }
        }
    }

    private fun generateDates(): List<Pair<String, String>> {
        val result = mutableListOf<Pair<String, String>>()
        val today = LocalDate.now()
        val displayFormat = SimpleDateFormat("EEE/dd/MMM", Locale("vi", "VN"))
        val internalFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        for (i in 0 until 7) {
            val date = today.plusDays(i.toLong())
            val display = displayFormat.format(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()))
            val internal = date.format(internalFormat)
            result.add(Pair(display, internal))
        }
        return result
    }

    private fun fetchShowtimeStartDate(movieId: String) {
        db.collection("showtimes")
            .whereEqualTo("movie_id", movieId)
            .get()
            .addOnSuccessListener { documents ->
                val firstShowtime = documents.firstOrNull()
                if (firstShowtime != null) {
                    val startTimestamp = firstShowtime.getTimestamp("start_time")
                    showtimeStartDate = startTimestamp?.toDate()?.toInstant()
                        ?.atZone(ZoneId.systemDefault())
                        ?.toLocalDate()
                }
                setupDateAdapter() // Gọi sau khi lấy được start date
            }
            .addOnFailureListener {
                Toast.makeText(this, "Không thể tải dữ liệu suất chiếu", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchTimelinesByMovieId(movieId: String, selectedDate: LocalDate) {
        db.collection("showtimes")
            .whereEqualTo("movie_id", movieId)
            .get()
            .addOnSuccessListener { showtimeDocs ->
                if (showtimeDocs.isEmpty) return@addOnSuccessListener

                val allTimelineItems = mutableListOf<Pair<String, String>>()
                val formatter = DateTimeFormatter.ofPattern("hh:mm a")

                for (showtimeDoc in showtimeDocs) {
                    val showtimeId = showtimeDoc.id

                    db.collection("showtimes")
                        .document(showtimeId)
                        .collection("timelines")
                        .get()
                        .addOnSuccessListener { timelineDocs ->
                            allTimelineItems.clear()
                            for (doc in timelineDocs) {
                                val time = doc.getTimestamp("time") ?: continue
                                val localTime = time.toDate().toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalTime()
                                val formatted = localTime.format(formatter)
                                allTimelineItems.add(doc.id to formatted)
                            }

                            binding.rcvTime.layoutManager = LinearLayoutManager(
                                this,
                                LinearLayoutManager.HORIZONTAL,
                                false
                            )
                            binding.rcvTime.adapter = ItemTimeAdapter(allTimelineItems.map { it.second }) { index ->
                                val selectedTimelineId = allTimelineItems[index].first
                                val selectedTime = allTimelineItems[index].second
                                Log.d("Selected", "Timeline ID: $selectedTimelineId, Time: $selectedTime")
                            }
                        }
                }
            }
    }
}