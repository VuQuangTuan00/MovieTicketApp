package com.example.movieticketsapp.activity.User

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.movieticketsapp.databinding.OtpCodeLayoutBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

class OTPCodeActivity : AppCompatActivity() {
    private lateinit var binding: OtpCodeLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = OtpCodeLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
    fun verifyOtp(email: String, inputOtp: String) {
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("otp_codes").document(email)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val otp = document.getString("otp")
                    val timestamp = document.getTimestamp("timestamp")
                    val now = Timestamp.now()
                    val seconds = now.seconds - (timestamp?.seconds ?: 0)

                    if (otp == inputOtp && seconds <= 300) { // 5 phút
                        Log.d("OTP", "OTP valid, allow reset password")
                        // Cho phép reset password tại đây
                    } else {
                        Toast.makeText(this, "Mã OTP sai hoặc đã hết hạn.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Không tìm thấy mã OTP.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("OTP", "Error reading OTP", e)
            }
    }
}