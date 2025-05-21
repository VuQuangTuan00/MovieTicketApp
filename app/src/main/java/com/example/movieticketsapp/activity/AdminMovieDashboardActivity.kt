package com.example.movieticketsapp.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.movieticketsapp.databinding.AdminMovieDashboardLayoutBinding
import com.google.firebase.auth.FirebaseAuth

class AdminMovieDashboardActivity : AppCompatActivity() {
    private lateinit var binding: AdminMovieDashboardLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AdminMovieDashboardLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appbar.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Movie Manager"

        setEventListeners()
    }

    private fun setEventListeners() {
        binding.lnMovieList.setOnClickListener {
            val intent = Intent(this, AdminMovieMovieActivity::class.java)
            startActivity(intent)
        }

        binding.lnGenreList.setOnClickListener {
            val intent = Intent(this, AdminMovieGenreActivity::class.java)
            startActivity(intent)
        }

        binding.lnCastList.setOnClickListener {
            val intent = Intent(this, AdminMovieCastActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}