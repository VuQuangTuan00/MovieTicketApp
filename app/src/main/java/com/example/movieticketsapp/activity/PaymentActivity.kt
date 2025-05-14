package com.example.movieticketsapp.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
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
import com.example.movieticketsapp.model.TicketMovie
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.lang.Double.parseDouble

class PaymentActivity : AppCompatActivity() {
    private lateinit var binding: PaymentLayoutBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var listGenerMovie: ArrayList<GenerMovie>
    private var movieListener: ListenerRegistration? = null
    private lateinit var selectedDate: String
    private lateinit var selectedTime: String
    private lateinit var showtimeId: String
    private lateinit var timelineId: String
    private lateinit var movieId: String
    private lateinit var standard: String
    private lateinit var totalAmounts: String
    private lateinit var conversionFee: String
    private lateinit var seat: String
    private lateinit var ticket_movie_id: String
    private lateinit var cinemaId: String
    private lateinit var dialog: Dialog

    private  var flag: Boolean = false
    private var price: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PaymentLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialize()
        getIntentExtra()
        fetchLocation()
        setEvent()
    }

    private fun initialize() {
        standard = ""
        totalAmounts = ""
        conversionFee = ""
        cinemaId = ""
        selectedDate = ""
        selectedTime= ""
        showtimeId= ""
        timelineId= ""
        movieId= ""
        seat= ""
        listGenerMovie = ArrayList()
        dialog = Dialog(this)
        db = Firebase.firestore
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
            tvDate.text = selectedDate
            tvHours.text = selectedTime
            tvSeat.text = seat
            tvConversionFree.text = "$0"
            tvStandard.text = "$${price}"
            tvActualPay.text = "$${price}"

            standard = tvStandard.text.toString()
            conversionFee = tvConversionFree.text.toString()
            totalAmounts = tvActualPay.text.toString()
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
        movieListener = db.collection("movie").document(movieId)
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

                    binding.tvTitleMovie.text = data?.get("title") as? String ?: "No title"
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
                        cinemaId = doc.id
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
        val btnClose = dialog.findViewById<Button>(R.id.btnClose)

        btnClose.setOnClickListener {
                uploadTicketToFirestore()
        }

        db.collection("bank").document(paymentId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val bank_number = doc.getString("bank_number") ?: return@addOnSuccessListener
                    val bank_name = doc.getString("bank_name") ?: return@addOnSuccessListener
                    val bank_account = doc.getString("bank_account") ?: return@addOnSuccessListener
                    val amount = doc.getLong("amount")?.toInt() ?: 0

                    tvAmount.text = "Số tiền: %,dđ".format(amount)
                    Glide.with(this)
                        .load("https://img.vietqr.io/image/$bank_name-$bank_number-compact.jpg?amount=$amount&addInfo=dong%20gop%20quy%20vac%20xin&accountName=$bank_account%20Vac%20Xin%20Covid")
                        .into(imgQRCode)
//                    https://img.vietqr.io/image/vietinbank-113366668888-compact2.jpg?amount=790000&addInfo=dong%20gop%20quy%20vac%20xin&accountName=Quy%20Vac%20Xin%20Covid
                }
            }

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }
    private fun uploadTicketToFirestore(
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Người dùng chưa đăng nhập", Toast.LENGTH_SHORT).show()
            return
        }

        val seatIds = seat?.split(",")?.map { it.trim() } ?: emptyList()

        val ticketMovie = TicketMovie(
            userId = userId,
            movieId = movieId,
            seatIds = seat,
            totalAmounts = totalAmounts,
            date = selectedDate,
            hour = selectedTime,
            cinemaId = cinemaId,
            standard = standard,
            conversionFee = conversionFee
        )

        FirebaseFirestore.getInstance().collection("tickets_movie")
            .add(ticketMovie)
            .addOnSuccessListener {
                binding.btnPay.text = "Cancle"
                Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cancelPayment() {
       ticket_movie_id =  db.collection("tickets_movie").id
        db
            .collection("tickets_movie")
            .document(ticket_movie_id)
            .delete()
            .addOnSuccessListener {
                binding.btnPay.text = "Pay"
                Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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
        movieId = intent?.getStringExtra("movie_id") ?: run {
            Toast.makeText(this, "Movie ID is missing!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        seat = intent?.getStringExtra("seat") ?: run {
            Toast.makeText(this, "Seat is missing!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        price = intent?.getDoubleExtra("price", 0.0) ?: run {
            Toast.makeText(this, "Price is missing!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        dialog.dismiss()
        movieListener?.remove()
    }
}
