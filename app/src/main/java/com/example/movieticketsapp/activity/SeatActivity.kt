package com.example.movieticketsapp.activity

import android.annotation.SuppressLint
import android.icu.text.DecimalFormat
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movieticketsapp.R
import com.example.movieticketsapp.adapter.ItemDateAdapter
import com.example.movieticketsapp.adapter.ItemSeatAdapter
import com.example.movieticketsapp.adapter.ItemTimeAdapter
import com.example.movieticketsapp.databinding.SeatLayoutBinding
import com.example.movieticketsapp.model.Seat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class SeatActivity : AppCompatActivity() {
    private lateinit var binding: SeatLayoutBinding
    private  var price:Double = 0.0
    private  var number:Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SeatLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getIntentExtra()
        setVariable()
        initSeatList()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
    }

    private fun initSeatList() {
        val gridLayoutManager = GridLayoutManager(this, 7)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (position % 7 == 3) 1 else 1
            }
        }
        binding.rcvSeat.layoutManager = gridLayoutManager
        val seatList = mutableListOf<Seat>()
        val numSeat = 60
        for (i in 0 until numSeat) {
            val seatcode = ""
            val seatStatus = if(i == 2 || i == 20) Seat.Status.UNAVAILABLE else Seat.Status.AVAILABLE
            seatList.add(Seat("",seatcode,"",0, seatStatus))
        }
        val adapter = ItemSeatAdapter(seatList,object : ItemSeatAdapter.SelectedSeat{
            @SuppressLint("SetTextI18n")
            override fun onSelectedSeat(selectedNme: String, num: Int) {
                binding.tvNumberSelected.text = "$num Seat Selected"
                val df = DecimalFormat("#.##")
                price = df.format(num*1.0).toDouble()
                number = num
                binding.tvPrice.text = "$$price"
            }
        })
        binding.rcvSeat.adapter = adapter
        binding.rcvSeat.isNestedScrollingEnabled = false

        binding.rcvTime.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.HORIZONTAL,false)
        binding.rcvTime.adapter = ItemTimeAdapter(generateTimeSlot())

        binding.rcvDate.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.HORIZONTAL,false)
        binding.rcvDate.adapter = ItemDateAdapter(generateDates())
    }
    private fun setVariable() {
        binding.imgBack.setOnClickListener {
            finish()
        }
    }

    private fun getIntentExtra() {

    }
    private fun generateTimeSlot():List<String> {
        val timeSlot = mutableListOf<String>()
        val formatter = DateTimeFormatter.ofPattern("hh:mm a")

        for (i in 0..22 step 2){
            val time = LocalTime.of(i,0)
            timeSlot.add(time.format(formatter))
        }
        return timeSlot
    }
    private fun generateDates():List<String> {
        val dates = mutableListOf<String>()
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("EEE/dd/MMM")
        for (i in 0 until 7){
            dates.add(today.plusDays(i.toLong()).format(formatter))
        }
        return dates
    }
}