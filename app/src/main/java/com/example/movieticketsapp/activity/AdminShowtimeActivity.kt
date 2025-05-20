package com.example.movieticketsapp.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.movieticketsapp.databinding.AdminShowtimeLayoutBinding

class AdminShowtimeActivity : AppCompatActivity() {
    private lateinit var binding: AdminShowtimeLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AdminShowtimeLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}