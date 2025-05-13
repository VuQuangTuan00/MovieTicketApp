package com.example.movieticketsapp.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.movieticketsapp.databinding.AdminCinemaRoomLayoutBinding

class AdminCinemaRoomActivity : AppCompatActivity() {
    private lateinit var binding: AdminCinemaRoomLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AdminCinemaRoomLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}