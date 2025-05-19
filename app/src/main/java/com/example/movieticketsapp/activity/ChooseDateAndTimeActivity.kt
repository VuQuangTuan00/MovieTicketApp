package com.example.movieticketsapp.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movieticketsapp.adapter.ItemDateAdapter
import com.example.movieticketsapp.adapter.ItemTimeAdapter
import com.example.movieticketsapp.databinding.ChooseDateAndTimeLayoutBinding
import com.example.movieticketsapp.model.Cinema
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ChooseDateAndTimeActivity : AppCompatActivity() {
    private lateinit var binding: ChooseDateAndTimeLayoutBinding
    private var selectedD: String? = null
    private var selectedT: String? = null
    private val db = Firebase.firestore
    private lateinit var movieId: String
    private var showtimeStartDate: LocalDate? = null
    private var timelineListener = mutableListOf<ListenerRegistration>()
    private var showtimeId = ""
    private var timelineId = ""
    private var price: Double = 0.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ChooseDateAndTimeLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        movieId = intent?.getStringExtra("movie_id") ?: run {
            Toast.makeText(this, "Movie ID is missing!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        fetchLocation()
        setEvent()
        fetchShowtimeStartDate(movieId)
    }

    private fun setEvent() {
        binding.imgBack.setOnClickListener { finish() }
        binding.btnContinue.setOnClickListener {
            if (selectedD == null) {
                Toast.makeText(this@ChooseDateAndTimeActivity, "Vui lòng chọn ngày", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedT.isNullOrEmpty()) {
                Toast.makeText(this@ChooseDateAndTimeActivity, "Vui lòng chọn thời gian", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(this@ChooseDateAndTimeActivity, SeatActivity::class.java)
            intent.putExtra("selectedDate", selectedD)
            intent.putExtra("selectedTime", selectedT)
            intent.putExtra("showtimeId", showtimeId)
            intent.putExtra("timelineId", timelineId)
            intent.putExtra("movie_id", movieId)
            intent.putExtra("price", price)
            startActivity(intent)
        }
    }

    private fun setupDateAdapter() {
        val dates = generateDates()

        binding.rcvDate.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rcvDate.adapter = ItemDateAdapter(dates) { selectedInternalDate ->
            selectedD = selectedInternalDate
            val selectedDate = LocalDate.parse(selectedD)
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
            val display = displayFormat.format(
                Date.from(
                    date.atStartOfDay(ZoneId.systemDefault()).toInstant()
                )
            )
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
                setupDateAdapter()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Không thể tải dữ liệu suất chiếu", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchTimelinesByMovieId(movieId: String, selectedDate: LocalDate) {
        timelineListener.forEach { it.remove() }
        timelineListener.clear()

        db.collection("showtimes")
            .whereEqualTo("movie_id", movieId)
            .addSnapshotListener { showtimeSnapshots, e ->
                if (e != null || showtimeSnapshots == null) {
                    Log.e("Timeline", "Error fetching showtimes", e)
                    binding.rcvTime.adapter = null
                    selectedT = null
                    return@addSnapshotListener
                }

                val formatter = DateTimeFormatter.ofPattern("hh:mm a")
                val allTimelineItems = mutableListOf<Pair<String, String>>()
                var timelinesPending = 0
                var timelinesCollected = 0

                for (showtimeDoc in showtimeSnapshots) {
                     showtimeId = showtimeDoc.id
                    val startTimeTimestamp = showtimeDoc.getTimestamp("start_time") ?: continue
                    price = showtimeDoc.getDouble("price") ?: continue
                    binding.tvStandard.text = "$$price"
                    val startDate = startTimeTimestamp.toDate().toInstant()
                        .atZone(TimeZone.getDefault().toZoneId())
                        .toLocalDate()

                    if (selectedDate.isBefore(startDate)) continue

                    timelinesPending++

                    val listener = db.collection("showtimes")
                        .document(showtimeId)
                        .collection("timelines")
                        .addSnapshotListener { timelineSnapshots, error ->
                            if (error != null || timelineSnapshots == null) {
                                Log.e("Timeline", "Error listening to timelines", error)
                            } else {
                                for (doc in timelineSnapshots) {
                                    timelineId = doc.id
                                    val timestamp = doc.getTimestamp("time") ?: continue
                                    val localTime = timestamp.toDate().toInstant()
                                        .atZone(TimeZone.getDefault().toZoneId())
                                        .toLocalTime()
                                    val timeFormatted = localTime.format(formatter)
                                    allTimelineItems.add(timelineId to timeFormatted)
                                }
                            }

                            timelinesCollected++
                            if (timelinesCollected == timelinesPending) {
                                allTimelineItems.sortBy { it.second }

                                if (allTimelineItems.isEmpty()) {
                                    binding.rcvTime.adapter = null
                                    selectedT = null
                                    return@addSnapshotListener
                                }

                                binding.rcvTime.layoutManager = LinearLayoutManager(
                                    this, LinearLayoutManager.HORIZONTAL, false
                                )
                                binding.rcvTime.adapter = ItemTimeAdapter(
                                    allTimelineItems.map { it.second }
                                ) { index ->
                                    selectedT = allTimelineItems[index].second
                                }
                            }
                        }

                    timelineListener.add(listener)
                }

                if (timelinesPending == 0) {
                    binding.rcvTime.adapter = null
                    selectedT = null
                }
            }
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
                        val address = doc.getString("address") ?: "Không có địa chỉ"
                        val cinemaName = doc.getString("cinema_name") ?: "Không có thông tin"
                        val phone = doc.getString("phone") ?: "Không có số điện thoại"
                        val locationMap = doc.getString("location_map") ?: "Không có bản đồ"
                        locationCinema = Cinema(cinemaName, address, phone, locationMap)
                    }
                }
                binding.tvCinemaName.text = locationCinema.cinemaName
                binding.tvAddress.text = locationCinema.address
                binding.tvPhone.text = locationCinema.phone
            }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        timelineListener.forEach { it.remove() }
        timelineListener.clear()
    }
}