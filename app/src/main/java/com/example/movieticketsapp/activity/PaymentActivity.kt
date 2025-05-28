package com.example.movieticketsapp.activity

import TicketMovie
import android.annotation.SuppressLint
import android.app.ComponentCaller
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.movieticketsapp.Api.CreateOrder
import com.example.movieticketsapp.R
import com.example.movieticketsapp.databinding.PaymentLayoutBinding
import com.example.movieticketsapp.model.Cinema
import com.example.movieticketsapp.model.GenerMovie
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import vn.zalopay.sdk.Environment
import vn.zalopay.sdk.ZaloPayError
import vn.zalopay.sdk.ZaloPaySDK
import vn.zalopay.sdk.listeners.PayOrderListener

class PaymentActivity : AppCompatActivity() {
    private lateinit var binding: PaymentLayoutBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var listGenerMovie: ArrayList<GenerMovie>
    private var ticketListener: ListenerRegistration? = null
    private lateinit var tickets:TicketMovie
    private lateinit var dialog: Dialog
    private lateinit var seatIds: ArrayList<String>
    private lateinit var showTimeId: String
    private lateinit var timelineId: String
    private lateinit var selectedDate: String
    private lateinit var selectedTime: String
    private  var standard: Double = 0.0
    private  var conversionFee: Double = 0.0
    private  var totalAmounts: Double = 0.0

    private lateinit var movieId: String
    private lateinit var userId: String
    private var policy: StrictMode.ThreadPolicy? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PaymentLayoutBinding.inflate(layoutInflater)
        getIntentExtra()
        initialize()
        setContentView(binding.root)
        fetchLocation()
        setEvent()
        policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        ZaloPaySDK.init(2553, Environment.SANDBOX)

    }

    private fun initialize() {
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        listGenerMovie = ArrayList()
        dialog = Dialog(this)
        db = Firebase.firestore
        tickets = TicketMovie()
    }

    private fun ZaloPay
                () {
        val orderApi = CreateOrder()
//        val priceText = tickets.totalAmounts.toString()
//
//        if (priceText.isEmpty()) {
//            Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show()
//            return
//        }

        try {
            val data = orderApi.createOrder("5000")
            val code = data.getInt("return_code")
            val message = data.optString("return_message")
            val subMessage = data.optString("sub_return_message")

            Log.d("ZaloPay", "Code: $code, Message: $message, Detail: $subMessage")
            if (data.has("return_code") && data.getString("return_code") == "1") {
                val token = data.getString("zp_trans_token")
                ZaloPaySDK.getInstance().payOrder(this, token, "demozpdk://app",
                    object : PayOrderListener {
                        override fun onPaymentSucceeded(transactionId: String?, transToken: String?, appTransID: String?) {
                            AlertDialog.Builder(this@PaymentActivity)
                                .setTitle("Payment Success")
                                .setMessage("TransactionId: $transactionId\nTransToken: $transToken")
                                .setPositiveButton("OK") { _, _ ->
                                    // Gọi lưu vé
                                    uploadTicketForUser(tickets,
                                        onSuccess = {
                                            updateSeatStatus(seatIds)
                                        },
                                        onFailure = { e ->
                                            Toast.makeText(this@PaymentActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                                .show()
                        }

                        override fun onPaymentCanceled(zpTransToken: String?, appTransID: String?) {
                            AlertDialog.Builder(this@PaymentActivity)
                                .setTitle("Đã hủy thanh toán")
                                .setMessage("zpTransToken: $zpTransToken")
                                .setPositiveButton("OK", null)
                                .show()
                        }

                        override fun onPaymentError(zaloPayError: ZaloPayError?, zpTransToken: String?, appTransID: String?) {
                            AlertDialog.Builder(this@PaymentActivity)
                                .setTitle("Thanh toán thất bại")
                                .setMessage("ZaloPayError: $zaloPayError\nTransToken: $zpTransToken")
                                .setPositiveButton("OK", null)
                                .show()
                        }
                    })
            } else {
                Toast.makeText(this, "Không thể tạo đơn hàng", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setEvent() {
        binding.apply {
            imgBack.setOnClickListener { finish() }
            btnPay.setOnClickListener {
//                    showVietQRDialog()
                ZaloPay()
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
        ticketListener = db.collection("movie").document(movieId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("MovieRealtime", "Listen failed.", e)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val data = snapshot.data
                    val imgMovie = data?.get("img_movie") as? String
                    val genreIds = data?.get("gener_movie") as? List<String> ?: emptyList()
                    val duration = data?.get("duration") as? Number ?: "Unknown duration"
                    val titleMovie = data?.get("title") as? String ?: "No title"
                    binding.tvDuration.text = "$duration minutes"
                    binding.tvDirector.text = data?.get("director") as? String ?: "Unknown director"
                    val genreNames = genreIds.mapNotNull { generMap[it] }
                    binding.tvGener.text = genreNames.joinToString(", ")
                    binding.tvTitleMovie.text = titleMovie
                    if (!imgMovie.isNullOrEmpty()) {
                        Glide.with(this@PaymentActivity)
                            .load(imgMovie)
                            .into(binding.imgMovie)
                    }
                    tickets = TicketMovie("",movieId, titleMovie,imgMovie!!,seatIds,totalAmounts,selectedDate,selectedTime,"",showTimeId,timelineId,standard,conversionFee,"")
                    binding.apply {
                        tvDate.text = tickets.date
                        tvHours.text = tickets.hour
                        tvSeat.text = tickets.seatIds.joinToString(", ")
                        tvConversionFree.text = "$${tickets.conversionFee}"
                        tvStandard.text = "$${tickets.standard}"
                        tvActualPay.text = "$${tickets.totalAmounts}"
                    }
                }
            }
    }
   private fun uploadTicketForUser(ticket: TicketMovie, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val db = FirebaseFirestore.getInstance()
       val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userTicketsRef = db.collection("users")
            .document(userId)
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

    private fun showVietQRDialog() {
        if (isFinishing || isDestroyed) return
        dialog.setContentView(R.layout.qr_dialog_layout)
        val imgQRCode = dialog.findViewById<ImageView>(R.id.imgQRCode)
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
        db.collection("bank").limit(1)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("Firestore", "Error fetching QR code", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    for (doc in snapshot) {
                        val bank_number = doc.getString("bank_number") ?: ""
                        val bank_name = doc.getString("bank_name") ?: ""
                        val bank_account = doc.getString("bank_account") ?: ""
                        val amount = doc.getLong("amount")?.toInt() ?: 0
                        Glide.with(this)
                            .load("https://img.vietqr.io/image/$bank_name-$bank_number-compact2.jpg?amount=${tickets.totalAmounts}&addInfo=dong%20gop%20quy%20vac%20xin&accountName=$bank_account")
                            .into(imgQRCode)
                    }
                }
            }
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
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
    override fun onNewIntent(intent: Intent, caller: ComponentCaller) {
        super.onNewIntent(intent, caller)
        ZaloPaySDK.getInstance().onResult(intent)
    }
}
