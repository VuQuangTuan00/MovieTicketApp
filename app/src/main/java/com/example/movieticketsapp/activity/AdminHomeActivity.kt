package com.example.movieticketsapp.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.movieticketsapp.databinding.AdminHomeLayoutBinding
import com.google.firebase.auth.FirebaseAuth

class AdminHomeActivity : AppCompatActivity() {
    private lateinit var binding: AdminHomeLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AdminHomeLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appbar.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.title = "Administrator Dashboard"

        setEventListeners()
    }

    private fun setEventListeners() {
        binding.lnMovieManager.setOnClickListener {
            val intent = Intent(this, AdminMovieDashboardActivity::class.java)
            startActivity(intent)
        }

        binding.lnTheaterManagement.setOnClickListener {
            val intent = Intent(this, AdminTheaterDashbroadActivity::class.java)
            startActivity(intent)
        }

        /*
        binding.lnSaleStatistic.setOnClickListener {
            val intent = Intent(this, SalesStatisticsActivity::class.java)
            startActivity(intent)
        }
         */

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            true
        }
    }
}