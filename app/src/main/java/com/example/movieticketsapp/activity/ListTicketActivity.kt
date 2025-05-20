package com.example.movieticketsapp.activity

import TicketMovie
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movieticketsapp.adapter.ItemTicketAdapter
import com.example.movieticketsapp.databinding.ListTicketLayoutBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ListTicketActivity : AppCompatActivity() {

    private lateinit var binding: ListTicketLayoutBinding
    private val tickets = mutableListOf<TicketMovie>()
    private val db = Firebase.firestore
    private lateinit var adapter: ItemTicketAdapter
    private lateinit var ticketID :String
    private lateinit var movieTitle :String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ticketID =""
        movieTitle = ""
        binding = ListTicketLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        fetchTicketsWithMovieTitles()
//        setUpRecyclerView()

    }

//    private fun setUpRecyclerView() {
//        // 1) Khởi tạo adapter với tickets và callback khi click
//        adapter = ItemTicketAdapter(tickets,movieTitle) { ticket ->
//            // đọc ticket.id và ticket.movieId
//            Toast.makeText(
//                this,
//                "Bạn vừa chọn vé: ${ticket.id}\nPhim: ${ticket.movieId}\nNgày: ${ticket.date} ${ticket.hour}",
//                Toast.LENGTH_SHORT
//            ).show()
//
//            // chuyền đúng TICKET_ID và MOVIE_ID
//            val intent = Intent(this, TicketDetailActivity::class.java).apply {
//                putExtra("TICKET_ID", ticket.id)
//                putExtra("MOVIE_ID", ticket.movieId)
//            }
//            startActivity(intent)
//        }
//
//        // 2) Gán LayoutManager + Adapter
//        binding.rcvList.apply {
//            layoutManager = LinearLayoutManager(this@ListTicketActivity)
//            adapter = this@ListTicketActivity.adapter
//        }
//    }

    private fun fetchTicketsWithMovieTitles() {

    }
}
