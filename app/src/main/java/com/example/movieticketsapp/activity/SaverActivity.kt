package com.example.movieticketsapp.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.example.movieticketsapp.R
import com.example.movieticketsapp.databinding.SaverLayoutBinding

class SaverActivity : AppCompatActivity() {
    private lateinit var binding: SaverLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SaverLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Handler(Looper.getMainLooper()).postDelayed({
           val intent = Intent(this, GetStartedActivity::class.java)
            startActivity(intent)
            finish()
        }, 1400)
       setAnimationForLogo()
    }
    private fun setAnimationForLogo(){
        binding.imgLogo.visibility = View.VISIBLE
       val animation = AnimationUtils.loadAnimation(this, R.anim.zoom_in)
        binding.imgLogo.startAnimation(animation)
    }
}