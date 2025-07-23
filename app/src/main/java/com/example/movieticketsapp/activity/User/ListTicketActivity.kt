package com.example.movieticketsapp.activity.User

import TicketMovie
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movieticketsapp.adapter.ItemTicketAdapter
import com.example.movieticketsapp.databinding.ListTicketLayoutBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ListTicketActivity : AppCompatActivity() {

    private lateinit var binding: ListTicketLayoutBinding
    private val tickets = mutableListOf<TicketMovie>()
    private val db = Firebase.firestore
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private lateinit var adapter: ItemTicketAdapter
    private var ticketID :String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ListTicketLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        fetchTicketsWithMovieTitles()
        setUpRecyclerView()
    }
    override fun onStart() {
        super.onStart()
        listenToUserTickets(onTicketsUpdate = {
            tickets.clear()
            tickets.addAll(it)
            adapter.notifyDataSetChanged()
        }, onError = {
            Log.e("Firestore", "Error fetching user tickets", it)
        })
    }
    private fun setUpRecyclerView() {
        adapter = ItemTicketAdapter(tickets) { ticket ->
            val intent = Intent(this, TicketDetailActivity::class.java).apply {
                putExtra("TICKET_ID", ticketID)
                putExtra("MOVIE_ID", ticket.movieId)
            }
            startActivity(intent)
        }

        binding.rcvList.apply {
            layoutManager = LinearLayoutManager(this@ListTicketActivity)
            adapter = this@ListTicketActivity.adapter
        }
    }
       private fun listenToUserTickets(
        onTicketsUpdate: (List<TicketMovie>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return db.collection("users")
            .document(userId)
            .collection("tickets")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    onError(e)
                    return@addSnapshotListener
                }
                val ticketList = snapshots?.documents?.mapNotNull { doc ->
                    try {
                       val ticket = doc.toObject(TicketMovie::class.java)
                        ticket?.let {
                            ticketID = doc.id
                        }
                        ticket
                    } catch (ex: Exception) {
                        null
                    }
                } ?: emptyList()

                onTicketsUpdate(ticketList)
            }
    }
    private fun fetchTicketsWithMovieTitles() {

    }
}
