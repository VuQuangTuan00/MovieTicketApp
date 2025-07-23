package com.example.movieticketsapp.activity.Admin

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.movieticketsapp.databinding.AdminStatisticsLayoutBinding
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.firestore.FirebaseFirestore

class AdminStatisticsActivity : AppCompatActivity() {
    private lateinit var binding: AdminStatisticsLayoutBinding
    private lateinit var db: FirebaseFirestore

    // Cache tên phim và user
    private val movieNameCache = HashMap<String, String>()
    private val userNameCache = HashMap<String, String>()

    // Cache toàn bộ tickets
    private val ticketsList = mutableListOf<TicketData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AdminStatisticsLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        setSupportActionBar(binding.appbar.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Statistics"
        }
        binding.appbar.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Spinner chọn kiểu thống kê
        val filterOptions = listOf("By Day", "By Month", "By Movie", "By User")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filterOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFilter.adapter = spinnerAdapter

        preloadMoviesAndUsers {
            preloadTickets {
                binding.spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?, view: View?, position: Int, id: Long
                    ) {
                        when (position) {
                            0 -> showStatisticByDay()
                            1 -> showStatisticByMonth()
                            2 -> showStatisticByMovie()
                            3 -> showStatisticByUser()
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }
        }
    }

    // Data class để lưu dữ liệu vé
    data class TicketData(
        val date: String,
        val totalAmounts: Int,
        val movieId: String,
        val userId: String
    )

    // Load tên phim từ collection "movies", tên user từ collection "users"
    private fun preloadMoviesAndUsers(onComplete: () -> Unit) {
        db.collection("movie").get().addOnSuccessListener { movieQuery ->
            for (doc in movieQuery) {
                movieNameCache[doc.id] = doc.getString("title") ?: doc.id
            }
            db.collection("users").get().addOnSuccessListener { userQuery ->
                for (doc in userQuery) {
                    userNameCache[doc.id] = doc.getString("name") ?: doc.id
                }
                onComplete()
            }.addOnFailureListener {
                onComplete()
            }
        }.addOnFailureListener {
            onComplete()
        }
    }

    // Load toàn bộ tickets_movie 1 lần
    private fun preloadTickets(onComplete: () -> Unit) {
        db.collection("tickets_movie").get().addOnSuccessListener { query ->
            ticketsList.clear()
            for (doc in query) {
                val date = doc.getString("date") ?: continue
                val amount = parseAmount(doc.getString("totalAmounts") ?: "$0")
                val movieId = doc.getString("movieId") ?: continue
                val userId = doc.getString("userId") ?: continue
                ticketsList.add(TicketData(date, amount, movieId, userId))
            }
            onComplete()
        }.addOnFailureListener {
            onComplete()
        }
    }

    // Thống kê theo ngày
    private fun showStatisticByDay() {
        val map = HashMap<String, Int>()
        for (ticket in ticketsList) {
            map[ticket.date] = (map[ticket.date] ?: 0) + ticket.totalAmounts
        }
        showPieChart(map)
    }

    // Thống kê theo tháng (lấy 7 ký tự đầu của date YYYY-MM)
    private fun showStatisticByMonth() {
        val map = HashMap<String, Int>()
        for (ticket in ticketsList) {
            if (ticket.date.length >= 7) {
                val month = ticket.date.substring(0, 7)
                map[month] = (map[month] ?: 0) + ticket.totalAmounts
            }
        }
        showPieChart(map)
    }

    // Thống kê theo phim, hiển thị tên phim
    private fun showStatisticByMovie() {
        val map = HashMap<String, Int>()
        for (ticket in ticketsList) {
            map[ticket.movieId] = (map[ticket.movieId] ?: 0) + ticket.totalAmounts
        }
        val dataWithNames = map.mapKeys { movieNameCache[it.key] ?: it.key }
        showPieChart(dataWithNames)
    }

    // Thống kê theo user, hiển thị tên user
    private fun showStatisticByUser() {
        val map = HashMap<String, Int>()
        for (ticket in ticketsList) {
            map[ticket.userId] = (map[ticket.userId] ?: 0) + ticket.totalAmounts
        }
        val dataWithNames = map.mapKeys { userNameCache[it.key] ?: it.key }
        showPieChart(dataWithNames)
    }

    // Hiển thị PieChart
    private fun showPieChart(data: Map<String, Int>) {
        val entries = data.map { PieEntry(it.value.toFloat(), it.key) }
        val dataSet = PieDataSet(entries, "")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 14f

        val pieData = PieData(dataSet)
        binding.pieChart.data = pieData
        binding.pieChart.description.isEnabled = false
        binding.pieChart.invalidate()

        Log.d("AdminStatistics", "Data displayed: $data")
    }

    private fun parseAmount(amount: String): Int {
        return amount.replace(Regex("[^\\d.]"), "").toFloatOrNull()?.toInt() ?: 0
    }
}
