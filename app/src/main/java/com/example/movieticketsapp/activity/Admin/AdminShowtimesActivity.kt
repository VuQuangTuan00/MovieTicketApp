package com.example.movieticketsapp.activity.Admin

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movieticketsapp.adapter.ItemShowtimeAdapter
import com.example.movieticketsapp.databinding.AdminShowtimesLayoutBinding
import com.example.movieticketsapp.databinding.DialogShowtimeEditBinding
import com.example.movieticketsapp.model.ShowTime
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class AdminShowtimesActivity : AppCompatActivity() {

    private lateinit var binding: AdminShowtimesLayoutBinding
    private lateinit var adapter: ItemShowtimeAdapter
    private val showtimeList = mutableListOf<ShowTime>()
    private val db = FirebaseFirestore.getInstance()
    private var movieId: String = ""

    private var cachedCinemaIdList = listOf<String>()
    private var cachedCinemaNameList = listOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AdminShowtimesLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Lấy movieId từ Intent
        movieId = intent.getStringExtra("movieId") ?: ""
        Log.d("AdminShowtimesActivity", "movieId = $movieId")

        loadCinemaList { idList, nameList ->
            cachedCinemaIdList = idList
            cachedCinemaNameList = nameList
        }

        // Setup toolbar với nút back và tiêu đề
        setSupportActionBar(binding.appbar.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Showtimes"
        }
        binding.appbar.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Setup RecyclerView
        adapter = ItemShowtimeAdapter(showtimeList,
            onEdit = { showtime -> showAddEditDialog(showtime) },
            onDelete = { showtime -> deleteShowtime(showtime) },
            onClick = { showtime ->
                // mở timeline activity truyền showtime.id
                val intent = Intent(this, AdminTimelineActivity::class.java)
                intent.putExtra("showtimeId", showtime.id)
                intent.putExtra("showtimeStartTime", showtime.start_time)
                startActivity(intent)
            }
        )
        binding.fabAddShowtime.setOnClickListener {
            showAddEditDialog(null)
        }

        binding.recyclerViewShowtimes.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewShowtimes.adapter = adapter

        // Load data showtime theo movieId
        loadShowtimes()
    }

    private fun loadCinemaList(onComplete: (cinemaIdList: List<String>, cinemaNameList: List<String>) -> Unit) {
        val cinemaIdList = mutableListOf<String>()
        val cinemaNameList = mutableListOf<String>()

        db.collection("cinema")
            .get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    cinemaIdList.add(doc.id)
                    cinemaNameList.add(doc.getString("cinema_name") ?: "Unknown Cinema")
                }
                onComplete(cinemaIdList, cinemaNameList)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load cinema", Toast.LENGTH_SHORT).show()
                onComplete(emptyList(), emptyList())
            }
    }

    private fun loadShowtimes() {
        if (movieId.isEmpty()) {
            Toast.makeText(this, "Movie ID is missing", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("showtimes")
            .whereEqualTo("movie_id", movieId)
            .get()
            .addOnSuccessListener { result ->
                val newList = mutableListOf<ShowTime>()
                for (doc in result) {
                    val startTime: Date = doc.getDate("start_time") ?: Date()
                    val showtime = ShowTime(
                        id = doc.id,
                        movie_id = doc.getString("movie_id") ?: "",
                        price = doc.getLong("price")?.toInt() ?: 0,
                        start_time = startTime,
                        cinema_id = doc.getString("cinema_id") ?: ""
                    )
                    newList.add(showtime)
                }
                adapter.updateList(newList)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load showtimes: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showAddEditDialog(showtime: ShowTime?) {
        val dialogBinding = DialogShowtimeEditBinding.inflate(layoutInflater)
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        val calendar = Calendar.getInstance()
        if (showtime != null) {
            calendar.time = showtime.start_time
            dialogBinding.etPrice.setText(showtime.price.toString())
            dialogBinding.etStartTime.setText(sdf.format(showtime.start_time))
        } else {
            dialogBinding.etStartTime.setText(sdf.format(calendar.time))
        }

        val spinner = dialogBinding.spinnerCinema

        // Dùng cache để gán adapter spinner
        val spinnerAdapter = android.widget.ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            cachedCinemaNameList
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter

        if (showtime != null) {
            val index = cachedCinemaIdList.indexOf(showtime.cinema_id)
            if (index >= 0) spinner.setSelection(index)
        }

        dialogBinding.etStartTime.setOnClickListener {
            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                TimePickerDialog(this, { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    dialogBinding.etStartTime.setText(sdf.format(calendar.time))
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(if (showtime == null) "Add Showtime" else "Edit Showtime")
            .setView(dialogBinding.root)
            .setPositiveButton(if (showtime == null) "Add" else "Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val btn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            btn.setOnClickListener {
                val price = dialogBinding.etPrice.text.toString().toIntOrNull()
                val startTime = try { sdf.parse(dialogBinding.etStartTime.text.toString()) } catch (e: Exception) { null }
                val selectedCinemaIndex = spinner.selectedItemPosition

                if (price != null && startTime != null && selectedCinemaIndex >= 0) {
                    val selectedCinemaId = cachedCinemaIdList.getOrNull(selectedCinemaIndex) ?: ""
                    if (showtime == null) addShowtime(price, startTime, selectedCinemaId)
                    else updateShowtime(showtime.id, price, startTime, selectedCinemaId)
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "Invalid input or cinema not selected", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }

    private fun addShowtime(price: Int, startTime: Date, cinemaId: String) {
        val newDoc = db.collection("showtimes").document()
        val data = hashMapOf(
            "movie_id" to movieId,
            "price" to price,
            "start_time" to startTime,
            "cinema_id" to cinemaId
        )
        newDoc.set(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Showtime added", Toast.LENGTH_SHORT).show()
                loadShowtimes()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add showtime", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateShowtime(id: String, price: Int, startTime: Date, cinemaId: String) {
        val data = mapOf(
            "price" to price,
            "start_time" to startTime,
            "cinema_id" to cinemaId
        )
        db.collection("showtimes").document(id)
            .update(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Showtime updated", Toast.LENGTH_SHORT).show()
                loadShowtimes()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update showtime", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteShowtime(showtime: ShowTime) {
        AlertDialog.Builder(this)
            .setTitle("Delete Showtime")
            .setMessage("Are you sure you want to delete this showtime?")
            .setPositiveButton("Delete") { _, _ ->
                db.collection("showtimes").document(showtime.id)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Showtime deleted", Toast.LENGTH_SHORT).show()
                        loadShowtimes()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to delete showtime", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
