package com.example.movieticketsapp.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movieticketsapp.adapter.ItemTicketAdapter
import com.example.movieticketsapp.databinding.ListTicketLayoutBinding
import com.example.movieticketsapp.model.TicketMovie
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
        setUpRecyclerView()

    }

    private fun setUpRecyclerView() {
        // 1) Khởi tạo adapter với tickets và callback khi click
        adapter = ItemTicketAdapter(tickets,movieTitle) { ticket ->
            // đọc ticket.id và ticket.movieId
            Toast.makeText(
                this,
                "Bạn vừa chọn vé: ${ticket.id}\nPhim: ${ticket.movieId}\nNgày: ${ticket.date} ${ticket.hour}",
                Toast.LENGTH_SHORT
            ).show()

            // chuyền đúng TICKET_ID và MOVIE_ID
            val intent = Intent(this, TicketDetailActivity::class.java).apply {
                putExtra("TICKET_ID", ticket.id)
                putExtra("MOVIE_ID", ticket.movieId)
            }
            startActivity(intent)
        }

        // 2) Gán LayoutManager + Adapter
        binding.rcvList.apply {
            layoutManager = LinearLayoutManager(this@ListTicketActivity)
            adapter = this@ListTicketActivity.adapter
        }
    }

    private fun fetchTicketsWithMovieTitles() {
        // 1) Pre-fetch tất cả phim vào map: movieId → title
        db.collection("movie")
            .get()
            .addOnSuccessListener { movieSnaps ->
                val movieMap = mutableMapOf<String, String>()
                for (doc in movieSnaps) {
                    val title = doc.getString("title").orEmpty()
                    movieMap[doc.id] = title
                }

                // 2) Sau khi có map, fetch tickets
                db.collection("tickets_movie")
                    .get()
                    .addOnSuccessListener { ticketSnaps ->
                        val enrichedTickets = ticketSnaps.map { tDoc ->
                            // đọc dữ liệu ticket
                            val id           = tDoc.id
                            val date         = tDoc.getString("date").orEmpty()
                            val hour         = tDoc.getString("hour").orEmpty()
                            val seatIds      = tDoc.getString("seatIds").orEmpty()
                            val standard     = tDoc.getString("standard").orEmpty()
                            val conversion   = tDoc.getString("conversionFee").orEmpty()
                            val total        = tDoc.getString("totalAmounts").orEmpty()
                            val movieId      = tDoc.getString("movieId").orEmpty()
                            // lookup title từ map
                             movieTitle   = movieMap[movieId] ?: "Unknown movie"
                            // dùng một data class mới hoặc mở rộng TicketMovie
                            TicketMovie(
                                id            = id,
                                cinemaId      = tDoc.getString("cinemaId").orEmpty(),
                                conversionFee = conversion,
                                date          = date,
                                hour          = hour,
                                movieId       = movieId,
                                seatIds       = seatIds,
                                standard      = standard,
                                totalAmounts  = total,
                                userId        = tDoc.getString("userId").orEmpty(),
                            )
                        }

                        // 3) Cập nhật UI / Adapter
                        tickets.clear()
                        tickets.addAll(enrichedTickets)
                        adapter.notifyDataSetChanged()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Lỗi tải tickets: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Lỗi tải movies: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
