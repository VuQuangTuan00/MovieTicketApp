package com.example.movieticketsapp.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.movieticketsapp.databinding.SignInLayoutBinding

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.Firebase



class SignInActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: SignInLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
        binding = SignInLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setEvent()
    }
    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            reload()
        }
    }
    private fun navigateTo(destination: Class<*>) {
        startActivity(Intent(this, destination))
    }
    private fun setEvent(){
        binding.apply {
            imBack.setOnClickListener {finish()}
            tvForgotPass.setOnClickListener { navigateTo(ResetPassWordActivity::class.java)}
            tvSignUp.setOnClickListener {navigateTo(SignUpActivity::class.java)}
            btnSignIn.setOnClickListener {signInToFireBase(edtMail,edtPass)}
        }
    }


    private fun signInToFireBase(mail: EditText, pass: EditText) {
        auth.signInWithEmailAndPassword(
            mail.text.toString(),
            pass.text.toString()
        ).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Log.d("TAGG", "signInWithEmail:success")
                val user = auth.currentUser
                user?.reload()?.addOnSuccessListener {
                    if (user.isEmailVerified) {
                        // Đã xác thực email -> Cho vào app
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    } else {
                        // Email chưa xác thực
                        Toast.makeText(this, "Vui lòng xác thực email để đăng nhập.", Toast.LENGTH_LONG).show()
                        auth.signOut()
                    }
                }
            } else {
                Log.w("TAG", "signInWithEmail:failure", task.exception)
                Toast.makeText(
                    baseContext,
                    "Đăng nhập thất bại: ${task.exception?.message}",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }


    private fun reload() {
    }

    companion object {
        private const val TAG = "EmailPassword"
    }
}

