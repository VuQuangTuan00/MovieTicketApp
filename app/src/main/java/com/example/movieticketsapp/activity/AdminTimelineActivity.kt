package com.example.movieticketsapp.activity

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movieticketsapp.adapter.ItemTimelineAdapter
import com.example.movieticketsapp.databinding.AdminTimelineLayoutBinding
import com.example.movieticketsapp.databinding.DialogTimelineEditBinding
import com.example.movieticketsapp.model.Timeline
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AdminTimelineActivity : AppCompatActivity() {
    private lateinit var binding: AdminTimelineLayoutBinding
    private lateinit var adapter: ItemTimelineAdapter
    private val timelineList = mutableListOf<Timeline>()
    private val db = FirebaseFirestore.getInstance()
    private var showtimeId: String = ""
    private var showtimeStartTime: Date = Date()
    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AdminTimelineLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showtimeId = intent.getStringExtra("showtimeId") ?: ""
        if (showtimeId.isEmpty()) {
            finish()
            return
        }
        showtimeStartTime = intent.getSerializableExtra("showtimeStartTime") as? Date ?: Date()

        setSupportActionBar(binding.appbar.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Timelines"
        }
        binding.appbar.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        adapter = ItemTimelineAdapter(
            timelineList,
            onEdit = { timeline -> showAddEditDialog(timeline) },
            onDelete = { timeline -> confirmDeleteTimeline(timeline) },
            onClick = { timeline ->
                // Xử lý mở xem ghế timeline
                val intent = Intent(this, AdminSeatManagerActivity::class.java)
                intent.putExtra("showtimeId", showtimeId)
                intent.putExtra("timelineId", timeline.id)
                startActivity(intent)
            }
        )

        binding.fabAddTimeline.setOnClickListener {
            showAddEditDialog(null)  // Thêm timeline mới
        }

        binding.recyclerViewTimelines.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewTimelines.adapter = adapter

        loadTimelines()
    }

    private fun loadTimelines() {
        db.collection("showtimes").document(showtimeId).collection("timelines")
            .get()
            .addOnSuccessListener { result ->
                timelineList.clear()
                val newList = mutableListOf<Timeline>()
                for (doc in result) {
                    val time = doc.getDate("time") ?: return@addOnSuccessListener
                    newList.add(Timeline(id = doc.id, time = time))
                }
                adapter.updateList(newList)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load timelines", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showAddEditDialog(timeline: Timeline?) {
        val dialogBinding = DialogTimelineEditBinding.inflate(layoutInflater)
        val calendar = Calendar.getInstance()

        if (timeline != null) {
            calendar.time = timeline.time
            dialogBinding.etTime.setText(sdf.format(timeline.time))
        } else {
            dialogBinding.etTime.setText(sdf.format(calendar.time))
        }

        // Bắt sự kiện click chọn ngày giờ với kiểm tra thời gian hợp lệ
        dialogBinding.etTime.setOnClickListener {
            DatePickerDialog(this, { _, y, m, d ->
                calendar.set(y, m, d)
                TimePickerDialog(this, { _, h, min ->
                    calendar.set(Calendar.HOUR_OF_DAY, h)
                    calendar.set(Calendar.MINUTE, min)

                    val selectedTime = calendar.time
                    if (selectedTime.after(showtimeStartTime)) {
                        dialogBinding.etTime.setText(sdf.format(selectedTime))
                    } else {
                        Toast.makeText(this, "Thời gian phải lớn hơn thời gian bắt đầu showtime", Toast.LENGTH_LONG).show()
                    }
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(if (timeline == null) "Thêm Timeline" else "Sửa Timeline")
            .setView(dialogBinding.root)
            .setPositiveButton(if (timeline == null) "Thêm" else "Lưu", null)
            .setNegativeButton("Hủy", null)
            .create()

        dialog.setOnShowListener {
            val btn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            btn.setOnClickListener {
                val timeStr = dialogBinding.etTime.text.toString()
                val time = try { sdf.parse(timeStr) } catch (e: Exception) { null }

                if (time == null) {
                    Toast.makeText(this, "Thời gian không hợp lệ", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (!time.after(showtimeStartTime)) {
                    Toast.makeText(this, "Thời gian phải lớn hơn thời gian bắt đầu showtime", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                if (timeline == null) addTimeline(time) else updateTimeline(timeline.id, time)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun addTimeline(time: Date) {
        val newDocRef = db.collection("showtimes").document(showtimeId).collection("timelines").document()
        val data = hashMapOf("time" to time)

        newDocRef.set(data)
            .addOnSuccessListener {
                createDefaultSeats(newDocRef.id)
                Toast.makeText(this, "Thêm timeline thành công", Toast.LENGTH_SHORT).show()
                loadTimelines()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Thêm timeline thất bại", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateTimeline(id: String, time: Date) {
        db.collection("showtimes").document(showtimeId).collection("timelines").document(id)
            .update("time", time)
            .addOnSuccessListener {
                Toast.makeText(this, "Cập nhật timeline thành công", Toast.LENGTH_SHORT).show()
                loadTimelines()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Cập nhật timeline thất bại", Toast.LENGTH_SHORT).show()
            }
    }

    private fun confirmDeleteTimeline(timeline: Timeline) {
        AlertDialog.Builder(this)
            .setTitle("Xóa Timeline")
            .setMessage("Bạn có chắc muốn xóa timeline này?")
            .setPositiveButton("Xóa") { _, _ ->
                deleteTimeline(timeline)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun deleteTimeline(timeline: Timeline) {
        db.collection("showtimes").document(showtimeId).collection("timelines").document(timeline.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Xóa timeline thành công", Toast.LENGTH_SHORT).show()
                loadTimelines()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Xóa timeline thất bại", Toast.LENGTH_SHORT).show()
            }
    }

    private fun createDefaultSeats(timelineId: String) {
        val seatsCollection = db.collection("showtimes")
            .document(showtimeId)
            .collection("timelines")
            .document(timelineId)
            .collection("seats")

        val batch = db.batch()

        val rows = listOf("A", "B", "C", "D", "E")
        val cols = 10

        for (row in rows) {
            for (col in 1..cols) {
                val seatCode = "$row$col"
                val seatDoc = seatsCollection.document(seatCode)
                val seatData = hashMapOf(
                    "code" to seatCode,
                    "row" to row,
                    "column" to col,
                    "status" to "AVAILABLE"
                )
                batch.set(seatDoc, seatData)
            }
        }

        batch.commit()
            .addOnSuccessListener {
                Toast.makeText(this, "Tạo ghế mặc định thành công", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Tạo ghế mặc định thất bại", Toast.LENGTH_SHORT).show()
            }
    }
}