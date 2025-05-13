package com.example.movieticketsapp.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.movieticketsapp.R
import com.example.movieticketsapp.adapter.MenuAdapter
import com.example.movieticketsapp.databinding.AccountProfileLayoutBinding
import com.example.movieticketsapp.model.MenuItem
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class AccountProfileActivity : AppCompatActivity() {
    private lateinit var binding: AccountProfileLayoutBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var menuAdapter: MenuAdapter
    private lateinit var menuList: ArrayList<MenuItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AccountProfileLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = Firebase.firestore
        menuList = ArrayList()

        setupRecyclerView()
        loadUserData()
        loadMenuItems()
        setupLogoutButton()
    }

    private fun setupRecyclerView() {
        menuAdapter = MenuAdapter(menuList)
        binding.menuList.layoutManager = LinearLayoutManager(this)
        binding.menuList.adapter = menuAdapter
    }

    private fun loadUserData() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val userId = it.uid
            val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)

            userRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: "No Name"
                    val email = document.getString("email") ?: "No Email"
                    val avatarUrl = document.getString("avatar") ?: ""

                    binding.tvNameUser.text = name
                    binding.tvEmailUser.text = email

                    if (avatarUrl.isNotEmpty()) {
                        Glide.with(this)
                            .load(avatarUrl)
                            .circleCrop()
                            .into(binding.imgAvatarUser)
                    }
                } else {
                    Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { e ->
                Log.w("LoadUserData", "Error getting user data", e)
                Toast.makeText(this, "Error loading user data", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadMenuItems() {
        // Mô phỏng dữ liệu menu
        menuList.apply {
            add(MenuItem("Watchlist", R.drawable.ic_watchlist))
            add(MenuItem("Payment Methods", R.drawable.ic_payment))
            add(MenuItem("Personal Info", R.drawable.ic_info))
            add(MenuItem("Security", R.drawable.ic_security))
            add(MenuItem("Notification", R.drawable.ic_notification))
        }
        menuAdapter.notifyDataSetChanged()
    }

    private fun setupLogoutButton() {
        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            true
        }
    }
}