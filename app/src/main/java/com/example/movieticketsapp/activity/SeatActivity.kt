package com.example.movieticketsapp.activity

import android.annotation.SuppressLint
import android.app.ComponentCaller
import android.content.Intent
import android.icu.text.DecimalFormat
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.movieticketsapp.Api.CreateOrder
import com.example.movieticketsapp.adapter.ItemSeatAdapter
import com.example.movieticketsapp.databinding.SeatLayoutBinding
import com.example.movieticketsapp.model.Seat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import vn.zalopay.sdk.Environment
import vn.zalopay.sdk.ZaloPaySDK

class SeatActivity : AppCompatActivity() {
    private lateinit var binding: SeatLayoutBinding
    private var price: Double = 0.0
    private var number: Int = 0
    private var totalPrice: Double = 0.0
    private val db = FirebaseFirestore.getInstance()
    private var selectedDate: String? = null
    private var selectedTime: String? = null
    private var showtimeId: String? = null
    private var timelineId: String? = null
    private var movieId: String? = null

    private lateinit var seat: Seat
    private var selectedSeats = arrayListOf<String>()
    private var seatListener: ListenerRegistration? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SeatLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getIntentExtra()
        setEvent()
        fetchSeatsRealtime(showtimeId!!, timelineId!!)

    }
    private fun fetchSeatsRealtime(showtimeId: String, timelineId: String) {
        seatListener?.remove()
        seatListener = db.collection("showtimes")
            .document(showtimeId)
            .collection("timelines")
            .document(timelineId)
            .collection("seats")
            .addSnapshotListener { seatSnapshots, e ->
                if (e != null) {
                    Log.e("SEAT", "Lỗi realtime: ", e)
                    return@addSnapshotListener
                }

                if (seatSnapshots == null || seatSnapshots.isEmpty) {
                    binding.rcvSeat.adapter = null
                    return@addSnapshotListener
                }

                val seatList = mutableListOf<Seat>()
                for (doc in seatSnapshots) {
                    val seat = Seat(
                        id = doc.id,
                        seatCode = doc.getString("code") ?: "",
                        row = doc.getString("row") ?: "",
                        col = doc.getLong("column")?.toInt() ?: 0,
                        status = doc.getString("status") ?: "AVAILABLE",
                    )
                    seatList.add(seat)
                }

                val sortedSeats = seatList.sortedWith(compareBy({ it.row }, { it.col }))

                binding.rcvSeat.layoutManager = GridLayoutManager(this, 8)
                binding.rcvSeat.adapter = ItemSeatAdapter(sortedSeats, object : ItemSeatAdapter.SelectedSeat {
                    @SuppressLint("SetTextI18n")
                    override fun onSelectedSeat(selectedNames: ArrayList<String>, num: Int) {
                        number = num
                        selectedSeats = selectedNames
                        totalPrice = (num * price * 100).toInt() / 100.0
                        binding.tvNumberSelected.text = "Đã chọn: ${selectedSeats.joinToString()} ($num)"
                        binding.tvPrice.text = "$" + String.format("%.2f", totalPrice)
                    }
                })
            }
    }

    private fun setEvent() {
        seat = Seat("", "", "", 0,"")
        binding.imgBack.setOnClickListener {
            finish()
        }
        binding.btnContinue.setOnClickListener {
            val intent = Intent(this, PaymentActivity::class.java)
            intent.putExtra("selectedDate", selectedDate)
            intent.putExtra("selectedTime", selectedTime)
            intent.putExtra("showtimeId", showtimeId)
            intent.putExtra("timelineId", timelineId)
            intent.putExtra("movie_id", movieId)
            intent.putStringArrayListExtra("seat", selectedSeats)
            intent.putExtra("price", totalPrice)
            startActivity(intent)
        }
    }

    private fun getIntentExtra() {
        selectedDate = intent?.getStringExtra("selectedDate") ?: run {
            Toast.makeText(this, "Date is missing!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        selectedTime = intent?.getStringExtra("selectedTime") ?: run {
            Toast.makeText(this, "Time is missing!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        showtimeId = intent?.getStringExtra("showtimeId") ?: run {
            Toast.makeText(this, "Showtime ID is missing!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        timelineId = intent?.getStringExtra("timelineId") ?: run {
            Toast.makeText(this, "Timeline ID is missing!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        movieId = intent?.getStringExtra("movie_id") ?: run {
            Toast.makeText(this, "Movie ID is missing!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        price = intent?.getDoubleExtra("price", 0.0) ?: run  {
            Toast.makeText(this, "Price is missing!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        seatListener?.remove()
    }
}
