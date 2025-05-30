package com.example.movieticketsapp.activity.Login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.movieticketsapp.databinding.GetStartedLayoutBinding
import com.example.movieticketsapp.utils.navigateTo

class GetStartedActivity : AppCompatActivity() {
    private lateinit var binding: GetStartedLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = GetStartedLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnGetStarted.setOnClickListener {
           navigateTo(LoginActivity::class.java,flag = false)
        }
    }
}