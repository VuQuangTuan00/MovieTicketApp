package com.example.movieticketsapp.activity

import android.annotation.SuppressLint
import android.icu.text.DecimalFormat
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.movieticketsapp.adapter.ItemSeatAdapter
import com.example.movieticketsapp.databinding.SeatLayoutBinding
import com.example.movieticketsapp.model.Seat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class SeatActivity : AppCompatActivity() {
    private lateinit var binding: SeatLayoutBinding
    private var price: Double = 0.0
    private var number: Int = 0
    private val db = FirebaseFirestore.getInstance()
    private var selectedDate: String? = null
    private var selectedTime: String? = null
    private var showtimeId: String? = null
    private var timelineId: String? = null
    private var seatListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SeatLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getIntentExtra()
        setEvent()
        fetchSeatsRealtime(showtimeId!!, timelineId!!)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
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
                        code = doc.getString("code") ?: "",
                        status = doc.getString("status") ?: "AVAILABLE",
                        row = doc.getString("row") ?: "",
                        column = doc.getLong("column")?.toInt() ?: 0
                    )
                    seatList.add(seat)
                }

                val sortedSeats = seatList.sortedWith(compareBy({ it.row }, { it.column }))

                binding.rcvSeat.layoutManager = GridLayoutManager(this, 8)
                binding.rcvSeat.adapter = ItemSeatAdapter(sortedSeats, object : ItemSeatAdapter.SelectedSeat {
                    @SuppressLint("SetTextI18n")
                    override fun onSelectedSeat(selectedNme: String, num: Int) {
                        number = num
                        price = DecimalFormat("#.##").format(num * 1.0).toDouble()
                        binding.tvNumberSelected.text = "Đã chọn: $selectedNme ($num)"
                        binding.tvPrice.text = "$$price"
                    }
                })
            }
    }

    private fun setEvent() {
        binding.imgBack.setOnClickListener {
            finish()
        }
        binding.btnContinue.setOnClickListener {
//            val intent = intent(this, PaymentActivity::class.java)
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
    }

    override fun onDestroy() {
        super.onDestroy()
        seatListener?.remove()
    }
}
