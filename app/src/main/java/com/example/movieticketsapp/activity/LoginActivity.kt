package com.example.movieticketsapp.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.movieticketsapp.databinding.LoginLayoutBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: LoginLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            btnLogin.setOnClickListener {
                val intent = Intent(this@LoginActivity, SignInActivity::class.java)
                startActivity(intent)
            }

            tvSignIn.setOnClickListener {
                val intent = Intent(this@LoginActivity, SignUpActivity::class.java)
                startActivity(intent)
            }
        }
    }
}