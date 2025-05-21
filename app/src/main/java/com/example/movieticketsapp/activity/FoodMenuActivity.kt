package com.example.movieticketsapp.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.movieticketsapp.R
import com.example.movieticketsapp.adapter.FoodAdapter
import com.example.movieticketsapp.databinding.FoodMenuLayoutBinding
import com.example.movieticketsapp.model.Food
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class FoodMenuActivity : AppCompatActivity() {
    private lateinit var binding: FoodMenuLayoutBinding
    private lateinit var adapter: FoodAdapter
    private val listFood = ArrayList<Food>()
    private val sliderImages = ArrayList<SlideModel>()
    private val db = FirebaseFirestore.getInstance()
    private var sliderListener: ListenerRegistration? = null
    private var foodListener: ListenerRegistration?   = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FoodMenuLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupSlider()
        setupRecycler()
        listenToSliderImages()
        listenToFoodCollectionRealtime()
        setupBottomNavigationView()
    }

    private fun setupToolbar() {
        binding.imgBack.setOnClickListener { finish() }
    }

    private fun setupSlider() {
        binding.imgSlider.setImageList(sliderImages, ScaleTypes.CENTER_CROP)
    }

    private fun setupRecycler() {
        // Thiết lập GridLayoutManager 2 cột
        binding.rcvFood.layoutManager = GridLayoutManager(this, 2)
        adapter = FoodAdapter(listFood) { food ->
            // Xử lý khi click vào 1 món, ví dụ show Toast
            Toast.makeText(this, "Bạn chọn: ${food.food_name}", Toast.LENGTH_SHORT).show()
        }
        binding.rcvFood.adapter = adapter
        binding.rcvFood.isNestedScrollingEnabled = false
    }

    private fun listenToSliderImages() {
        sliderListener = db.collection("list_img_food")
            .addSnapshotListener { snaps, e ->
                if (e != null) {
                    Log.w("SliderRealtime", "Listen failed.", e)
                    return@addSnapshotListener
                }
                sliderImages.clear()
                snaps?.forEach { doc ->
                    doc.getString("img")?.let { url ->
                        sliderImages.add(SlideModel(url, ScaleTypes.CENTER_CROP))
                    }
                }
                binding.imgSlider.setImageList(sliderImages, ScaleTypes.CENTER_CROP)
            }
    }

    private fun listenToFoodCollectionRealtime() {
        foodListener = db.collection("food")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("FoodRealtime", "Listen failed.", e)
                    Toast.makeText(this, "Không thể tải thực đơn", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                snapshots?.let {
                    listFood.clear()
                    for (doc in it) {
                        val name    = doc.getString("food_name").orEmpty()
                        val imgFood = doc.getString("img_food").orEmpty()
                        if (name.isNotEmpty()) {
                            listFood.add(
                                Food(
                                    food_id          = doc.id,
                                    img_food     = imgFood,
                                    food_name    = name,
                                    price       = doc.getDouble("price") ?: 0.0,
                                    description = doc.getString("description").orEmpty(),
                                    status      = doc.getString("status").orEmpty()
                                )
                            )
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }

    private fun setupBottomNavigationView() {
        binding.bottomnvg.selectedItemId = R.id.food_order
        binding.bottomnvg.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    startActivity(Intent(this, HomaPageActivity::class.java))
                    true
                }
                R.id.food_order -> true  // đang ở màn này
                //R.id.ticket -> {
                  //  startActivity(Intent(this, TicketActivity::class.java))
                    //true
                //}
                R.id.account -> {
                    startActivity(Intent(this, AccountProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sliderListener?.remove()
        foodListener?.remove()
    }
}
