package com.example.movieticketsapp.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.movieticketsapp.adapter.ItemSeatAdminAdapter
import com.example.movieticketsapp.databinding.AdminSeatManagerLayoutBinding
import com.example.movieticketsapp.model.Seat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class AdminSeatManagerActivity : AppCompatActivity() {

    private lateinit var binding: AdminSeatManagerLayoutBinding
    private val db = FirebaseFirestore.getInstance()
    private var seatListener: ListenerRegistration? = null

    private val seatList = mutableListOf<Seat>()

    private var showtimeId: String? = null
    private var timelineId: String? = null

    private lateinit var adapter: ItemSeatAdminAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AdminSeatManagerLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Nhận dữ liệu showtimeId và timelineId từ Intent
        showtimeId = intent.getStringExtra("showtimeId")
        timelineId = intent.getStringExtra("timelineId")

        if (showtimeId == null || timelineId == null) {
            Toast.makeText(this, "Missing showtimeId or timelineId!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setSupportActionBar(binding.appbar.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "SeatLists"
        }
        binding.appbar.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        setupRecyclerView()
        fetchSeatsRealtime(showtimeId!!, timelineId!!)
    }

    private fun setupRecyclerView() {
        adapter = ItemSeatAdminAdapter(seatList) { seat ->
            toggleSeatStatus(seat)
        }
        binding.rcvSeats.layoutManager = GridLayoutManager(this, 8)
        binding.rcvSeats.adapter = adapter
    }

    private fun fetchSeatsRealtime(showtimeId: String, timelineId: String) {
        seatListener?.remove()
        seatListener = db.collection("showtimes")
            .document(showtimeId)
            .collection("timelines")
            .document(timelineId)
            .collection("seats")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e("AdminSeatManager", "Listen failed.", error)
                    return@addSnapshotListener
                }

                if (snapshots == null || snapshots.isEmpty) {
                    seatList.clear()
                    adapter.notifyDataSetChanged()
                    return@addSnapshotListener
                }

                seatList.clear()
                for (doc in snapshots.documents) {
                    val id = doc.id
                    val seatCode = doc.getString("code") ?: ""
                    val row = doc.getString("row") ?: ""
                    val col = doc.getLong("column")?.toInt() ?: 0
                    val statusStr = doc.getString("status") ?: "AVAILABLE"
//                    val status = try {
//                        Seat.Status.valueOf(statusStr)
//                    } catch (e: Exception) {
//                        Seat.Status.AVAILABLE
//                    }

                    val seat = Seat(id, seatCode, row, col, statusStr)
                    seatList.add(seat)
                }

                seatList.sortWith(compareBy({ it.row }, { it.col }))

                adapter.notifyDataSetChanged()
            }
    }

    private fun toggleSeatStatus(seat: Seat) {
        val newStatus = when (seat.status) {
           "AVAILABLE"-> Seat.Status.UNAVAILABLE
           "UNAVAILABLE" -> Seat.Status.SELECTED
           "SELECTED" -> Seat.Status.AVAILABLE
            else -> {Log.d("AdminSeatManager", "Unknown status: ${seat.status}")}
        }

        val seatRef = db.collection("showtimes")
            .document(showtimeId!!)
            .collection("timelines")
            .document(timelineId!!)
            .collection("seats")
            .document(seat.id)

        seatRef.update("status", newStatus)
            .addOnSuccessListener {
                Toast.makeText(this, "Ghế ${seat.seatCode} được cập nhật thành $newStatus", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Cập nhật thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        seatListener?.remove()
    }
}
