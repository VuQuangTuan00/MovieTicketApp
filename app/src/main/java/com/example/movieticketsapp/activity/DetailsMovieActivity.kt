package com.example.movieticketsapp.activity

import android.net.Uri
import android.os.Bundle
import android.widget.VideoView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.movieticketsapp.R
import com.example.movieticketsapp.databinding.DetailsMovieLayoutBinding

class DetailsMovieActivity : AppCompatActivity() {
    private lateinit var binding: DetailsMovieLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DetailsMovieLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val videoUri = Uri.parse("android.resource://${packageName}/${R.raw.gta}")
        binding.vdvTrailer.setVideoURI(videoUri)
        binding.vdvTrailer.start()
    }
}