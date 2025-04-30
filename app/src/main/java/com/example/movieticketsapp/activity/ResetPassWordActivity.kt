package com.example.movieticketsapp.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.movieticketsapp.databinding.ResetPassWordLayoutBinding
import com.example.movieticketsapp.utils.navigateTo
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase

class ResetPassWordActivity : AppCompatActivity() {
    private lateinit var binding: ResetPassWordLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ResetPassWordLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setEvent()
    }
        private fun setEvent(){
        binding.apply {
            imgBack.setOnClickListener {finish()}
            edtEmail.text.toString()
            btnReset.setOnClickListener {
                val email = edtEmail.text.toString().trim()
                if (email.isEmpty()){
                    edtEmail.error = "Vui lòng nhập email"
                }else{
                    Toast.makeText(this@ResetPassWordActivity, "Hãy kiểm tra email của bạn!", Toast.LENGTH_LONG).show()
                    sendPasswordResetEmail(edtEmail.text.toString())
                }
            }
        }
    }

    private fun sendPasswordResetEmail(email:String){
        Firebase.auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("Email sent SC", "Email sent.")
                    navigateTo(SignInActivity::class.java)
                }
            }
    }

    // DUNG XOA PHAN NAY PLS!!! T-T
    // custom email send otp
//    private fun setEvent(){
//        binding.apply {
//            imgBack.setOnClickListener {finish()}
//            edtEmail.text.toString()
//            btnReset.setOnClickListener {
//                val email = edtEmail.text.toString().trim()
//                if (email.isEmpty()){
//                    edtEmail.error = "Vui lòng nhập email"
//                }else{
//                    Toast.makeText(this@ResetPassWordActivity, "Mã OTP đã được gửi đến email!", Toast.LENGTH_LONG).show()
//                    sendOtpToEmail(email)
//                }
//            }
//        }
//    }
//    private fun generateOtp(): String {
//        return (100000..999999).random().toString()
//    }
//    private fun sendOtpToEmail(email: String) {
//        val otpCode = generateOtp()
//        val firestore = FirebaseFirestore.getInstance()
//
//        val data = hashMapOf(
//            "email" to email,
//            "otp" to otpCode,
//            "timestamp" to FieldValue.serverTimestamp()
//        )
//
//        firestore.collection("otp_codes")
//            .document(email)
//            .set(data)
//            .addOnSuccessListener {
//                sendEmailViaCloudFunction(email, otpCode)
//            }
//            .addOnFailureListener { e ->
//                Log.e("OTP", "Error saving OTP: ", e)
//            }
//    }
//    private fun sendEmailViaCloudFunction(email: String, otpCode: String) {
//        val functions = Firebase.functions
//
//        val data = hashMapOf(
//            "email" to email,
//            "otp" to otpCode
//        )
//
//        functions
//            .getHttpsCallable("sendOtpEmail")
//            .call(data)
//            .addOnSuccessListener {
//                Log.d("OTP", "Email sent successfully")
//                navigateTo(OTPCodeActivity::class.java)
//            }
//            .addOnFailureListener { e ->
//                Log.e("OTP", "Failed to send email: ", e)
//            }
//    }
}