package com.example.movieticketsapp.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.movieticketsapp.databinding.SignUpLayoutBinding
import com.example.movieticketsapp.utils.navigateTo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: SignUpLayoutBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SignUpLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        setEvent()
    }

    private fun setEvent() {
        binding.apply {
            btnSignUp.setOnClickListener {
                val email = edtEmail.text.toString().trim()
                val password = edtPassword.text.toString().trim()

                when {
                    email.isEmpty() -> {
                        edtEmail.error = "Vui lòng nhập email"
                    }

                    password.isEmpty() -> {
                        edtPassword.error = "Vui lòng nhập mật khẩu"
                    }

                    password.length < 6 -> {
                        edtPassword.error = "Mật khẩu phải có ít nhất 6 ký tự"
                    }

                    !ckbAgree.isChecked -> {
                        Toast.makeText(
                            this@SignUpActivity,
                            "Vui lòng đồng ý với điều khoản sử dụng.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    else -> {
                        signUpWithEmailPassword(email, password)
                    }
                }
            }
            tvSignIn.setOnClickListener {
                navigateTo(SignInActivity::class.java, flag = false)
            }
        }
    }

    private fun signUpWithEmailPassword(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "createUserWithEmail:success")
                    sendVerificationEmail()
                } else {
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        this,
                        "Đăng ký thất bại: ${task.exception?.message}",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }

    private fun sendVerificationEmail() {
        val user = auth.currentUser
        user?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showVerifyEmailPopup()
                } else {
                    Toast.makeText(
                        this,
                        "Gửi email xác thực thất bại: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun showVerifyEmailPopup() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Xác thực Email")
        builder.setMessage("Bạn cần xác thực email trước khi đăng nhập.\nVui lòng kiểm tra email.")
        val progressBar = ProgressBar(this)
        progressBar.isIndeterminate = true
        builder.setView(progressBar)
        builder.setCancelable(false)
        val alertDialog = builder.create()
        alertDialog.show()
        (checkIfEmailVerified(alertDialog, progressBar))
    }

    private fun checkIfEmailVerified(
        dialog: androidx.appcompat.app.AlertDialog,
        progressBar: ProgressBar
    ) {
        val user = auth.currentUser
        val handler = android.os.Handler()
        val checkRunnable = object : Runnable {
            override fun run() {
                user?.reload()?.addOnSuccessListener {
                    if (user.isEmailVerified) {
                        Log.d("checkSC", "checkIfEmailVerified:success")
                        progressBar.visibility = View.GONE
                        dialog.dismiss()
                        Toast.makeText(
                            this@SignUpActivity,
                            "Xác thực thành công!",
                            Toast.LENGTH_SHORT
                        ).show()
                        startActivity(Intent(this@SignUpActivity, ProfileActivity::class.java))
                        finish()
                    } else {
                        handler.postDelayed(this, 3000)
                    }
                }
            }
        }
        handler.post(checkRunnable)
    }

    companion object {
        private const val TAG = "EmailPassword"
    }
}
