package com.example.movieticketsapp.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.movieticketsapp.BottomSheetDialog.FoodBottomSheetFragment
import com.example.movieticketsapp.R
import com.example.movieticketsapp.adapter.ItemFoodAdapter
import com.example.movieticketsapp.databinding.FoodLayoutBinding
import com.example.movieticketsapp.model.Food

class FoodActivity : AppCompatActivity() {
    private lateinit var binding: FoodLayoutBinding
    private lateinit var adapter: ItemFoodAdapter
    private lateinit var listFood: ArrayList<Food>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize()
        setContentView(R.layout.food_layout)
        setAdapter()
    }
    private fun initialize() {
        binding = FoodLayoutBinding.inflate(layoutInflater)
    }
    private fun setAdapter() {
        val bottomSheet = FoodBottomSheetFragment()
        val layoutManager = GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false)
        binding.rcvFood.layoutManager = layoutManager
        adapter = ItemFoodAdapter(listFood){
            bottomSheet.show(supportFragmentManager, bottomSheet.tag)
        }
        binding.rcvFood.adapter = adapter
    }
}