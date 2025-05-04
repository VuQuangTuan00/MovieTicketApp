package com.example.movieticketsapp.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.movieticketsapp.databinding.LoginLayoutBinding
import com.example.movieticketsapp.utils.navigateTo

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: LoginLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            btnLogin.setOnClickListener {
                navigateTo(SignInActivity::class.java,flag = false)
            }

            tvSignIn.setOnClickListener {
                navigateTo(SignUpActivity::class.java,flag = false)
            }
        }
    }
}