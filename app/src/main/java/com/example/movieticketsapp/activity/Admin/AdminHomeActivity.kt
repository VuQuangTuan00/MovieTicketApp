package com.example.movieticketsapp.activity.Admin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.movieticketsapp.activity.Login.LoginActivity
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

        binding.lnFoodManager.setOnClickListener {
            val intent = Intent(this, AdminFoodManagerActivity::class.java)
            startActivity(intent)
        }

        binding.lnSaleStatistic.setOnClickListener {
            val intent = Intent(this, AdminStatisticsActivity::class.java)
            startActivity(intent)
        }

        binding.btnLogout.setOnClickListener {
            // Đăng xuất khỏi Firebase
            FirebaseAuth.getInstance().signOut()

            // Xóa thông tin ghi nhớ đăng nhập
            val sharedPref = getSharedPreferences("login_prefs", MODE_PRIVATE)
            sharedPref.edit().clear().apply()

            // Hiển thị thông báo
            Toast.makeText(this, "Đăng xuất thành công", Toast.LENGTH_SHORT).show()

            // Điều hướng về màn hình đăng nhập và kết thúc Activity hiện tại
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}