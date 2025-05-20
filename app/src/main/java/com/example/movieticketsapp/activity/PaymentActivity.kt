package com.example.movieticketsapp.activity

import TicketMovie
import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.movieticketsapp.R
import com.example.movieticketsapp.databinding.PaymentLayoutBinding
import com.example.movieticketsapp.model.Cinema
import com.example.movieticketsapp.model.GenerMovie
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class PaymentActivity : AppCompatActivity() {
    private lateinit var binding: PaymentLayoutBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var listGenerMovie: ArrayList<GenerMovie>
    private var ticketListener: ListenerRegistration? = null
    private lateinit var tickets:TicketMovie
    private lateinit var dialog: Dialog
    private  var flag: Boolean = false
    private lateinit var seatIds: ArrayList<String>
    private lateinit var showTimeId: String
    private var titleMovie:String = ""
    private lateinit var timelineId: String
    private lateinit var selectedDate: String
    private lateinit var selectedTime: String
    private  var standard: Double = 0.0
    private  var conversionFee: Double = 0.0
    private  var totalAmounts: Double = 0.0
    private lateinit var movieId: String
    private lateinit var userId: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PaymentLayoutBinding.inflate(layoutInflater)
        getIntentExtra()
        initialize()
        setContentView(binding.root)
        fetchLocation()
        setEvent()
    }

    private fun initialize() {
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        listGenerMovie = ArrayList()
        dialog = Dialog(this)
        db = Firebase.firestore
        tickets = TicketMovie(userId,movieId, titleMovie,seatIds,totalAmounts,selectedDate,selectedTime,"",showTimeId,timelineId,standard,conversionFee,"")
    }

    @SuppressLint("SetTextI18n")
    private fun setEvent() {
        binding.apply {
            imgBack.setOnClickListener { finish() }
            btnPay.setOnClickListener {
                if (!flag){
                    showVietQRDialog("WfezerpFvKNYs1m2Bi7W")
                    flag = true
                }else{
                    cancelPayment()
                    flag = false
                }
            }
            tickets.totalAmounts = tickets.standard + tickets.conversionFee
            tvDate.text = tickets.date
            tvHours.text = tickets.hour
            tvSeat.text = tickets.seatIds.joinToString ( ", " )
            tvConversionFree.text = "$${tickets.conversionFee}"
            tvStandard.text = "$${tickets.standard}"
            tvActualPay.text = "$${tickets.totalAmounts}"
        }
    }

    override fun onStart() {
        super.onStart()
        val generMap = mutableMapOf<String, String>()
        listGenerMovie.clear()

        db.collection("gener").get().addOnSuccessListener { genreSnapshot ->
            for (doc in genreSnapshot) {
                val genreId = doc.id
                val name = doc.getString("name") ?: ""
                generMap[genreId] = name
                if (name.isNotEmpty()) {
                    listGenerMovie.add(GenerMovie(genreId, name))
                }
            }
            fetchMovieDetail(generMap)
        }
    }

    private fun fetchMovieDetail(generMap: Map<String, String>) {
        ticketListener = db.collection("movie").document(tickets.movieId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("MovieRealtime", "Listen failed.", e)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val data = snapshot.data
                    val imgMovie = data?.get("img_movie") as? String
                    val genreIds = data?.get("gener_movie") as? List<String> ?: emptyList()
                    val duration = data?.get("druation") as? Number ?: "Unknown duration"
                    titleMovie = data?.get("title") as? String ?: "No title"
                    binding.tvTitleMovie.text = titleMovie
                    binding.tvDuration.text = "$duration minutes"
                    binding.tvDirector.text = data?.get("director") as? String ?: "Unknown director"
                    val genreNames = genreIds.mapNotNull { generMap[it] }
                    binding.tvGener.text = genreNames.joinToString(", ")

                    if (!imgMovie.isNullOrEmpty()) {
                        Glide.with(this@PaymentActivity)
                            .load(imgMovie)
                            .into(binding.imgMovie)
                    }
                }
            }
    }
   private fun uploadTicketForUser(ticket: TicketMovie, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val userTicketsRef = db.collection("users")
            .document(ticket.userId)
            .collection("tickets")

        userTicketsRef
            .add(ticket)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

//   private fun listenToUserTickets(
//        userId: String,
//        onTicketsUpdate: (List<TicketMovie>) -> Unit,
//        onError: (Exception) -> Unit
//    ): ListenerRegistration {
//        val db = FirebaseFirestore.getInstance()
//        return db.collection("users")
//            .document(userId)
//            .collection("tickets")
//            .addSnapshotListener { snapshots, e ->
//                if (e != null) {
//                    onError(e)
//                    return@addSnapshotListener
//                }
//
//                val ticketList = snapshots?.documents?.mapNotNull { doc ->
//                    try {
//                        doc.toObject(TicketMovie::class.java)
//                    } catch (ex: Exception) {
//                        null
//                    }
//                } ?: emptyList()
//
//                onTicketsUpdate(ticketList)
//            }
//    }
    private fun fetchLocation() {
        var locationCinema = Cinema("", "", "", "")
        db.collection("cinema")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("Firestore", "Error fetching location_cinema", e)
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    for (doc in snapshots) {
                        tickets.cinemaId = doc.id
                        val cinemaName = doc.getString("cinema_name") ?: "Không có thông tin"
                        locationCinema = Cinema(cinemaName, "", "phone", "")
                    }
                }
                binding.tvCinema.text = locationCinema.cinemaName
            }
    }

    private fun showVietQRDialog(paymentId: String) {
        if (isFinishing || isDestroyed) return
        dialog.setContentView(R.layout.qr_dialog_layout)
        val imgQRCode = dialog.findViewById<ImageView>(R.id.imgQRCode)
        val tvAmount = dialog.findViewById<TextView>(R.id.tvAmount)
        val btnPay = dialog.findViewById<Button>(R.id.btnPaid)
        btnPay.setOnClickListener {
            uploadTicketForUser(tickets,
                onSuccess = {
                    updateSeatStatus(seatIds)
                },
                onFailure = { e ->
                    Toast.makeText(this, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }
        db.collection("bank").document(paymentId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("Firestore", "Error fetching QR code", e)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                        val bank_number = snapshot.getString("bank_number") ?: ""
                        val bank_name = snapshot.getString("bank_name") ?: ""
                        val bank_account = snapshot.getString("bank_account") ?: ""
                        val amount = snapshot.getLong("amount")?.toInt() ?: 0

                        tvAmount.text = "Số tiền: %,dđ".format(amount)
                        Glide.with(this)
                            .load("https://img.vietqr.io/image/$bank_name-$bank_number-compact2.jpg?amount=$amount&addInfo=dong%20gop%20quy%20vac%20xin&accountName=$bank_account")
                            .into(imgQRCode)
                }
            }
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    private fun cancelPayment() {
       tickets.movieId =  db.collection("tickets_movie").id
        db
            .collection("tickets_movie")
            .document(tickets.movieId)
            .delete()
            .addOnSuccessListener {
                binding.btnPay.text = "Pay"
                Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun updateSeatStatus(seatIds: List<String>) {
        val db = FirebaseFirestore.getInstance()
        val batch = db.batch()
        val seatRef = db.collection("showtimes").document(showTimeId).collection("timelines").document(timelineId).collection("seats")

        seatIds.forEach { seatId ->
            val docRef = seatRef.document(seatId)
            batch.update(docRef, "status", "UNAVAILABLE")
        }

        batch.commit()
            .addOnSuccessListener {
                Log.d("SeatUpdate", "Ghế đã được cập nhật realtime")
                Toast.makeText(this, "Ticket booking successful!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Log.e("SeatUpdate", "Lỗi cập nhật trạng thái ghế", it)
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
       showTimeId = intent?.getStringExtra("showtimeId") ?: run {
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
        seatIds = intent?.getStringArrayListExtra("seat") ?: run {
            Toast.makeText(this, "Seat is missing!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        standard = intent?.getDoubleExtra("price", 0.0) ?: run {
            Toast.makeText(this, "Price is missing!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        totalAmounts = standard + conversionFee
    }
    override fun onStop() {
        super.onStop()
        ticketListener?.remove()
    }
    override fun onDestroy() {
        super.onDestroy()
        dialog.dismiss()
    }
}
